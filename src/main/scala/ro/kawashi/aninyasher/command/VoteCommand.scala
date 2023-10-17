package ro.kawashi.aninyasher.command

import scala.annotation.tailrec

import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.OptParser.Config
import ro.kawashi.aninyasher.remoteservice.anison.AnisonException
import ro.kawashi.aninyasher.remoteservice.anison.SessionManager

class VoteCommand extends Command with Logging {

  override def run(config: Config): Unit = {
    val session = SessionManager(config.tor, config.loginsFile, config.antiCaptchaKey)

    @tailrec
    def votingLoop(wasOnTop: Boolean = false): Unit = {
      val songStatus = session.doAnonymously(_.getSongStatus(config.songId))

      val votesToDo = songStatus.topSongVotes - songStatus.votes + 1
      val isInQueue = songStatus.votes > 0
      if (!isInQueue && wasOnTop) {
        return
      }

      logger.info(s"$votesToDo votes to be done")
      if (votesToDo > 0) {
        (1 to votesToDo).foreach({iteration =>
          try {
            session.doAuthorized(_.vote(config.songId, if (iteration == 1 && !wasOnTop) config.comment else ""))
            logger.info("Voted successfully!")

          } catch {
            case e: AnisonException => logger.warn(s"Failed to vote for #${config.songId}: ${e.getMessage}")
          }
        })
      } else {
        Thread.sleep(10000)
      }

      votingLoop(true)
    }

    votingLoop()
  }
}
