package ro.kawashi.aninyasher.captcha

trait CaptchaSolver {
  def solve(url: String, key: String): String
}
