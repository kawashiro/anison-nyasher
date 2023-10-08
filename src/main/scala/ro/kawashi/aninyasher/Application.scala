package ro.kawashi.aninyasher

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.text
import org.jsoup.Connection
import java.net.Proxy

class AnisonBrowser(userAgent: String = "jsoup/1.8", proxy: Proxy = null) extends JsoupBrowser(userAgent, proxy) {

  override def requestSettings(conn: Connection): Connection = {
    super.requestSettings(conn)
    conn.header("Referer", "https://anison.fm/")
  }
}


object Application extends App {
  println("Hello, Anison!!!")
  val browser = new AnisonBrowser(userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0")

  while (true) {
    val jsonText = browser.get("https://anison.fm/status.php") >> text("body")
    val title = ujson.read(jsonText)("on_air")("anime").str

    println(s"Currently on air: $title")

    Thread.sleep(10000)
  }
}
