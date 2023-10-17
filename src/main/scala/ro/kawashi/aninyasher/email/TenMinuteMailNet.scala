package ro.kawashi.aninyasher.email

import scala.annotation.tailrec
import scala.collection.mutable.{Set => MutableSet}

import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{attr, attrs, text}
import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.browser.Browser
import ro.kawashi.aninyasher.remoteservice.RemoteService

object TenMinuteMailNet {

  private val baseUrl = "https://10minutemail.net"

  def apply(): TenMinuteMailNet = {
    new TenMinuteMailNet(Browser())
  }
}

class TenMinuteMailNet(override protected val browser: Browser)
  extends RemoteService(browser) with TemporaryInbox with Logging {

  private val processedEmails = MutableSet[String]()

  override def create(): String = {
    browser.get(s"${TenMinuteMailNet.baseUrl}/new.html") >> attr("value")("input#fe_text")
  }

  override def onNewMail(fn: Email => Option[String]): String = {
    @tailrec
    def pollingLoop(): String = {
      var res: Option[String] = None
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

      Thread.sleep(5000)
      pollingLoop()
    }

    pollingLoop()
  }
}
