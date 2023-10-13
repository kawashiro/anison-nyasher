package ro.kawashi.aninyasher.command

import net.ruippeixotog.scalascraper.scraper.ContentExtractors.text
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.OptParser.Config
import ro.kawashi.aninyasher.browser.Browser
import ro.kawashi.aninyasher.browser.features.{AcceptAny, Referer}
import ro.kawashi.aninyasher.proxy.TorProxyProvider
import ro.kawashi.aninyasher.tor.PosixTorProcess
import ro.kawashi.aninyasher.useragent.BuiltInUserAgentList

class OnAirCommand extends Command with Logging {

  override def run(config: Config): Unit = {
    logger.info("Hello, Anison!!!")
    logger.info(s"Nyasher version: ${ro.kawashi.aninyasher.version}")

    val tor = new PosixTorProcess("build/tor/dist/bin/tor")
    tor.start()

    val userAgents = BuiltInUserAgentList()

    TorProxyProvider(tor).foreach(proxy => {
      val ua = userAgents.next()
      logger.info(s"Using user agent: $ua")

      val anisonBrowser = Browser(proxy = proxy).applyFeature(new Referer("https://anison.fm/"))
      val ipdataBrowser = Browser(proxy = proxy).applyFeature(new AcceptAny)
      val ipData = ipdataBrowser.get("http://ip-api.com/json") >> text("body")

      val jsonText = anisonBrowser.get("https://anison.fm/status.php") >> text("body")
      val title = ujson.read(jsonText)("on_air")(config.attribute).str

      logger.info(s"Currently on air: $title")
      logger.info(s"Tor status: ${tor.getStatus}")
      logger.info(s"IP Data: $ipData")

      Thread.sleep(10000)
    })
  }
}
