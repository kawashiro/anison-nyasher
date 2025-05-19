package ro.kawashi.aninyasher.remoteservice.anison

import scala.annotation.tailrec

import org.apache.logging.log4j.scala.Logging

/**
 * Companion object for VotingHelper.
 */
object VotingHelper {

  /**
   * Create a new VotingHelper instance.
   *
   * @param session SessionManager
   * @return VotingHelper
   */
  def apply(session: SessionManager): VotingHelper = new VotingHelper(session)
}

/**
 * Perform song voting actions.
 *
 * @param session SessionManager
 */
class VotingHelper(session: SessionManager) extends Logging {

  private val pollingInterval = 10000

  /**
   * Vote for a song by ID.
   *
   * @param songId Int
   * @param comment String
   */
  def vote(songId: Int, comment: String = ""): Unit = {
    @tailrec
    def votingLoop(wasOnTop: Boolean = false): Unit = {
      val songStatus = session.doAnonymously()(_.getSongStatus(songId))

      val votesToDo = songStatus.topSongVotes - songStatus.votes + 1
      val isInQueue = songStatus.votes > 0
      if (!isInQueue && wasOnTop) {
        return
      }

      logger.info(s"$votesToDo votes to be done")
      val statuses = if (votesToDo > 0) {
        (1 to votesToDo).map({ iteration =>
          try {
            session.doAuthorized(_.vote(songId, if (iteration == 1 && !wasOnTop) comment else ""))
            logger.info("Voted successfully!")
            true

          } catch {
            case e: AnimeNotVotableException => throw e
            case e: SongNotVotableException => throw e
            case e: AnisonException => logger.warn(s"Failed to vote for #$songId: ${e.getMessage}")
              false
          }
        }).toList
      } else {
        Thread.sleep(pollingInterval)
        Nil
      }

      votingLoop((statuses :+ true).reduce(_ && _))
    }

    votingLoop()
  }
}
