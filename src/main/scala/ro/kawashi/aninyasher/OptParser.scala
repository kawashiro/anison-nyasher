package ro.kawashi.aninyasher

import scopt.OParser
import ro.kawashi.aninyasher.command.{Command, OnAirCommand, VoteCommand}

object OptParser {
  case class Config(
    // Global options
    command: Option[Command] = None,
    tor: String = "/usr/bin/tor",
    debug: Boolean = false,
    antiCaptchaKey: String = sys.env.getOrElse("ANTI_CAPTCHA_KEY", ""),

    // Vote command params
    songId: Int = -1,
    loginsFile: String = sys.env.getOrElse("HOME", ".") + "/.anison-logins.txt",
    comment: String = ""
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
      opt[Boolean]('d', "debug")
        .action((arg, c) => c.copy(debug = arg))
        .text("print error backtraces"),
      opt[String]('a', "anti-captcha-key")
        .action((arg, c) => c.copy(antiCaptchaKey = arg))
        .text("anti-captcha.com API key"),

      // On-air command
      cmd("onair")
        .action((_, c) => c.copy(command = Some(new OnAirCommand)))
        .text("Get currently on-air song"),

      // Vote command
      cmd("vote")
        .action((_, c) => c.copy(command = Some(new VoteCommand)))
        .text("Vote for a song")
        .children(
          opt[Int]('s', "song")
            .action((arg, c) => c.copy(songId = arg))
            .required()
            .text("Anison song ID"),
          opt[String]('l', "logins")
            .action((arg, c) => c.copy(loginsFile = arg))
            .text("Anison logins file"),
          opt[String]('c', "comment")
            .action((arg, c) => c.copy(comment = arg))
            .text("Anison comment for the vote"),
        ),
    )
  }
}
