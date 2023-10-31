package ro.kawashi.aninyasher.util

import scala.util.Random

/**
 * Shuffled random iterator
 *
 * @tparam T type of elements
 */
trait RandomizedIterator[T] {
  private lazy val values: Array[T] = load()
  private val random = new Random()

  /**
   * Get the next random element
   *
   * @return T
   */
  def next(): T = {
    values(random.nextInt(values.length))
  }

  /**
   * Load an array of elements
   *
   * @return Array[T]
   */
  protected def load(): Array[T]
}
