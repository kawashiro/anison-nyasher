package ro.kawashi.aninyasher

import scopt.OParser

import ro.kawashi.aninyasher.command.{Command, OnAirCommand}

object OptParser {
  case class Config(
    command: Command = null,

    // On-air command params
    attribute: String = ""
  )

  def get(): OParser[Unit, Config] = {
    val builder = OParser.builder[Config]
    import builder._

    OParser.sequence(
      programName(ro.kawashi.aninyasher.name),
      head(ro.kawashi.aninyasher.name, ro.kawashi.aninyasher.version),

      // On-air command
      cmd("onair")
        .required()
        .action((_, c) => c.copy(command = new OnAirCommand))
        .children(
          opt[String]('a', "attribute")
            .action((arg, c) => c.copy(attribute = arg))
            .required()
            .text("command argument")
        ),
    )
  }
}
