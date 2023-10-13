package ro.kawashi.aninyasher.useragent

import scala.io.Source

object BuiltInUserAgentList {

  private val resourceName = "user-agents.txt"
  def apply(): BuiltInUserAgentList = new BuiltInUserAgentList()
}

class BuiltInUserAgentList extends UserAgentList {
  override protected def load(): Array[String] = {
    Source.fromResource(BuiltInUserAgentList.resourceName).getLines().toArray
  }
}
