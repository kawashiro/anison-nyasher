package ro.kawashi.aninyasher.remoteservice

import java.net.Proxy

import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{attr, text}
import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.browser.Browser
import ro.kawashi.aninyasher.browser.features.Referer
import ro.kawashi.aninyasher.remoteservice.anison.AnisonException

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

  case class CaptchaChallenge(url: String, key: String)

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

  def getVoters: Set[Int] = {
    val jsonData = getStatusData
    jsonData("orders_list").arr.map(el => el("userid").str.toInt).toSet
  }

  def getUserLogin(userId: Int): String = {
    val fullName = browser.get(s"${Anison.anisonBaseUrl}/user/$userId") >> text("div.userdata > h2")
    val nameParts = fullName.split(" ")
    val login = nameParts(if (nameParts.length == 1) 0 else 1)
    login.slice(1, login.length - 1)
  }

  def login(login: String, password: String): Unit = {
    val error = browser.post(s"${Anison.anisonBaseUrl}/user/login", Map(
      "login" -> login,
      "password" -> password,
      "authform" -> "Логин"
    )) >?> text("div.anime_blocked")

    error.foreach(err => throw new AnisonException(s"Failed to log in anison.fm as $login: $err"))
  }

  def getRegistrationCaptchaChallenge: CaptchaChallenge = {
    val registrationUrl = s"${Anison.anisonBaseUrl}/user/join"
    val challenge = browser.get(registrationUrl) >> attr("data-sitekey")("div.g-recaptcha")
    if (challenge.isEmpty) {
      throw new AnisonException("Unable to get registration captcha challenge")
    }

    CaptchaChallenge(registrationUrl, challenge)
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

  def register(login: String, password: String, email: String, captcha: String): Unit = {
    val response = browser.post(s"${Anison.anisonBaseUrl}/user/join", Map(
      "login" -> login,
      "password" -> password,
      "email" -> email,
      "g-recaptcha-response" -> captcha,
      "authform" -> "Регистрация"
    ))

    if ((response >?> text("div.restore_success")).isEmpty) {
      throw new AnisonException("Unable to register a new account")
    }
  }

  def confirmEmail(token: String): Unit = {
    val success = browser.get(s"${Anison.anisonBaseUrl}/user/join-confirm?token=$token") >> text("div.restore_success")
    if (success.isEmpty) {
      throw new AnisonException(s"Something went wrong confirming e-mail (token: $token)")
    }
  }

  private def getStatusData: ujson.Value = {
    browser.getJson(s"${Anison.anisonBaseUrl}/status.php")
  }
}
