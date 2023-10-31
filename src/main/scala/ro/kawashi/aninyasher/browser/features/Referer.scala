package ro.kawashi.aninyasher.browser.features

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.jsoup.Connection

/**
 * A feature to save the last document url as the referer for the next request.
 *
 * @param initialReferer initial referer if needed
 */
class Referer(initialReferer: Option[String] = None) extends Feature {

  /**
   * The last document url.
   */
  private[this] var lastDocumentUrl: Option[String] = initialReferer

  /**
   * Add Referer header to the connection.
   *
   * @param conn jsoup connection
   * @return Connection
   */
  override def modifyConnection(conn: Connection): Connection = {
    lastDocumentUrl.foreach(conn.header("Referer", _))
    conn
  }

  /**
   * Save the document url as the last document url.
   *
   * @param doc jsoup document
   */
  override def onDocumentReceived(doc: JsoupBrowser.JsoupDocument): Unit = {
    lastDocumentUrl = Some(doc.location)
  }
}
