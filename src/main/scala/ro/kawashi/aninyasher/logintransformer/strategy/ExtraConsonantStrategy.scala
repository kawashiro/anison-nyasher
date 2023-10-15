package ro.kawashi.aninyasher.logintransformer.strategy

import ro.kawashi.aninyasher.logintransformer.strategy.ExtraConsonantStrategy.consonants

import scala.util.Random

object ExtraConsonantStrategy {
  private val consonants = "bcdfghjklmpqrstvwxz"
}

class ExtraConsonantStrategy extends Strategy {

  private val random = new Random()
  override def canTransform(login: String): Boolean = {
    s"(?i)[$consonants]+".r.findFirstMatchIn(login).isDefined
  }

  override def transform(login: String): String = {
    var transformed = false
    val prefix = s"(?i)^([^$consonants]+)".r.findPrefixMatchOf(login).getOrElse("")
    val res = prefix + s"(?i)([$consonants]+[^$consonants]*)".r.findAllMatchIn(login).map { m =>
      if (!transformed && random.nextBoolean()) {
        val consonant = consonants(random.nextInt(consonants.length))
        transformed = true
        m.group(0) + consonant
      } else {
        m.group(0)
      }
    }.mkString

    if (transformed) res else res + consonants(random.nextInt(consonants.length))
  }
}
