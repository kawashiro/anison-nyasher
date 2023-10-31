package ro.kawashi.aninyasher.captcha

/**
 * Captcha solver interface
 */
trait CaptchaSolver {

  /**
   * Solve a captcha
   *
   * @param url String
   * @param key String
   * @return String
   */
  def solve(url: String, key: String): String
}
