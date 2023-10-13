package ro.kawashi.aninyasher.util

import scala.util.Random

trait RandomizedIterator[T] {
  private lazy val values: Array[T] = load()
  private val random = new Random()

  def next(): T = {
    values(random.nextInt(values.length))
  }

  protected def load(): Array[T]
}
