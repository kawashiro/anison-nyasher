package ro.kawashi.aninyasher.email

/**
 * Temporary email service inbox interface.
 */
trait TemporaryInbox {

  /**
   * Create a new inbox and return it's address.
   *
   * @return String
   */
  def create(): String

  /**
   * Process each new email.
   *
   * @param fn Callback function (returns None if email is not expected by it's content)
   * @tparam T Callback return type
   * @return T
   */
  def onNewMail[T](fn: Email => Option[T]): T
}
