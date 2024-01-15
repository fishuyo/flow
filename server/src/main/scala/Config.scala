package flow

import com.typesafe.config.ConfigFactory

object Config {
  val config = ConfigFactory.load()
  def apply(cfg:String) = config.getString(cfg)
}