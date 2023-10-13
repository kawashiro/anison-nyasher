package ro.kawashi.aninyasher

import scopt.OParser

import ro.kawashi.aninyasher.command.{Command, OnAirCommand}

object OptParser {
  case class Config(
    // Global options
    command: Option[Command] = None,
    tor: String = "/usr/bin/tor",

    // On-air command params
    attribute: String = ""
  )

  def get(): OParser[Unit, Config] = {
    val builder = OParser.builder[Config]
    import builder._

    OParser.sequence(
      programName(ro.kawashi.aninyasher.name),
      head(ro.kawashi.aninyasher.name, ro.kawashi.aninyasher.version),

      opt[String]('t', "tor")
        .action((arg, c) => c.copy(tor = arg))
        .text("path to tor executable"),

      // On-air command
      cmd("onair")
        .required()
        .action((_, c) => c.copy(command = Some(new OnAirCommand)))
        .children(
          opt[String]('a', "attribute")
            .action((arg, c) => c.copy(attribute = arg))
            .required()
            .text("command argument")
        ),
    )
  }
}
