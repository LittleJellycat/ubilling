package ru.fintech.school.ubilling

/**
  * Created by Nikolay Poperechnyi on 23.04.18.
  */

import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait HasDbConfig {

  def config: DatabaseConfig[JdbcProfile]

  final lazy val profile: JdbcProfile = config.profile

  def close() = config.db.close()

  def db = config.db
}

/*trait HasDbConfigPath[P <: BasicProfile] extends HasDbConfig[P] {
  protected[this] val path: String

  override lazy val config: DatabaseConfig[P] = DatabaseConfig.forConfig(path)
}
*/


object H2Db extends HasDbConfig {
  val path = "h2"
  override lazy val config = DatabaseConfig.forConfig(path)
}

object PostgresDb extends HasDbConfig {
  val path = "postgres"
  override lazy val config = DatabaseConfig.forConfig(path)
}

trait HasDbConfigProvider extends HasDbConfig {
  override final lazy val config: DatabaseConfig[JdbcProfile] = DbConfigProvider.get
}

trait DbConfigProvider {
  def get: DatabaseConfig[JdbcProfile]
}

object DbConfigProvider extends DbConfigProvider {
  override def get: DatabaseConfig[JdbcProfile] = {
    val config = ConfigFactory.load()
    val selectedConfig = config.getString("mode") match {
      case "dev" => "h2"
      case _ => "postgres"
    }
    DatabaseConfig.forConfig[JdbcProfile](selectedConfig, config)
  }
}


