package ro.kawashi.aninyasher.command

import scala.util.Random

import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.OptParser.Config
import ro.kawashi.aninyasher.loginprovider.LegacyLoginProvider
import ro.kawashi.aninyasher.remoteservice.anison.{AnisonDatabase, SessionManager, VotingHelper}

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
    val playlist = database.search(config.keywords, config.year, config.strictMatch)

    if (config.preview) {
      playlist.foreach { res =>
        logger.info(
          s"${res.song.id} :: ${res.song.artist} - ${res.song.title} " +
            s"from ${res.anime.titleEn} (${res.anime.titleRu}); score: ${res.score}"
        )
      }
    } else {
      logger.info("Playing the playlist ^-^")
      Random.shuffle(playlist).foldLeft(SessionManager(config.tor, config.loginsFile, config.antiCaptchaKey))(
        (session, song) => {
          val votingHelper = VotingHelper(session)
          logger.info(s"Voting for song ${song.song.artist} - ${song.song.title} " +
            s"from ${song.anime.titleEn} (${song.anime.titleRu})")
          votingHelper.vote(song.song.id)
          session.withNewLoginProvider(LegacyLoginProvider(config.loginsFile))
        }
      )
    }
  }
}
