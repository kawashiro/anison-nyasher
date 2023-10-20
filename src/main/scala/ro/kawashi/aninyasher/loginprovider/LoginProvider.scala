package ro.kawashi.aninyasher.loginprovider

trait LoginProvider extends Iterator[(String, String)] {

  def +=(login: String, password: String): Unit
}
