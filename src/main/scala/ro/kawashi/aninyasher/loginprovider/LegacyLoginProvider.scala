package ro.kawashi.aninyasher.loginprovider

import scala.io.Source
import scala.util.Random

object LegacyLoginProvider {
  private val password = "qweqwe"

  def apply(filePath: String): LegacyLoginProvider = new LegacyLoginProvider(filePath)
}

class LegacyLoginProvider(filePath: String) extends LoginProvider {
  private lazy val innerIterator = load()

  override def hasNext: Boolean = {
    innerIterator.hasNext
  }

  override def next(): (String, String) = {
    innerIterator.next()
  }

  private def load(): Iterator[(String, String)] = {
    val source = Source.fromFile(filePath)
    try {
      Random.shuffle(source.getLines().map { username =>
        (username, LegacyLoginProvider.password)
      }.toList).toIterator
    } finally {
      source.close()
    }
  }
}
