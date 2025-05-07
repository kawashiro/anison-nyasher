package ro.kawashi.aninyasher.browser

import java.io.{FileOutputStream, OutputStream}
import java.net.{Proxy, URL}

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.text
import org.jsoup.{Connection, Jsoup}
import org.jsoup.Connection.Method.{GET, POST}
import org.jsoup.helper.HttpConnection

import ro.kawashi.aninyasher.browser.features.Feature

/**
 * A browser companion object.
 */
object Browser {

  private val userAgent = s"${ro.kawashi.aninyasher.name}/${ro.kawashi.aninyasher.version}"
  private val connectionTimeout = 10000

  /**
   * Create a new browser instance.
   *
   * @param userAgent user agent
   * @param proxy proxy
   * @return Browser
   */
  def apply(userAgent: String = userAgent, proxy: Option[Proxy] = None): Browser = {
    new Browser(userAgent, proxy)
  }
}

/**
 * A browser is a wrapper around a jsoup browser.
 *
 * @param userAgent user agent
 * @param proxyOpt proxy
 * @param features a list of additional features
 */
class Browser(override val userAgent: String = Browser.userAgent,
              private val proxyOpt: Option[Proxy] = None,
              private val features: List[Feature] = Nil) extends JsoupBrowser(userAgent, proxyOpt.orNull) {

  /**
   * Add a new feature to the browser.
   *
   * @param feature Feature
   * @return Browser
   */
  def applyFeature(feature: Feature): Browser = {
    new Browser(userAgent, proxyOpt, feature :: features)
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
   * TURBOHACK - Workaround for anison-incompatible URL encoding implemented in Jsoup.
   *
   * @param url URL
   * @return JsoupDocument
   */
  def get(url: URL): JsoupDocument = {
    val pipeline = (conn => defaultRequestSettings(conn))
      .andThen(requestSettings)
      .andThen(executeRequest)
      .andThen(processResponse)

    pipeline(HttpConnection.connect(url).method(GET).proxy(proxy))
  }

  /**
   * Download a file.
   *
   * @param url String
   * @param path String
   */
  def download(url: String, path: String): Unit = {
    download(url, new FileOutputStream(path))
  }

  /**
   * Download a file to a stream.
   *
   * @param url  String
   * @param outputStream OutputStream
   */
  def download(url: String, outputStream: OutputStream): Unit = {
    val pipeline = (conn => defaultRequestSettings(conn))
      .andThen(requestSettings)
      .andThen(executeRequest)

    val response = pipeline(Jsoup.connect(url).method(GET).proxy(proxy))

    try {
      outputStream.write(response.bodyAsBytes())
    } finally {
      outputStream.close()
    }
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
