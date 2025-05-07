package ro.kawashi.aninyasher.email

import scala.annotation.tailrec

import org.apache.logging.log4j.scala.Logging
import so.tempmail.TempMailSoClient

import ro.kawashi.aninyasher.util.RandomizedIterator

/**
 * Companion object for TempMailSo.
 */
object TempMailSo {

  /**
   * Default lifespan of the email address in seconds.
   */
  private val defaultLifespan = 300

  /**
   * Polling interval in milliseconds.
   */
  private val pollingInterval = 10000

  /**
   * Create TempMailSo instance.
   *
   * @param tempMailSoKey String
   * @param rapidApiKey String
   * @return TempMailSo
   */
  def apply(tempMailSoKey: String, rapidApiKey: String): TempMailSo = {
    new TempMailSo(tempMailSoKey, rapidApiKey)
  }
}

/**
 * Temporary email service using TempMailSo API.
 *
 * @param client TempMailSoClient
 */
class TempMailSo(private val client: TempMailSoClient) extends TemporaryInbox with Logging {

  /**
   * Email address ID of the inbox.
   */
  private[this] var mailId: Option[String] = None

  /**
   * Create instance of TempMailSo by string keys.
   *
   * @param tempMailSoKey String
   * @param rapidApiKey String
   */
  def this(tempMailSoKey: String, rapidApiKey: String) = {
    this(new TempMailSoClient(rapidApiKey, tempMailSoKey))
  }

  /**
   * Create a new inbox and return it's address.
   *
   * @param preferredLogin String
   * @return String
   */
  override def create(preferredLogin: String): String = {
    val domainsIterator = new RandomizedIterator[String] {
      override protected def load(): Array[String] = {
        ujson.read(client.listDomains())("data").arr.map(_("domain").str).toArray
      }
    }

    val domain = domainsIterator.next()
    val emailRes = ujson.read(client.createInbox(preferredLogin, domain, TempMailSo.defaultLifespan))
    this.mailId = Some(emailRes("data")("id").str)

    s"$preferredLogin@$domain"
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
    val mailboxId = this.mailId.getOrElse(throw new IllegalStateException("Inbox not created"))

    val emailsJson = ujson.read(client.listEmails(mailboxId))("data")
    val emails = if (emailsJson.arrOpt.isDefined) {
      emailsJson.arr.toArray
    } else {
      logger.debug("No new emails so far...")
      Array.empty[ujson.Value]
    }

    val results = emails.map { email =>
      val from = email("from").str
      val subject = email("subject").str
      val body = ujson.read(client.getEmail(mailboxId, email("id").str))("data")("textContent").str

      client.deleteEmail(mailboxId, email("id").str)
      logger.debug(s"Received a new email from $from")
      fn(Email(subject, from, body))
    }

    val firstRes = results.find(_.isDefined)
    if (firstRes.isDefined) {
      client.deleteInbox(mailboxId)
      this.mailId = None
      firstRes.get.get
    } else {
      Thread.sleep(TempMailSo.pollingInterval)
      onNewMail(fn)
    }
  }
}
