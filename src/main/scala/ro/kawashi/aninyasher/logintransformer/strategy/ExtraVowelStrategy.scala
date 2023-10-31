package ro.kawashi.aninyasher.logintransformer.strategy

import scala.util.Random

/**
 * Adds extra vowel to the login.
 */
class ExtraVowelStrategy extends Strategy {

  private val random = new Random()

  /**
   * Check if the login can be transformed.
   *
   * @param login String
   * @return Boolean
   */
  override def canTransform(login: String): Boolean = {
    s"(?i)[$consonants]+".r.findFirstMatchIn(login).isDefined
  }

  /**
   * Transform the login.
   *
   * @param login String
   * @return String
   */
  override def transform(login: String): String = {
    var transformed = false
    val prefix = s"(?i)^([^$consonants]+)".r.findPrefixMatchOf(login).getOrElse("")
    val res = prefix + s"(?i)([$consonants]+[^$consonants]*)".r.findAllMatchIn(login).map { m =>
      if (!transformed && random.nextBoolean()) {
        val vowel = vowels(random.nextInt(vowels.length))
        transformed = true
        m.group(0) + vowel
      } else {
        m.group(0)
      }
    }.mkString

    if (transformed) res else res + vowels(random.nextInt(vowels.length))
  }
}
