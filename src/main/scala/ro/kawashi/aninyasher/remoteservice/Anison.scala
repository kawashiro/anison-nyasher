package ro.kawashi.aninyasher.remoteservice

import java.net.Proxy

import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.text

import ro.kawashi.aninyasher.browser.Browser
import ro.kawashi.aninyasher.browser.features.Referer


object Anison {
  private val anisonBaseUrl = "https://anison.fm"

  def apply(userAgent: String, proxy: Proxy): Anison = {
    val browser = Browser(userAgent = userAgent, proxy = proxy)
      .applyFeature(new Referer(Some(anisonBaseUrl)))

    new Anison(browser)
  }
}

class Anison(override protected val browser: Browser) extends RemoteService(browser) {

  case class SongInfo(anime: String, title: String)

  def getCurrentlyOnAir: SongInfo = {
    val jsonText = browser.get(s"${Anison.anisonBaseUrl}/status.php") >> text("body")
    val jsonData = ujson.read(jsonText)

    SongInfo(jsonData("on_air")("anime").str, jsonData("on_air")("track").str)
  }
}
