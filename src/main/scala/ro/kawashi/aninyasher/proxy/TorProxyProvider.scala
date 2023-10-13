package ro.kawashi.aninyasher.proxy

import java.net.{InetSocketAddress, Proxy}

import ro.kawashi.aninyasher.tor.TorProcess

object TorProxyProvider {
  private val torProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050))
}

class TorProxyProvider(torProcess: TorProcess) extends ProxyProvider {
  override def hasNext: Boolean = true

  override def next(): Proxy = {
    torProcess.changeExitNode()
    TorProxyProvider.torProxy
  }
}
