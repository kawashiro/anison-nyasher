package ro.kawashi.aninyasher

import java.net.Proxy

import org.apache.logging.log4j.scala.Logging
import org.jsoup.Connection
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.text

import ro.kawashi.aninyasher.proxy.TorProxyProvider
import ro.kawashi.aninyasher.tor.PosixTorProcess

class AnisonBrowser(userAgent: String = "jsoup/1.8", proxy: Proxy = null) extends JsoupBrowser(userAgent, proxy) {

  override def requestSettings(conn: Connection): Connection = {
    super.requestSettings(conn)
    conn.header("Referer", "https://anison.fm/")
  }
}

object Application extends App with Logging {
  val appVersion = getClass.getPackage.getImplementationVersion

  logger.info("Hello, Anison!!!")
  logger.info(s"Nyasher version: $appVersion")

  val tor = new PosixTorProcess("build/tor/dist/bin/tor")
  tor.start()

  new TorProxyProvider(tor).foreach(proxy => {
    val browser = new AnisonBrowser(userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0", proxy = proxy)
    val ipData = browser.get("https://api.myip.com") >> text("body")

    val jsonText = browser.get("https://anison.fm/status.php") >> text("body")
    val title = ujson.read(jsonText)("on_air")("anime").str

    logger.info(s"Currently on air: $title")
    logger.info(s"Tor status: ${tor.getStatus}")
    logger.info(s"IP Data: $ipData")

    Thread.sleep(10000)
  })
}
