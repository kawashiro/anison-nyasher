package ro.kawashi.aninyasher.logintransformer.strategy

import scala.util.Random

class NumberAddStrategy extends Strategy {

  private val random = new Random()

  override def canTransform(login: String): Boolean = true

  override def transform(login: String): String = {
    login + random.nextInt(100).toString
  }
}
