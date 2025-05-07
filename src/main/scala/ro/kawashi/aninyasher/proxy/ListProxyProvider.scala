package ro.kawashi.aninyasher.proxy

import java.io.IOException
import java.net.{InetSocketAddress, Proxy}

import scala.annotation.tailrec
import scala.io.Source
import scala.util.Random

import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.remoteservice.IPChecker

/**
 * Companion object for ListProxyProvider
 */
object ListProxyProvider {

  /**
   * Create a new instance of ListProxyProvider
   *
   * @param proxyFile String
   * @return ListProxyProvider
   */
  def apply(proxyFile: String): ListProxyProvider = {
    new ListProxyProvider(proxyFile)
  }
}

/**
 * Proxy list from a simple text file.
 *
 * @param proxyFile String
 */
class ListProxyProvider(proxyFile: String) extends ProxyProvider with Logging {

  /**
   * Values iterator
   */
  private lazy val values = load()

  /**
   * Get the next proxy instance
   *
   *
   * @return Option[Proxy]
   */
  @tailrec
  override final def next(): Option[Proxy] = {
    val proxyOpt = values.next()

    try {
      val ipInfo = IPChecker(proxyOpt.get).getIPInfo
      logger.info(s"Using new IP address ${ipInfo.ip} from ${ipInfo.country}")
      proxyOpt
    } catch {
      case exc: IOException =>
        logger.warn(s"Proxy ${proxyOpt.get} is dead: ${exc.getMessage}")
        next()
    }
  }

  /**
   * Check if there is a new free proxy
   *
   * @return Boolean
   */
  override def hasNext: Boolean = values.hasNext

  /**
   * Load an array of proxies from the file
   *
   * @return Iterator[Proxy]
   */
  private def load(): Iterator[Option[Proxy]] = {
    val source = Source.fromFile(proxyFile)
    try {
      Random.shuffle(source.getLines().map { line =>
        val lineParts = line.split(":")
        val host = lineParts(0)
        val port = lineParts(1).toInt
        Some(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port)))
      })
    } finally {
      source.close()
    }
  }
}
