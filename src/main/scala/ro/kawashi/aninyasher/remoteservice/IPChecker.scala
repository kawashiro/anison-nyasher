package ro.kawashi.aninyasher.remoteservice

import java.net.Proxy

import ro.kawashi.aninyasher.browser.Browser
import ro.kawashi.aninyasher.browser.features.AcceptAny

/**
 * Companion object for IPChecker.
 */
object IPChecker {

  private val checkerUrl = "http://ip-api.com/json"
  private val timeout = 3000

  /**
   * Create IP checker instance.
   * @param proxy Proxy
   * @return IPChecker
   */
  def apply(proxy: Proxy): IPChecker = {
    val browser = Browser(proxy = Some(proxy)).applyFeature(new AcceptAny).setTimeout(Some(timeout))
    new IPChecker(browser)
  }
}

/**
 * External IP checker service.
 * @param browser Browser
 */
class IPChecker(override protected val browser: Browser) extends RemoteService(browser) {

  /**
   * IP address and it's country based on GeoIP database
   *
   * @param country String
   * @param ip String
   */
  case class IPData(country: String, ip: String)

  /**
   * Get IP info
   *
   * @return IPInfo
   */
  def getIPInfo: IPData = {
    val ipData = browser.getJson(IPChecker.checkerUrl)

    IPData(ipData("country").str, ipData("query").str)
  }
}
