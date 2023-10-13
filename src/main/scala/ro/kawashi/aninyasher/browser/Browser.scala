package ro.kawashi.aninyasher.browser

import java.net.Proxy

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import org.jsoup.Connection
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

  override protected[this] def processResponse(res: Connection.Response): JsoupDocument = {
    val doc = super.processResponse(res)
    features.foreach(_.onDocumentReceived(doc))

    doc
  }

  override def requestSettings(conn: Connection): Connection = {
    super.requestSettings(conn)
    conn.header("Connection", "close")

    features.foldLeft(conn)((c, f) => f.modifyConnection(c))
  }
}
