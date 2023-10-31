package ro.kawashi.aninyasher.util

trait InfiniteIterator[+A] extends Iterator[A] {

  /**
   * Yes, we have
   *
   * @return Boolean
   */
  override def hasNext: Boolean = true
}
