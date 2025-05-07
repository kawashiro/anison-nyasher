package ro.kawashi.aninyasher.command

import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.OptParser.Config
import ro.kawashi.aninyasher.remoteservice.anison._

/**
 * Implementation of simple voting by song ID
 *
 * @constructor create a new vote command
 */
class VoteCommand extends Command with Logging {

  /**
   * Run the related actions.
   *
   * @param config the configuration
   */
  override def run(config: Config): Unit = {
    logger.info(s"Voting for song #${config.songId}")

    val votingHelper = VotingHelper(
      SessionManager(config.tor, config.loginsFile, config.antiCaptchaKey, config.tempMailSoKey, config.rapidApiKey)
    )
    try {
      votingHelper.vote(config.songId, config.comment)
    } catch {
      case e: AnisonException =>
        logger.error(s"Failed to vote for song ${config.songId}: ${e.getMessage}")
    }
  }
}
