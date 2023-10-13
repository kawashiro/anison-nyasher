package ro.kawashi.aninyasher.command

import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.OptParser.Config
import ro.kawashi.aninyasher.remoteservice.Anison
import ro.kawashi.aninyasher.useragent.BuiltInUserAgentList

class OnAirCommand extends Command with Logging {

  override def run(config: Config): Unit = {
    val songInfo = Anison(BuiltInUserAgentList().next()).getCurrentlyOnAir
    logger.info(s"Currently on air: ${songInfo.title} (from ${songInfo.anime})")
  }
}
