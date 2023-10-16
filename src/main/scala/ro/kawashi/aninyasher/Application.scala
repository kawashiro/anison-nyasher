package ro.kawashi.aninyasher

import scala.util.{Failure, Success, Try}

import org.apache.logging.log4j.scala.Logging
import scopt.OParser

import ro.kawashi.aninyasher.OptParser.Config

object Application extends Logging {
  def main(args: Array[String]): Unit = {
    val optParser = OptParser.get()

    OParser.parse(optParser, args, Config()) match {
      case Some(config) =>
        if (config.command.isEmpty) {
          println(OParser.usage(optParser))
        } else {
          logger.info(s"Greeting to Anison from Aninson Nyasher v.${ro.kawashi.aninyasher.version}")
          Try(config.command.get.run(config)) match {
            case Failure(exception) =>
              logger.error(exception.getMessage)
              if (config.debug) {
                throw exception
              }
            case Success(_) => ()
          }
        }
      case _ => println(OParser.usage(optParser))
    }
  }
}
