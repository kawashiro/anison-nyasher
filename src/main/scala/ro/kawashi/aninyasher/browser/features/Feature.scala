package ro.kawashi.aninyasher.browser.features

import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import org.jsoup.Connection

trait Feature {
  def modifyConnection(conn: Connection): Connection

  def onDocumentReceived(doc: JsoupDocument): Unit
}
