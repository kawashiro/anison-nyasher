package ro.kawashi.aninyasher.browser.features

import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import org.jsoup.Connection

class AcceptAny extends Feature {
  override def modifyConnection(conn: Connection): Connection = {
    conn
      .ignoreContentType(true)
      .header("Accept", "*/*")
  }

  override def onDocumentReceived(doc: JsoupDocument): Unit = {}
}
