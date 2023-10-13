package ro.kawashi.aninyasher.proxy

import scala.annotation.tailrec

import java.net.{InetSocketAddress, Proxy}
import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.remoteservice.IPChecker
import ro.kawashi.aninyasher.tor.TorProcess

object TorProxyProvider {
  private val torProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050))

  def apply(torProcess: TorProcess): TorProxyProvider = new TorProxyProvider(torProcess)
}

class TorProxyProvider(torProcess: TorProcess) extends ProxyProvider with Logging {

  private val ipChecker = IPChecker(TorProxyProvider.torProxy)

  override def next(): Proxy = {
    val oldIp = ipChecker.getIPInfo.ip
    torProcess.changeExitNode()

    @tailrec
    def awaitLoop(): Unit = {
      val newIp = ipChecker.getIPInfo.ip
      if (newIp != oldIp) {
        return
      }
      logger.debug(s"Exit node IP address is still $newIp, awaiting...")
      Thread.sleep(1000)
      awaitLoop()
    }
    awaitLoop()

    TorProxyProvider.torProxy
  }
}
