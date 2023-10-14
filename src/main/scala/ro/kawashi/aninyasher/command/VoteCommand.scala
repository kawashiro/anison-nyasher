package ro.kawashi.aninyasher.command

import scala.annotation.tailrec
import org.apache.logging.log4j.scala.Logging
import ro.kawashi.aninyasher.OptParser.Config
import ro.kawashi.aninyasher.loginprovider.LegacyLoginProvider
import ro.kawashi.aninyasher.proxy.TorProxyProvider
import ro.kawashi.aninyasher.remoteservice.{Anison, AnisonException, IPChecker}
import ro.kawashi.aninyasher.tor.PosixTorProcess
import ro.kawashi.aninyasher.useragent.BuiltInUserAgentList

class VoteCommand extends Command with Logging {

  override def run(config: Config): Unit = {
    val tor = new PosixTorProcess(config.tor)
    val proxyProvider = TorProxyProvider(tor)
    val userAgentList = BuiltInUserAgentList()
    val loginProvider = LegacyLoginProvider(config.loginsFile)

    tor.start()

    @tailrec
    def votingLoop(wasOnTop: Boolean = false): Unit = {
      val anonymousBrowser = Anison(userAgentList.next())
      val songStatus = anonymousBrowser.getSongStatus(config.songId)

      val votesToDo = songStatus.topSongVotes - songStatus.votes + 1
      val isInQueue = songStatus.votes > 0
      if (!isInQueue && wasOnTop) {
        return
      }

      logger.info(s"$votesToDo votes to be done")
      if (votesToDo > 0) {
        (1 to votesToDo).foreach({iteration =>
          val (login, password) = loginProvider.next()
          try {
            val userAgent = userAgentList.next()
            val proxy = proxyProvider.next()

            val ipInfo = IPChecker(proxy).getIPInfo
            logger.info(s"Voting as $login from ${ipInfo.country} (${ipInfo.ip}) using browser $userAgent")

            val browser = Anison(userAgent, proxy)
            browser.login(login, password)
            browser.vote(config.songId, if (iteration == 1) config.comment else "")
            logger.info("Voted successfully!")

          } catch {
            case e: AnisonException => logger.warn(s"Failed to vote for #${config.songId} as $login: ${e.getMessage}")
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
