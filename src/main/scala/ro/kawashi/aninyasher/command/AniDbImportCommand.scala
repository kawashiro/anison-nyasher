package ro.kawashi.aninyasher.command

import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.OptParser.Config
import ro.kawashi.aninyasher.remoteservice.AniDb

/**
 * Imports AniDB dump file into database.
 */
class AniDbImportCommand extends Command with Logging {

  /**
   * Run the related actions.
   *
   * @param config Config
   */
  override def run(config: Config): Unit = {
    logger.info("Importing AniDB dump file...")
    val file = AniDb().getOrDownloadDumpFile(config.homeDir)
    logger.info(s"Downloaded to $file")
  }
}
