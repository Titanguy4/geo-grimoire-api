package database

import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*
import com.zaxxer.hikari.HikariConfig
import javax.sql.DataSource

/**
 * Configuration de la couche base de données.
 * 
 * Fournit le contexte Quill pour exécuter les requêtes SQL
 * et configure le pool de connexions HikariCP.
 */
object DatabaseLayer {
  
  /**
   * Layer du DataSource avec HikariCP.
   */
  val dataSourceLayer: ZLayer[Any, Throwable, DataSource] =
    ZLayer.fromZIO {
      ZIO.attempt {
        val config = new HikariConfig()
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/geogrimoire")
        config.setUsername("geouser")
        config.setPassword("geopassword")
        config.setDriverClassName("org.postgresql.Driver")
        config.setMaximumPoolSize(10)
        config.setMinimumIdle(2)
        config.setConnectionTimeout(30000)
        config.setIdleTimeout(600000)
        config.setMaxLifetime(1800000)
        
        new com.zaxxer.hikari.HikariDataSource(config)
      }
    }
  
  /**
   * Layer Quill pour PostgreSQL avec naming strategy SnakeCase.
   */
  val quillLayer: ZLayer[DataSource, Nothing, Quill.Postgres[SnakeCase]] =
    Quill.Postgres.fromNamingStrategy(SnakeCase)
  
  /**
   * Layer complet de la base de données (DataSource + Quill).
   */
  val live: ZLayer[Any, Throwable, DataSource with Quill.Postgres[SnakeCase]] =
    dataSourceLayer >+> (dataSourceLayer >>> quillLayer)
}