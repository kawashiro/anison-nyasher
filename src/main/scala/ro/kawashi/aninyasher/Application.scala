package ro.kawashi.aninyasher

import scopt.OParser

import ro.kawashi.aninyasher.OptParser.Config

object Application {
  def main(args: Array[String]): Unit = {
    val optParser = OptParser.get()

    OParser.parse(optParser, args, Config()) match {
      case Some(config) =>
        if (config.command == null) {
          println(OParser.usage(optParser))
        } else {
          config.command.run(config)
        }
      case _ => println(OParser.usage(optParser))
    }
  }
}
