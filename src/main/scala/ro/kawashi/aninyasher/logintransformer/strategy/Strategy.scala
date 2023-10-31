package ro.kawashi.aninyasher.logintransformer.strategy

/**
 * Login transformer strategy interface.
 */
trait Strategy {

  /**
   * Check if the login can be transformed.
   *
   * @param login String
   * @return Boolean
   */
  def canTransform(login: String): Boolean

  /**
   * Transform the login.
   *
   * @param login String
   * @return String
   */
  def transform(login: String): String
}
