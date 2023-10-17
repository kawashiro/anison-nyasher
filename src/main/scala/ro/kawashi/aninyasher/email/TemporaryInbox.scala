package ro.kawashi.aninyasher.email

trait TemporaryInbox {

  def create(): String

  def onNewMail(fn: Email => Option[String]): String
}
