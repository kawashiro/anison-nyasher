package ro.kawashi.aninyasher.remoteservice

import java.net.Proxy

import ro.kawashi.aninyasher.browser.Browser
import ro.kawashi.aninyasher.browser.features.AcceptAny

object IPChecker {
  private val checkerUrl = "http://ip-api.com/json"
  def apply(proxy: Proxy): IPChecker = {
    val browser = Browser(proxy = proxy).applyFeature(new AcceptAny)
    new IPChecker(browser)
  }
}

class IPChecker(override protected val browser: Browser) extends RemoteService(browser) {

  case class IPData(country: String, ip: String)
  def getIPInfo: IPData = {
    val ipData = browser.getJson(IPChecker.checkerUrl)

    IPData(ipData("country").str, ipData("query").str)
  }
}
