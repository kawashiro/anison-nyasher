package ro.kawashi.aninyasher.logintransformer.strategy

trait Strategy {

  def canTransform(login: String): Boolean

  def transform(login: String): String
}
