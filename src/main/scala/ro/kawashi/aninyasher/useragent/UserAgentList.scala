package ro.kawashi.aninyasher.useragent

import ro.kawashi.aninyasher.util.{InfiniteIterator, RandomizedIterator}

/**
 * Randomized user agent list
 */
trait UserAgentList extends InfiniteIterator[String] with RandomizedIterator[String]
