package ro.kawashi.aninyasher.useragent

import scala.io.Source

/**
 * Companion object for BuiltInUserAgentList
 */
object BuiltInUserAgentList {

  private val resourceName = "user-agents.txt"

  /**
   * Get instance
   *
   * @return BuiltInUserAgentList
   */
  def apply(): BuiltInUserAgentList = new BuiltInUserAgentList()
}

/**
 * Compiled list of user agents
 */
class BuiltInUserAgentList extends UserAgentList {

  /**
   * Load the list
   *
   * @return Array[String]
   */
  override protected def load(): Array[String] = {
    Source.fromResource(BuiltInUserAgentList.resourceName).getLines().toArray
  }
}
