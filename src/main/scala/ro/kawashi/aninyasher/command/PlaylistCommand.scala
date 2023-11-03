package ro.kawashi.aninyasher.command

import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.OptParser.Config
import ro.kawashi.aninyasher.remoteservice.anison.AnisonDatabase

/**
 * Creates and submits a playlist of songs.
 */
class PlaylistCommand extends Command with Logging {

  /**
   * Run the related actions.
   *
   * @param config Config
   */
  override def run(config: Config): Unit = {
    logger.info("Searching for the songs...")

    val database = AnisonDatabase(config.homeDir)
    val playlist = database.search(config.keywords, config.year)
    playlist.foreach { res =>
      logger.info(
        s"${res.song.id} :: ${res.song.artist} - ${res.song.title} " +
          s"from ${res.anime.titleEn} (${res.anime.titleRu}); score: ${res.score}"
      )
    }
  }
}
