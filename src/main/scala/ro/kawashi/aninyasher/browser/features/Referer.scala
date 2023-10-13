package ro.kawashi.aninyasher.browser.features

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.jsoup.Connection

class Referer(initialReferer: String = null) extends Feature {

  private[this] var lastDocumentUrl: String = initialReferer

  override def modifyConnection(conn: Connection): Connection = {
    if (lastDocumentUrl != null) {
      conn.header("Referer", lastDocumentUrl)
    }
    conn
  }

  override def onDocumentReceived(doc: JsoupBrowser.JsoupDocument): Unit = {
    lastDocumentUrl = doc.location
  }
}
