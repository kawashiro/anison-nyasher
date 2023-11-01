package ro.kawashi.aninyasher.remoteservice.anison

import java.io.FileWriter

import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.remoteservice.Anison

/**
 * Companion object for AnisonDatabase.
 */
object AnisonDatabase {

  private val fileName = "anisondb.tsv"

  /**
   * Create a new AnisonDatabase instance.
   *
   * @param homeDir String
   * @return AnisonDatabase
   */
  def apply(homeDir: String): AnisonDatabase = new AnisonDatabase(homeDir)
}

/**
 * Simple text-file-based "database" of Anison songs
 *
 * @param homeDir String
 */
class AnisonDatabase(homeDir: String) extends Logging {

  /**
   * Save the song info to the database.
   *
   * @param anisonService Anison
   */
  def importDatabase(anisonService: Anison): Unit = {
    val writer = new FileWriter(s"$homeDir/${AnisonDatabase.fileName}")
    try {
      anisonService.foreachAnimeInfo { anime =>
        logger.info(s"Importing ${anime.titleEn} / ${anime.titleRu}")
        anime.songs.foreach { song =>
          val songStr = s"${anime.titleEn}\t${anime.titleRu}\t${anime.years.mkString(",")}" +
            s"\t${song.id}\t${song.album}\t${song.artist}\t${song.title}\n"
          writer.write(songStr)
          writer.flush()
        }
      }
    } finally {
      writer.close()
    }
  }
}
