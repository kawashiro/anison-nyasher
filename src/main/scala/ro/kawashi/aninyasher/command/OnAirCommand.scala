package ro.kawashi.aninyasher.command

import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.OptParser.Config
import ro.kawashi.aninyasher.proxy.TorProxyProvider
import ro.kawashi.aninyasher.remoteservice.{Anison, IPChecker}
import ro.kawashi.aninyasher.tor.PosixTorProcess
import ro.kawashi.aninyasher.useragent.BuiltInUserAgentList

class OnAirCommand extends Command with Logging {

  override def run(config: Config): Unit = {
    val tor = new PosixTorProcess(config.tor)
    tor.start()

    val proxyProvider = TorProxyProvider(tor)
    val userAgentList = BuiltInUserAgentList()

    (1 to 30).foreach(_ => {
      val userAgent = userAgentList.next()
      val proxy = proxyProvider.next()

      val ipInfo = IPChecker(proxy).getIPInfo
      val songInfo = Anison(userAgent, proxy).getCurrentlyOnAir

      logger.info(s"Requested from ${ipInfo.ip} (${ipInfo.country})")
      logger.info(s"Currently on air: ${songInfo.title} (from ${songInfo.anime})")

      Thread.sleep(10000)
    })
  }
}
