package ro.kawashi.aninyasher.proxy

import java.net.{InetSocketAddress, Proxy}

import ro.kawashi.aninyasher.tor.TorProcess

object TorProxyProvider {
  private val torProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9050))

  def apply(torProcess: TorProcess): TorProxyProvider = new TorProxyProvider(torProcess)
}

class TorProxyProvider(torProcess: TorProcess) extends ProxyProvider {
  override def next(): Proxy = {
    torProcess.changeExitNode()
    Thread.sleep(500)
    TorProxyProvider.torProxy
  }
}
