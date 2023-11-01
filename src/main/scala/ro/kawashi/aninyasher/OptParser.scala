package ro.kawashi.aninyasher

import scopt.OParser

import ro.kawashi.aninyasher.command._

/**
 * CLI options parser impl.
 */
object OptParser {

  /**
   * Command config.
   *
   * @param command Command
   * @param tor String
   * @param debug Boolean
   * @param antiCaptchaKey String
   * @param homeDir String
   * @param songId Int
   * @param loginsFileOverride String
   * @param comment String
   */
  case class Config(
    // Global options
    command: Option[Command] = None,
    tor: String = "/usr/lib/anison-nyasher/libexec/tor",
    debug: Boolean = false,
    antiCaptchaKey: String = sys.env.getOrElse("ANTI_CAPTCHA_KEY", ""),
    homeDir: String = sys.env.getOrElse("HOME", ".") + "/.config/anison-nyasher",

    // Vote command params
    songId: Int = -1,
    loginsFileOverride: Option[String] = None,
    comment: String = ""
  ) {

    /**
     * Get logins file path.
     *
     * @return String
     */
    def loginsFile: String = {
      loginsFileOverride.getOrElse(s"$homeDir/logins.txt")
    }
  }

  /**
   * Configure and get options parser
   *
   * @return OParser[Unit, Config]
   */
  def get(): OParser[Unit, Config] = {
    val builder = OParser.builder[Config]
    // scalastyle:off import.grouping
    import builder._
    // scalastyle:on import.grouping

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
      opt[String]('h', "home-dir")
        .action((arg, c) => c.copy(homeDir = arg))
        .text("Nyasher configuration home directory"),

      // On-air command
      cmd("onair")
        .action((_, c) => c.copy(command = Some(new OnAirCommand)))
        .text("Get currently on-air song"),

      // AniDB import command
      cmd("anidb")
        .action((_, c) => c.copy(command = Some(new AniDbImportCommand)))
        .text("Import AniDB dump file into database"),

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
            .action((arg, c) => c.copy(loginsFileOverride = Some(arg)))
            .text("Anison logins file"),
          opt[String]('c', "comment")
            .action((arg, c) => c.copy(comment = arg))
            .text("Anison comment for the vote"),
        ),
    )
  }
}
