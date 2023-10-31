package ro.kawashi.aninyasher.util

import scala.util.Random

/**
 * Simple random password generator
 */
object PasswordGenerator {

  private val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
  private val defaultLength = 8

  /**
   * Generate a password of the fixed length
   *
   * @param length Int
   * @return String
   */
  def generate(length: Int = defaultLength): String = {
    (0 until length).foldLeft("")((acc, _) => acc + chars(Random.nextInt(chars.length)).toString)
  }
}
