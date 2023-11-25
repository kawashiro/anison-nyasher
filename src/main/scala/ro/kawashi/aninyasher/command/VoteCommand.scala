package ro.kawashi.aninyasher.command

import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.OptParser.Config
import ro.kawashi.aninyasher.remoteservice.anison.{SessionManager, VotingHelper}

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

    val votingHelper = VotingHelper(SessionManager(config.tor, config.loginsFile, config.antiCaptchaKey))
    votingHelper.vote(config.songId, config.comment)
  }
}
