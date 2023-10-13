package ro.kawashi.aninyasher.util

trait InfiniteIterator[+A] extends Iterator[A] {
  override def hasNext: Boolean = true
}
