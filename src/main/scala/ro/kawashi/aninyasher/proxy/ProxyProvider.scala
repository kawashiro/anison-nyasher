package ro.kawashi.aninyasher.proxy

import java.net.Proxy

import ro.kawashi.aninyasher.util.InfiniteIterator

/**
 * Proxy list with random proxies interface.
 */
trait ProxyProvider extends InfiniteIterator[Proxy]
