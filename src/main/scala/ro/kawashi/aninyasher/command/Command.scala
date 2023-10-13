package ro.kawashi.aninyasher.command

import ro.kawashi.aninyasher.OptParser.Config

trait Command {
  def run(config: Config): Unit
}
