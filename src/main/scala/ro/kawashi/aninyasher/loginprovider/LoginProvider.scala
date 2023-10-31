package ro.kawashi.aninyasher.loginprovider

/**
 * Saved logins list interface.
 */
trait LoginProvider extends Iterator[(String, String)] {

  /**
   * Add a new login.
   *
   * @param login String
   * @param password String
   */
  def +=(login: String, password: String): Unit
}
