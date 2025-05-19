package ro.kawashi.aninyasher

import scala.util.{Failure, Success, Try}

import org.apache.logging.log4j.scala.Logging
import scopt.OParser

import ro.kawashi.aninyasher.net.SSLSocketFactoryProvider
import ro.kawashi.aninyasher.OptParser.Config

/**
 * AniNyasher application
 */
object Application extends Logging {

  /**
   * Main routine
   *
   * @param args Array[String]
   */
  def main(args: Array[String]): Unit = {
    val optParser = OptParser.get()

    OParser.parse(optParser, args, Config()) match {
      case Some(config) =>
        if (config.command.isEmpty) {
          // scalastyle:off regex
          println(OParser.usage(optParser))
          // scalastyle:on regex
        } else {
          logger.info(s"Greeting to Anison from Aninson Nyasher v.${ro.kawashi.aninyasher.version}")
          SSLSocketFactoryProvider.register()
          Try(config.command.get.init(config).run(config)) match {
            case Failure(exception) =>
              logger.error(exception.getMessage)
              if (config.debug) {
                throw exception
              }
            case Success(_) => ()
          }
        }
      // scalastyle:off regex
      case _ => println(OParser.usage(optParser))
      // scalastyle:on regex
    }
  }
}
