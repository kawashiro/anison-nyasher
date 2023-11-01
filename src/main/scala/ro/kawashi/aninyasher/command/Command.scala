package ro.kawashi.aninyasher.command

import java.io.File

import ro.kawashi.aninyasher.OptParser.Config

/**
 * Command pattern impl, one CLI command interface.
 */
trait Command {

  /**
   * Initialize command environment
   *
   * @param config Config
   * @return Command
   */
  def init(config: Config): Command = {
    new File(config.homeDir).mkdirs()
    this
  }

  /**
   * Run the related actions.
   *
   * @param config Config
   */
  def run(config: Config): Unit
}
