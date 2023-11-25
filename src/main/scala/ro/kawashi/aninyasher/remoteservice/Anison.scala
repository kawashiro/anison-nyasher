package ro.kawashi.aninyasher.remoteservice

import java.io.ByteArrayOutputStream
import java.net.{Proxy, URL}

import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, XML}

import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{attr, elements, text, texts}
import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.browser.Browser
import ro.kawashi.aninyasher.browser.features.Referer
import ro.kawashi.aninyasher.remoteservice.Anison._
import ro.kawashi.aninyasher.remoteservice.anison.{AnisonException, SongNotVotableException}

/**
 * Companion object for Anison.
 */
object Anison {

  /**
   * Song info.
   *
   * @param anime String
   * @param title String
   */
  case class SongInfo(anime: String, title: String)

  /**
   * Song status in the airing queue.
   *
   * @param votes        Int
   * @param topSongVotes Int
   */
  case class SongStatus(votes: Int, topSongVotes: Int)

  /**
   * ReCaptcha request
   *
   * @param url String
   * @param key String
   */
  case class CaptchaChallenge(url: String, key: String)

  /**
   * Song info per anime.
   *
   * @param id     Int
   * @param album  String
   * @param artist String
   * @param title  String
   */
  case class AnimeSongInfo(id: Int, album: String, artist: String, title: String) {

    /**
     * Convert entity to XML
     *
     * @return Elem
     */
    def toXML: Elem = {
      <song>
        <id>{id}</id>
        <album>{album}</album>
        <artist>{artist}</artist>
        <title>{title}</title>
      </song>
    }
  }

  /**
   * Anime info object.
   *
   * @param titleRu Cyrillic title
   * @param titleEn Romaji title
   * @param genres  Genres list
   * @param years   Years anime has been aired
   * @param songs   Songs list
   */
  case class AnimeInfo(titleRu: String, titleEn: String, genres: Array[String],
                       years: Set[Int], songs: Array[AnimeSongInfo]) {

    /**
     * Convert entity to XML
     *
     * @return Elem
     */
    def toXML: Elem = {
      <anime>
        <title_ru>{titleRu}</title_ru>
        <title_en>{titleEn}</title_en>
        <genres>
          {genres.map(genre => <genre>{genre}</genre>)}
        </genres>
        <years>
          {years.map(year => <year>{year}</year>)}
        </years>
        <songs>
          {songs.map(_.toXML)}
        </songs>
      </anime>
    }
  }

  private val anisonBaseUrl = "https://anison.fm"

  /**
   * Create a new Anison instance.
   *
   * @param userAgent String
   * @param proxy Proxy
   * @return Anison
   */
  // scalastyle:off null
  def apply(userAgent: String, proxy: Proxy = null): Anison = {
    val browser = Browser(userAgent = userAgent, proxy = proxy)
      .applyFeature(new Referer(Some(anisonBaseUrl)))

    new Anison(browser)
  }
  // scalastyle:on null
}

/**
 * Anison service integration implementation.
 *
 * @param browser Browser
 */
class Anison(override protected val browser: Browser) extends RemoteService(browser) with Logging {


  /**
   * Get currently on-air song.
   *
   * @return SongInfo
   */
  def getCurrentlyOnAir: SongInfo = {
    val jsonData = getStatusData
    SongInfo(jsonData("on_air")("anime").str, jsonData("on_air")("track").str)
  }

  /**
   * Get song status in the airing queue.
   *
   * @param songId Int
   * @return SongStatus
   */
  def getSongStatus(songId: Int): SongStatus = {
    def votesOrZero(data: ujson.Value, filter: Int => Boolean): Int = {
      val iterator = data("orders_list").arr.filter(el => filter(el("song_id").str.toInt) && el("votes").str.toInt < 99)
      if (iterator.isEmpty) 0 else iterator.head("votes").str.toInt
    }

    val jsonData = getStatusData
    SongStatus(votesOrZero(jsonData, _ == songId), votesOrZero(jsonData, _ != songId))
  }

  /**
   * Get users IDs from the voting queue
   *
   * @return Set[Int]
   */
  def getVoters: Set[Int] = {
    val jsonData = getStatusData
    jsonData("orders_list").arr.map(el => el("userid").str.toInt).toSet
  }

  /**
   * Get user login
   *
   * @param userId Int
   * @return String
   */
  def getUserLogin(userId: Int): String = {
    val fullName = browser.get(s"${Anison.anisonBaseUrl}/user/$userId") >> text("div.userdata > h2")
    val nameParts = fullName.split(" ")
    val login = nameParts(if (nameParts.length == 1) 0 else 1)
    login.slice(1, login.length - 1)
  }

  /**
   * Authorize at Anison
   *
   * @param login String
   * @param password String
   */
  def login(login: String, password: String): Unit = {
    val error = browser.post(s"${Anison.anisonBaseUrl}/user/login", Map(
      "login" -> login,
      "password" -> password,
      "authform" -> "Логин"
    )) >?> text("div.anime_blocked")

    error.foreach(err => throw new AnisonException(s"Failed to log in anison.fm as $login: $err"))
  }

  /**
   * Get captcha challenge from the registration page
   *
   * @return CaptchaChallenge
   */
  def getRegistrationCaptchaChallenge: CaptchaChallenge = {
    val registrationUrl = s"${Anison.anisonBaseUrl}/user/join"
    val challenge = browser.get(registrationUrl) >> attr("data-sitekey")("div.g-recaptcha")
    if (challenge.isEmpty) {
      throw new AnisonException("Unable to get registration captcha challenge")
    }

    CaptchaChallenge(registrationUrl, challenge)
  }

  /**
   * Vote for the song
   *
   * @param songId Int
   * @param comment String
   */
  def vote(songId: Int, comment: String = ""): Unit = {
    val error = browser.post(s"${Anison.anisonBaseUrl}/song_actions.php", Map(
      "action" -> "up",
      "song" -> songId.toString,
      "premium" -> "0",
      "comment" -> comment,
    )) >?> text("div.error")

    error.foreach(err => {
      if (err.contains("звучал")) {
        throw new SongNotVotableException(s"Song #$songId was already aired: $err")
      } else {
        throw new AnisonException(s"Unable to vote for the song #$songId: $err")
      }
    })
  }

  /**
   * Register a new account
   *
   * @param login String
   * @param password String
   * @param email String
   * @param captcha String
   */
  def register(login: String, password: String, email: String, captcha: String): Unit = {
    browser.get(s"${Anison.anisonBaseUrl}/suggestion.php?login=$login")

    val response = browser.post(s"${Anison.anisonBaseUrl}/user/join", Map(
      "login" -> login,
      "password" -> password,
      "email" -> email,
      "g-recaptcha-response" -> captcha,
      "authform" -> "Регистрация"
    ))

    if ((response >?> text("div.restore_success")).isEmpty) {
      val errors = response >> texts("span.help-inline span")
      throw new AnisonException("Unable to register a new account: " + errors.mkString(", "))
    }
  }

  /**
   * Confirm email by the token sent to the mailbox
   *
   * @param token String
   */
  def confirmEmail(token: String): Unit = {
    val success = browser.get(s"${Anison.anisonBaseUrl}/user/join-confirm?token=$token") >> text("div.restore_success")
    if (success.isEmpty) {
      throw new AnisonException(s"Something went wrong confirming e-mail (token: $token)")
    }
  }

  /**
   * Process each anime at anison.fm
   *
   * @param fn Function accepts an AnimeInfo object
   */
  def foreachAnimeInfo(fn: AnimeInfo => Unit): Unit = {
    getAnimeLinks.foreach { link =>
      Try(getAnimeInfo(link)) match {
        case Success(value) => fn(value)
        case Failure(exception) => logger.error(s"Failed to get anime info from $link: ${exception.getMessage}")
      }
    }
  }

  private def getAnimeInfo(link: String): AnimeInfo = {
    val document = browser.get(new URL(link))

    val titleRu = document >> text("span[itemprop=name]")
    val titleEn = document >?> text("span.title_alt")
    val genres = (document >?> text("td[itemprop=genre]")).map(_.split(", ")).getOrElse(Array.empty[String])

    val fromTo  = (document >> texts("div.alt_title tr"))
      .filter(_.contains("период выхода"))
      .flatMap(x => "\\d{4}".r.findAllIn(x))
      .map(_.toInt)
      .toList
    val years = fromTo match {
      case List(from, to) => (from to to).toSet
      case List(from) => Set(from)
      case _ => Set.empty[Int]
    }

    val songs = (document >> elements("div.album"))
      .flatMap(albumEl => {
        val album = albumEl >> text("span.title")
        (albumEl >> elements("div.titem"))
          .map(songEl => {
            val songId = (songEl >> attr("data-song")("div.song_item")).toInt
            val songTitle = songEl >?> text("a.local_link")
            val songArtist = songEl >?> text("span[itemprop=name]")
            AnimeSongInfo(songId, album, songArtist.getOrElse(""), songTitle.getOrElse(""))
          })
      })
      .toArray

    AnimeInfo(titleRu, titleEn.getOrElse(""), genres, years, songs)
  }

  private def getAnimeLinks: Iterable[String] = {
    val stream = new ByteArrayOutputStream()
    browser.download(s"${Anison.anisonBaseUrl}/sitemap.xml", stream)

    val xml = XML.loadString(stream.toString("UTF-8"))

    (xml \\ "loc").map(_.text)
      .flatMap(
        s"^${Anison.anisonBaseUrl}/catalog/\\d+/[^/]+$$".r
          .findFirstMatchIn(_)
          .map(_.group(0))
      )
  }

  private def getStatusData: ujson.Value = {
    browser.getJson(s"${Anison.anisonBaseUrl}/status.php")
  }
}
