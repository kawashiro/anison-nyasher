package ro.kawashi.aninyasher.remoteservice

import java.io.File

import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.browser.Browser
import ro.kawashi.aninyasher.browser.features.AcceptAny
import ro.kawashi.aninyasher.useragent.BuiltInUserAgentList

/**
 * Companion object for AniDB.
 */
object AniDb {

  private val dumpFile = "anidb.xml.gz"
  private val dumpFileTtl = 86400 * 1000 // 24 hours
  private val dumpUrl = "https://anidb.net/api/anime-titles.xml.gz"

  /**
   * Create a new AniDB service  instance.
   *
   * @return AniDb
   */
  def apply(): AniDb = {
    new AniDb(
      Browser(BuiltInUserAgentList().next())
        .applyFeature(new AcceptAny)
    )
  }
}

/**
 * AniDB service integration implementation.
 *
 * @param browser Browser
 */
class AniDb(override protected val browser: Browser) extends RemoteService(browser) with Logging {

  /**
   * Return existing or download a new AniDB dump file.
   *
   * @param homeDir String
   * @return String, path to AniDB dump file
   */
  def getOrDownloadDumpFile(homeDir: String): String = {
    val dumpFileObj = new File(s"$homeDir/${AniDb.dumpFile}")

    if (!dumpFileObj.exists() || System.currentTimeMillis() - dumpFileObj.lastModified() > AniDb.dumpFileTtl) {
      logger.info("AniDB dump file not found or is too old, downloading a new one...")
      browser.download(AniDb.dumpUrl, dumpFileObj.getAbsolutePath)

    } else {
      logger.info("AniDB dump file found and is not too old, using it")
    }

    dumpFileObj.getAbsolutePath
  }
}
