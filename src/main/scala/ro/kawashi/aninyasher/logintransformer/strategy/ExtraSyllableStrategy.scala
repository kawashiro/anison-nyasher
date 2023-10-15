package ro.kawashi.aninyasher.logintransformer.strategy

import scala.util.Random

class ExtraSyllableStrategy extends Strategy {

  val random = new Random()

  override def canTransform(login: String): Boolean = {
    s"(?i)[$consonants][$vowels]+$$".r.findFirstMatchIn(login).isDefined
  }

  override def transform(login: String): String = {
    var transformed = false
    val syllable = consonants(random.nextInt(consonants.length)).toString + vowels(random.nextInt(vowels.length)).toString
    val prefix = s"(?i)^([^$consonants]+)".r.findPrefixMatchOf(login).getOrElse("")
    val res = prefix + s"(?i)([$consonants]+[$vowels]+)".r.findAllMatchIn(login).map { m =>
      if (!transformed && random.nextBoolean()) {
        transformed = true
        m.group(0) + syllable
      } else {
        m.group(0)
      }
    }.mkString

    if (transformed) res else res + syllable
  }
}
