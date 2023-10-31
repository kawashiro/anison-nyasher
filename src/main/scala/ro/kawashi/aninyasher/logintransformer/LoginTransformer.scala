package ro.kawashi.aninyasher.logintransformer

import scala.util.Random

import ro.kawashi.aninyasher.logintransformer.strategy.Strategy

/**
 * Login transformer companion object.
 */
object LoginTransformer {

  /**
   * Create a new login transformer.
   *
   * @return LoginTransformer
   */
  def apply(): LoginTransformer = new LoginTransformer()
}

/**
 * Login transformer: selects a strategy to transform a login.
 *
 * @param strategies List[Strategy]
 */
class LoginTransformer(strategies: List[Strategy] = Nil) {

  /**
   * Add a strategy to the transformer.
   *
   * @param strategy Strategy
   * @return LoginTransformer
   */
  def addStrategy(strategy: Strategy): LoginTransformer = {
    new LoginTransformer(strategy :: strategies)
  }

  /**
   * Transform the login with random strategy.
   *
   * @param login String
   * @return String
   */
  def transform(login: String): String = {
    Random.shuffle(strategies).find(_.canTransform(login)) match {
      case Some(strategy) => strategy.transform(login)
      case None => throw new IllegalArgumentException(s"Cannot transform $login: no strategy found for such string")
    }
  }
}
