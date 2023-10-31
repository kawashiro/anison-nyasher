package ro.kawashi.aninyasher.captcha

import scala.annotation.tailrec

import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.browser.Browser
import ro.kawashi.aninyasher.browser.features.AcceptAny
import ro.kawashi.aninyasher.remoteservice.RemoteService

/**
 * Exception on inability to resolve captcha.
 *
 * @param message String
 */
class AntiCaptchaException(message: String) extends RuntimeException(message)

/**
 * A companion object for the AntiCaptcha class.
 */
object AntiCaptcha {

  private val captchaSubmitUrl = "https://api.anti-captcha.com/createTask"
  private val captchaResultUrl = "https://api.anti-captcha.com/getTaskResult"
  private val pollingInterval = 10000

  /**
   * Create a new AntiCaptcha instance.
   *
   * @param clientKey String
   * @return AntiCaptcha
   */
  def apply(clientKey: String): AntiCaptcha = {
    new AntiCaptcha(Browser().applyFeature(new AcceptAny), clientKey)
  }
}

/**
 * A captcha solver using the anti-captcha.com service.
 *
 * @param browser Browser
 * @param clientKey String
 */
class AntiCaptcha(override protected val browser: Browser, clientKey: String)
  extends RemoteService(browser) with CaptchaSolver with Logging {

  /**
   * Solve a captcha.
   *
   * @param url String
   * @param key String
   * @return String
   */
  override def solve(url: String, key: String): String = {
    getTaskResult(submitTask(url, key))
  }

  private def submitTask(url: String, key: String): Int = {
    val task = ujson.Obj(
      "clientKey" -> clientKey,
      "task" -> ujson.Obj(
        "type" -> "RecaptchaV2TaskProxyless",
        "websiteURL" -> url,
        "websiteKey" -> key,
      ),
    )

    logger.debug(s"Submitting captcha task $key for $url")
    request(AntiCaptcha.captchaSubmitUrl, task)("taskId").num.toInt
  }

  @tailrec
  private def getTaskResult(taskId: Int): String = {
    val taskResult = request(
      AntiCaptcha.captchaResultUrl,
      ujson.Obj("clientKey" -> clientKey, "taskId" -> taskId)
    )

    taskResult("status").str match {
      case "processing" =>
        logger.debug(s"Waiting for captcha task $taskId to be solved...")
        Thread.sleep(AntiCaptcha.pollingInterval)
        getTaskResult(taskId)

      case "ready" =>
        val solution = taskResult("solution")("gRecaptchaResponse").str
        logger.debug(s"Captcha task $taskId solution is $solution")
        solution

      case status: String => throw new AntiCaptchaException(s"Invalid captcha status: $status")
    }
  }

  private def request(url: String, payload: ujson.Value): ujson.Value = {
    val response = browser.postJson(url, payload)
    if (response("errorId").num > 0) {
      val errorCode = response("errorCode").str
      val errorText = response("errorDescription").str
      throw new AntiCaptchaException(s"Captcha could not be solved: $errorText ($errorCode)")
    }

    response
  }
}
