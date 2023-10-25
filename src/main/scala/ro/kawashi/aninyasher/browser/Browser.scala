package ro.kawashi.aninyasher.browser

import java.net.Proxy
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.text
import org.jsoup.{Connection, Jsoup}
import org.jsoup.Connection.Method.POST
import ro.kawashi.aninyasher.browser.features.Feature

object Browser {
  private val userAgent = s"${ro.kawashi.aninyasher.name}/${ro.kawashi.aninyasher.version}"

  def apply(userAgent: String = userAgent, proxy: Proxy = null): Browser = {
    new Browser(userAgent, proxy)
  }
}

class Browser(override val userAgent: String = Browser.userAgent,
              override val proxy: Proxy = null,
              private val features: List[Feature] = Nil) extends JsoupBrowser(userAgent, proxy) {

  def applyFeature(feature: Feature): Browser = {
    new Browser(userAgent, proxy, feature :: features)
  }

  def getJson(url: String): ujson.Value = {
    val jsonText = get(url) >> text("body")
    ujson.read(jsonText)
  }

  def postJson(url: String, data: ujson.Value): ujson.Value = {
    val jsonText = data.render()
    val pipeline = (conn => defaultRequestSettings(conn))
      .andThen(requestSettings)
      .andThen(executeRequest)
      .andThen(processResponse)

    val responseText = pipeline(Jsoup.connect(url).method(POST).proxy(proxy).requestBody(jsonText)) >> text("body")
    ujson.read(responseText)
  }

  override protected[this] def processResponse(res: Connection.Response): JsoupDocument = {
    val doc = super.processResponse(res)
    features.foreach(_.onDocumentReceived(doc))

    doc
  }

  override def requestSettings(conn: Connection): Connection = {
    super.requestSettings(conn)

    conn.header("Connection", "close")
    conn.timeout(60000)

    features.foldLeft(conn)((c, f) => f.modifyConnection(c))
  }
}
