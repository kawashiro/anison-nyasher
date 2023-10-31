package ro.kawashi

/**
 * Application metadata
 */
package object aninyasher {

  /**
   * Application pretty name
   */
  val name: String = getClass.getPackage.getImplementationTitle

  /**
   * Application version
   */
  val version: String = getClass.getPackage.getImplementationVersion
}
