package ro.kawashi.aninyasher.browser.features

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.jsoup.Connection

class Referer(initialReferer: Option[String] = None) extends Feature {

  private[this] var lastDocumentUrl: Option[String] = initialReferer

  override def modifyConnection(conn: Connection): Connection = {
    lastDocumentUrl.foreach(conn.header("Referer", _))
    conn
  }

  override def onDocumentReceived(doc: JsoupBrowser.JsoupDocument): Unit = {
    lastDocumentUrl = Some(doc.location)
  }
}
