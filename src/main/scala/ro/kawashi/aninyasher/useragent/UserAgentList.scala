package ro.kawashi.aninyasher.useragent

import ro.kawashi.aninyasher.util.{InfiniteIterator, RandomizedIterator}

trait UserAgentList extends InfiniteIterator[String] with RandomizedIterator[String]
