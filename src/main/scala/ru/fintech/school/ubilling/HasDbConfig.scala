package ru.fintech.school.ubilling

import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait HasDbConfig {

  def config: DatabaseConfig[JdbcProfile]

  final lazy val profile: JdbcProfile = config.profile

  def close(): Unit = config.db.close()

  def db: JdbcProfile#Backend#Database = config.db
}

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