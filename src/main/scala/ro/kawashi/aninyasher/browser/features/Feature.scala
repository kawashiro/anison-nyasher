package ro.kawashi.aninyasher.browser.features

import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import org.jsoup.Connection

/**
 * A feature is a piece of functionality that can be added to a browser instance.
 */
trait Feature {

  /**
   * Modify the connection before it is used to fetch the document.
   *
   * @param conn jsoup connection
   * @return Connection
   */
  def modifyConnection(conn: Connection): Connection

  /**
   * Is invoked when the document is received.
   *
   * @param doc jsoup document
   */
  def onDocumentReceived(doc: JsoupDocument): Unit
}
