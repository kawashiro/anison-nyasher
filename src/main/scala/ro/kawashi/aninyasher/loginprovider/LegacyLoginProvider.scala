package ro.kawashi.aninyasher.loginprovider

import java.io.{File, FileWriter}

import scala.io.Source
import scala.util.{Failure, Random, Success, Try}

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
    Try(innerIterator.next()) match {
      case Success(value) => value
      case Failure(exception) => throw new RuntimeException(s"No more alive logins at $filePath", exception)
    }
  }

  override def +=(login: String, password: String): Unit = {
    val file = new File(filePath)
    if (!file.exists()) {
      file.createNewFile()
    }

    val writer = new FileWriter(file, true)
    try{
      writer.append(s"$login\t$password\n")
      writer.flush()
    } finally {
      writer.close()
    }
  }

  private def load(): Iterator[(String, String)] = {
    val fileExists = new File(filePath).exists()
    if (!fileExists) {
      return Iterator.empty
    }

    val source = Source.fromFile(filePath)
    try {
      Random.shuffle(source.getLines().map { line =>
        val lineParts = line.split("\t")
        val username = lineParts(0)
        val password = if (lineParts.length == 2) lineParts(1) else LegacyLoginProvider.password
        (username, password)
      }.toList).toIterator
    } finally {
      source.close()
    }
  }
}
