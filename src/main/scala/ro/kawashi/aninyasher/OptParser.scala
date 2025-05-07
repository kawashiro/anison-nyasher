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
   * @param proxy String
   * @param debug Boolean
   * @param antiCaptchaKey String
   * @param tempMailSoKey String
   * @param rapidApiKey String
   * @param homeDir String
   * @param songId Int
   * @param loginsFileOverride String
   * @param comment String
   * @param year Int from -> Int to
   * @param keywords String
   * @param strictMatch Boolean
   * @param preview Boolean
   */
  case class Config(
    // Global options
    command: Option[Command] = None,
    proxy: String = "tor:/usr/lib/anison-nyasher/libexec/tor",
    debug: Boolean = false,
    antiCaptchaKey: String = sys.env.getOrElse("ANTI_CAPTCHA_KEY", ""),
    tempMailSoKey: String = sys.env.getOrElse("TEMPMAIL_SO_KEY", ""),
    rapidApiKey: String = sys.env.getOrElse("RAPID_API_KEY", ""),
    homeDir: String = sys.env.getOrElse("HOME", ".") + "/.config/anison-nyasher",

    // Vote command params
    songId: Int = -1,
    loginsFileOverride: Option[String] = None,
    comment: String = "",

    // Playlist command params
    year: Option[(Int, Int)] = None,
    keywords: Option[String] = None,
    strictMatch: Boolean = false,
    preview: Boolean = false,
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

      opt[String]('p', "proxy")
        .action((arg, c) => c.copy(proxy = arg))
        .text("path to a file; path to tor:executable; pass `disable` to use a direct connection"),
      opt[Boolean]('d', "debug")
        .action((arg, c) => c.copy(debug = arg))
        .text("print error backtraces"),
      opt[String]('a', "anti-captcha-key")
        .action((arg, c) => c.copy(antiCaptchaKey = arg))
        .text("anti-captcha.com API key"),
      opt[String]('m', "temp-mail-so-key")
        .action((arg, c) => c.copy(tempMailSoKey = arg))
        .text("tempmail.so API key"),
      opt[String]('r', "rapidapi-com-key")
        .action((arg, c) => c.copy(rapidApiKey = arg))
        .text("rapidapi.com API key"),
      opt[String]('h', "home-dir")
        .action((arg, c) => c.copy(homeDir = arg))
        .text("Nyasher configuration home directory"),
      opt[String]('l', "logins")
        .action((arg, c) => c.copy(loginsFileOverride = Some(arg)))
        .text("Anison logins file"),

      // On-air command
      cmd("onair")
        .action((_, c) => c.copy(command = Some(new OnAirCommand)))
        .text("Get currently on-air song"),

      // AniDB import command
      cmd("anidb")
        .action((_, c) => c.copy(command = Some(new AniDbImportCommand)))
        .text("Import AniDB dump file into database"),

      // Anison songs DB import command
      cmd("anisondb")
        .action((_, c) => c.copy(command = Some(new AnisonDbImportCommand)))
        .text("Import Anison songs DB into database"),

      // Vote command
      cmd("vote")
        .action((_, c) => c.copy(command = Some(new VoteCommand)))
        .text("Vote for a song")
        .children(
          opt[Int]('s', "song")
            .action((arg, c) => c.copy(songId = arg))
            .required()
            .text("Anison song ID"),
          opt[String]('c', "comment")
            .action((arg, c) => c.copy(comment = arg))
            .text("Anison comment for the vote"),
        ),

      // Playlist command
      cmd("playlist")
        .action((_, c) => c.copy(command = Some(new PlaylistCommand)))
        .text("Create and submit a playlist")
        .children(
          opt[String]('y', "year")
            .action((arg, c) => c.copy(year = arg.split("-").map(_.toInt) match {
              case Array(from, to) => Some((from, to))
              case _ => None
            }))
            .text("year of the anime title"),
          opt[String]('k', "keywords")
            .action((arg, c) => c.copy(keywords = Some(arg)))
            .text("keywords to search for"),
          opt[Boolean]('r', "strict")
            .action((arg, c) => c.copy(strictMatch = arg))
            .text("strict match for the song keywords"),
          opt[Boolean]('v', "preview")
            .action((arg, c) => c.copy(preview = arg))
            .text("preview the playlist instead of voting"),
        ),
    )
  }
}
