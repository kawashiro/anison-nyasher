package ro.kawashi.aninyasher.remoteservice.anison

import java.util.Locale

import scala.collection.mutable.ListBuffer
import scala.xml.XML

import com.github.vickumar1981.stringdistance.StringDistance.Levenshtein
import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.remoteservice.Anison

/**
 * Companion object for AnisonDatabase.
 */
object AnisonDatabase {

  /**
   * Songs search result structure.
   *
   * @param anime AnimeInfo
   * @param song AnimeSongInfo
   * @param score Double
   */
  case class SearchResult(anime: Anison.AnimeInfo, song: Anison.AnimeSongInfo, score: Double)

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

  private lazy val content = load()

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

  /**
   * Search songs for the given keywords and/or year.
   *
   * @param keywords Option[String]
   * @param year Option[Int]
   * @return List[Anison.AnimeInfo]
   */
  def search(keywords: Option[String], year: Option[(Int, Int)]): List[AnisonDatabase.SearchResult] = {
    val kwToSet = (kw: String) => kw.toLowerCase(new Locale("ru")).split(" ").filter(_.nonEmpty).toSet

    val keywordsFilter = keywords match {
      case Some(value) =>
        val keywords_ = kwToSet(value)
        (anime: Anison.AnimeInfo) => {
          val animeKeywords = kwToSet(anime.titleEn) ++ kwToSet(anime.titleRu) ++ anime.genres
          anime.songs.map { song =>
            val songKeywords = kwToSet(song.artist + " " + song.title + " " + song.album) ++ animeKeywords
            val score = keywords_.toList
              .map(kw => songKeywords.map(skw => Levenshtein.score(skw, kw)).max).sum / keywords_.size
            AnisonDatabase.SearchResult(anime, song, score)
          }.toIterator
        }
      case None => (anime: Anison.AnimeInfo) => anime.songs.map { song =>
        AnisonDatabase.SearchResult(anime, song, 1.0)
      }.toIterator
    }

    val yearsFilter = year match {
      case Some((from, to)) => (prevResult: AnisonDatabase.SearchResult) => {
        val found = prevResult.anime.years.exists(year => year >= from && year <= to)
        if (found) {
          prevResult.anime.songs.map(AnisonDatabase.SearchResult(prevResult.anime, _, prevResult.score)).toIterator
        } else {
          Iterator.empty
        }
      }
      case None => (prevResult: AnisonDatabase.SearchResult) => List(prevResult).toIterator
    }

    content.flatMap(keywordsFilter).flatMap(yearsFilter).filter(_.score > 0.7).sortBy(-_.score)
  }

  private def load(): List[Anison.AnimeInfo] = {
    val xml = XML.loadFile(s"$homeDir/${AnisonDatabase.fileName}")

    val animeList = (xml \ "anime").map { anime =>
      val titleEn = (anime \ "title_en").text
      val titleRu = (anime \ "title_ru").text
      val years = (anime \ "years" \ "year").map(_.text.toInt).toSet
      val genres = (anime \ "genres" \ "genre").map(_.text).toArray
      val songs = (anime \ "songs" \ "song").map { song =>
        Anison.AnimeSongInfo(
          (song \ "id").text.toInt,
          (song \ "album").text,
          (song \ "artist").text,
          (song \ "title").text,
        )
      }.toArray

      Anison.AnimeInfo(titleEn, titleRu, genres, years, songs)
    }

    animeList.toList
  }
}
