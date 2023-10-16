package ro.kawashi.aninyasher.util

import scala.util.Random

object PasswordGenerator {
  private val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')

  def generate(length: Int = 8): String = {
    (0 until length).foldLeft("")((acc, _) => acc + chars(Random.nextInt(chars.length)).toString)
  }
}
