package ro.kawashi.aninyasher.browser.features

import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import org.jsoup.Connection

/**
 * A feature to accept any content type.
 */
class AcceptAny extends Feature {

  /**
   * Add Accept header to the connection.
   *
   * @param conn jsoup connection
   * @return Connection
   */
  override def modifyConnection(conn: Connection): Connection = {
    conn
      .ignoreContentType(true)
      .header("Accept", "*/*")
  }

  /**
   * Do nothing
   *
   * @param doc jsoup document
   */
  override def onDocumentReceived(doc: JsoupDocument): Unit = {}
}
