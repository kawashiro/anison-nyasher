package ro.kawashi.aninyasher.proxy

import java.net.{InetSocketAddress, Proxy}

import scala.annotation.tailrec
import scala.util.{Success, Try}

import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.remoteservice.IPChecker
import ro.kawashi.aninyasher.tor.TorProcess

object TorProxyProvider {
  private val torProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050))

  def apply(torProcess: => TorProcess): TorProxyProvider = {
    new TorProxyProvider(torProcess, IPChecker(TorProxyProvider.torProxy))
  }
}

class TorProxyProvider(torProcess: => TorProcess, ipChecker: IPChecker) extends ProxyProvider with Logging {

  private lazy val torProcessInstance = torProcess

  override def next(): Proxy = {
    val oldIp = Try(ipChecker.getIPInfo.ip) match {
      case Success(ip) => ip
      case _ => "not started"
    }
    torProcessInstance.changeExitNode()

    @tailrec
    def awaitLoop(): Unit = {
      val newIpInfo = ipChecker.getIPInfo
      if (newIpInfo.ip != oldIp) {
        logger.info(s"Using new IP address ${newIpInfo.ip} from ${newIpInfo.country}")
        return
      }
      logger.debug(s"Exit node IP address is still ${newIpInfo.ip}, awaiting...")
      Thread.sleep(1000)
      awaitLoop()
    }
    awaitLoop()

    TorProxyProvider.torProxy
  }
}
