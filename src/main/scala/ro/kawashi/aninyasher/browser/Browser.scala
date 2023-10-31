package ro.kawashi.aninyasher.browser

import java.net.Proxy

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.text
import org.jsoup.{Connection, Jsoup}
import org.jsoup.Connection.Method.POST

import ro.kawashi.aninyasher.browser.features.Feature

/**
 * A browser companion object.
 */
object Browser {

  private val userAgent = s"${ro.kawashi.aninyasher.name}/${ro.kawashi.aninyasher.version}"
  private val connectionTimeout = 60000

  /**
   * Create a new browser instance.
   *
   * @param userAgent user agent
   * @param proxy proxy
   * @return Browser
   */
  // scalastyle:off null
  def apply(userAgent: String = userAgent, proxy: Proxy = null): Browser = {
    new Browser(userAgent, proxy)
  }
  // scalastyle:on null
}

/**
 * A browser is a wrapper around a jsoup browser.
 *
 * @param userAgent user agent
 * @param proxy proxy
 * @param features a list of additional features
 */
// scalastyle:off null
class Browser(override val userAgent: String = Browser.userAgent,
              override val proxy: Proxy = null,
              private val features: List[Feature] = Nil) extends JsoupBrowser(userAgent, proxy) {
// scalastyle:on null

  /**
   * Add a new feature to the browser.
   *
   * @param feature Feature
   * @return Browser
   */
  def applyFeature(feature: Feature): Browser = {
    new Browser(userAgent, proxy, feature :: features)
  }

  /**
   * Perform a GET request and return the response as a JSON object.
   *
   * @param url String
   * @return ujson.Value
   */
  def getJson(url: String): ujson.Value = {
    val jsonText = get(url) >> text("body")
    ujson.read(jsonText)
  }

  /**
   * Perform a POST request with JSON payload and return the response as a JSON object.
   *
   * @param url String
   * @param data ujson.Value
   * @return ujson.Value
   */
  def postJson(url: String, data: ujson.Value): ujson.Value = {
    val jsonText = data.render()
    val pipeline = (conn => defaultRequestSettings(conn))
      .andThen(requestSettings)
      .andThen(executeRequest)
      .andThen(processResponse)

    val responseText = pipeline(Jsoup.connect(url).method(POST).proxy(proxy).requestBody(jsonText)) >> text("body")
    ujson.read(responseText)
  }

  /**
   * Apply features to the document received.
   *
   * @param res Connection.Response
   * @return JsoupDocument
   */
  override protected[this] def processResponse(res: Connection.Response): JsoupDocument = {
    val doc = super.processResponse(res)
    features.foreach(_.onDocumentReceived(doc))

    doc
  }

  /**
   * Apply features to the connection and some other common hacks.
   *
   * @param conn Connection
   * @return Connection
   */
  override def requestSettings(conn: Connection): Connection = {
    super.requestSettings(conn)

    conn.header("Connection", "close")
    conn.timeout(Browser.connectionTimeout)

    features.foldLeft(conn)((c, f) => f.modifyConnection(c))
  }
}
