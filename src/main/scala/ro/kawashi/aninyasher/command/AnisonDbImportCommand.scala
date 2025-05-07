package ro.kawashi.aninyasher.command

import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.OptParser.Config
import ro.kawashi.aninyasher.remoteservice.anison.{AnisonDatabase, SessionManager}

/**
 * Imports AnisonDB anime info from the site pages.
 */
class AnisonDbImportCommand extends Command with Logging {

  /**
   * Run the related actions.
   *
   * @param config Config
   */
  override def run(config: Config): Unit = {
    logger.info("Importing AnisonDB anime data...")

    val session = SessionManager(
      config.proxy, config.loginsFile, config.antiCaptchaKey, config.tempMailSoKey, config.rapidApiKey
    )
    val database = AnisonDatabase(config.homeDir)

    session.doAnonymously(withProxy = true)(database.importDatabase)

    logger.info("All done!")
  }
}
