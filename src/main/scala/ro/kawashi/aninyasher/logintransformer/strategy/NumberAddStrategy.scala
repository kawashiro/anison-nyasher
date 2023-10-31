package ro.kawashi.aninyasher.logintransformer.strategy

import scala.util.Random

/**
 * Adds a random number to the end of the login.
 */
class NumberAddStrategy extends Strategy {

  private val random = new Random()
  private val maxLoginNum = 100

  /**
   * Check if the login can be transformed.
   *
   * @param login String
   * @return Boolean
   */
  override def canTransform(login: String): Boolean = true

  /**
   * Transform the login.
   *
   * @param login String
   * @return String
   */
  override def transform(login: String): String = {
    login + random.nextInt(maxLoginNum).toString
  }
}
