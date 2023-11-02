package ro.kawashi.aninyasher.remoteservice.anison

import scala.collection.mutable.ListBuffer
import scala.xml.XML

import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.remoteservice.Anison

/**
 * Companion object for AnisonDatabase.
 */
object AnisonDatabase {

  private val fileName = "anisondb.xml"

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
    val animeList = ListBuffer.empty[Anison.AnimeInfo]
    try {
      anisonService.foreachAnimeInfo { anime =>
        logger.info(s"Importing ${anime.titleEn} / ${anime.titleRu}")
        animeList += anime
      }
    } finally {
      XML.save(
        s"$homeDir/${AnisonDatabase.fileName}",
        <animes>{animeList.map(_.toXML)}</animes>,
        xmlDecl = true,
      )
    }
  }
}
