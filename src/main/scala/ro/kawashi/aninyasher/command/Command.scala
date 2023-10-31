package ro.kawashi.aninyasher.command

import ro.kawashi.aninyasher.OptParser.Config

/**
 * Command pattern impl, one CLI command interface.
 */
trait Command {

  /**
   * Run the related actions.
   *
   * @param config Config
   */
  def run(config: Config): Unit
}
