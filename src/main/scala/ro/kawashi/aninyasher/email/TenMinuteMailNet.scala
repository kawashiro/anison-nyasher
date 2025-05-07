package ro.kawashi.aninyasher.email

import scala.annotation.tailrec
import scala.collection.mutable.{Set => MutableSet}

import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{attr, attrs, text}
import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.browser.Browser
import ro.kawashi.aninyasher.remoteservice.RemoteService

/**
 * Companion object for TenMinuteMailNet
 */
object TenMinuteMailNet {

  private val baseUrl = "https://10minutemail.net"
  private val pollingInterval = 5000

  /**
   * Create a new instance.
   *
   * @return TenMinuteMailNet
   */
  def apply(): TenMinuteMailNet = {
    new TenMinuteMailNet(Browser())
  }
}

/**
 * 10minutemail.net service integration impl.
 *
 * @param browser Browser
 */
class TenMinuteMailNet(override protected val browser: Browser)
  extends RemoteService(browser) with TemporaryInbox with Logging {

  private val processedEmails = MutableSet[String]()

  /**
   * Create a new inbox and return it's address.
   *
   * @param preferredLogin String
   * @return String
   */
  override def create(preferredLogin: String): String = {
    browser.get(s"${TenMinuteMailNet.baseUrl}/new.html") >> attr("value")("input#fe_text")
  }

  /**
   * Process each new email.
   *
   * @param fn Callback function (returns None if email is not expected by it's content)
   * @tparam T Callback return type
   * @return T
   */
  @tailrec
  override final def onNewMail[T](fn: Email => Option[T]): T = {
    var res: Option[T] = None
    logger.debug("Polling for new emails...")
    val emailUrls = browser.get(TenMinuteMailNet.baseUrl) >> attrs("href")("td a.row-link")
    emailUrls.toSet.foreach((url: String) => {
      if (res.isEmpty && !processedEmails.contains(url)) {
        val emailPage = browser.get(s"${TenMinuteMailNet.baseUrl}/$url")
        val subject = emailPage >> text("div.mail_header h2")
        val from = emailPage >> text("span.mail_from")
        val body = emailPage >> text("div.mailinhtml")

        logger.debug(s"Received a new email from $from")
        res = fn(Email(subject, from, body))

        processedEmails.add(url)
      }
    })

    if (res.isDefined) {
      return res.get
    }

    Thread.sleep(TenMinuteMailNet.pollingInterval)
    onNewMail(fn)
  }
}
