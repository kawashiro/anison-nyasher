package ro.kawashi.aninyasher.remoteservice

import java.net.Proxy

import scala.annotation.tailrec

import scala.util.{Failure, Random, Success, Try}
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.text
import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.browser.Browser
import ro.kawashi.aninyasher.browser.features.Referer

class AnisonException(message: String) extends RuntimeException(message)

object Anison {
  private val anisonBaseUrl = "https://anison.fm"

  def apply(userAgent: String, proxy: Proxy = null): Anison = {
    val browser = Browser(userAgent = userAgent, proxy = proxy)
      .applyFeature(new Referer(Some(anisonBaseUrl)))

    new Anison(browser)
  }
}

class Anison(override protected val browser: Browser) extends RemoteService(browser) with Logging {

  case class SongInfo(anime: String, title: String)

  case class SongStatus(votes: Int, topSongVotes: Int)

  private val random = new Random()

  def getCurrentlyOnAir: SongInfo = {
    val jsonData = getStatusData
    SongInfo(jsonData("on_air")("anime").str, jsonData("on_air")("track").str)
  }

  def getSongStatus(songId: Int): SongStatus = {
    def votesOrZero(data: ujson.Value, filter: Int => Boolean): Int = {
      val iterator = data("orders_list").arr.filter(el => filter(el("song_id").str.toInt))
      if (iterator.isEmpty) 0 else iterator.head("votes").str.toInt
    }

    val jsonData = getStatusData
    SongStatus(votesOrZero(jsonData, _ == songId), votesOrZero(jsonData, _ != songId))
  }

  private def getVoters: Set[Int] = {
    val jsonData = getStatusData
    jsonData("orders_list").arr.map(el => el("userid").str.toInt).toSet
  }

  private def getUserLogin(userId: Int): String = {
    val fullName = browser.get(s"${Anison.anisonBaseUrl}/user/$userId") >> text("div.userdata > h2")
    val nameParts = fullName.split(" ")
    val login = nameParts(if (nameParts.length == 1) 0 else 1)
    login.slice(1, login.length - 1)
  }

  def getRandomLogin: String = {
    val maxId = getVoters.max

    @tailrec
    def selectLoop(): String = {
      val id = random.nextInt(maxId)
      Try(getUserLogin(id)) match {
        case Success(value) => value
        case Failure(exception) =>
          logger.debug(s"Failed to get user data with id $id: ${exception.getMessage}")
          selectLoop()
      }
    }

    selectLoop()
  }

  def login(login: String, password: String): Unit = {
    val error = browser.post(s"${Anison.anisonBaseUrl}/user/login", Map(
      "login" -> login,
      "password" -> password,
      "authform" -> "Логин"
    )) >?> text("div.anime_blocked")

    error.foreach(err => throw new AnisonException(s"Failed to log in anison.fm as $login: $err"))
  }

  def vote(songId: Int, comment: String = ""): Unit = {
    val error = browser.post(s"${Anison.anisonBaseUrl}/song_actions.php", Map(
      "action" -> "up",
      "song" -> songId.toString,
      "premium" -> "0",
      "comment" -> comment,
    )) >?> text("div.error")

    error.foreach(err => throw new AnisonException(s"Unable to vote for the song #$songId: $err"))
  }

  private def getStatusData: ujson.Value = {
    val jsonText = browser.get(s"${Anison.anisonBaseUrl}/status.php") >> text("body")
    ujson.read(jsonText)
  }
}
