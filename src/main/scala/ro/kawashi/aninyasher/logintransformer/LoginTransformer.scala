package ro.kawashi.aninyasher.logintransformer

import scala.util.Random

import ro.kawashi.aninyasher.logintransformer.strategy.Strategy

class LoginTransformer(private val fallbackStrategy: Strategy, private val strategies: List[Strategy] = Nil) {

  def addStrategy(strategy: Strategy): LoginTransformer = {
    new LoginTransformer(fallbackStrategy, strategy :: strategies)
  }

  def transform(login: String): String = {
    Random.shuffle(strategies).find(_.canTransform(login)) match {
      case Some(strategy) => strategy.transform(login)
      case None => fallbackStrategy.transform(login)
    }
  }
}
