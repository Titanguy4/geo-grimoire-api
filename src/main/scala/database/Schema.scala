package database

import io.getquill._
import io.getquill.jdbczio.Quill
import zio._
import javax.sql.DataSource
import java.sql.Connection

/**
 * Schéma de la base de données.
 * 
 * Définit les entités pour les tables :
 * - geo_indice : Table principale des indices géographiques
 * - keyword : Table des mots-clés
 * - geo_indice_keyword : Table de jonction (many-to-many)
 */
object Schema {
  
  /**
   * Entité pour la table geo_indice.
   */
  case class GeoIndiceEntity(
    id: Option[Long],
    country: String,
    region: String,
    category: String,
    content: String
  )
  
  /**
   * Entité pour la table keyword.
   */
  case class KeywordEntity(
    id: Option[Long],
    keyword: String
  )
  
  /**
   * Table de jonction entre geo_indice et keyword.
   */
  case class GeoIndiceKeywordEntity(
    geoIndiceId: Long,
    keywordId: Long
  )
  
  /**
   * Exécute une commande DDL via JDBC direct.
   */
  private def executeDDL(sql: String): ZIO[DataSource, Throwable, Unit] = {
    ZIO.serviceWithZIO[DataSource] { ds =>
      ZIO.attemptBlocking {
        val conn: Connection = ds.getConnection
        try {
          val stmt = conn.createStatement()
          try {
            stmt.execute(sql)
          } finally {
            stmt.close()
          }
        } finally {
          conn.close()
        }
      }
    }
  }
  
  /**
   * Crée les tables de la base de données si elles n'existent pas.
   */
  def createTables: ZIO[DataSource, Throwable, Unit] = {
    for {
      _ <- executeDDL(
        """
        CREATE TABLE IF NOT EXISTS geo_indice_entity (
          id SERIAL PRIMARY KEY,
          country VARCHAR(255) NOT NULL,
          region VARCHAR(255) NOT NULL,
          category VARCHAR(100) NOT NULL,
          content TEXT NOT NULL
        )
        """
      )
      _ <- ZIO.logInfo("✓ Table geo_indice_entity créée")
      
      _ <- executeDDL(
        """
        CREATE TABLE IF NOT EXISTS keyword_entity (
          id SERIAL PRIMARY KEY,
          keyword VARCHAR(255) NOT NULL UNIQUE
        )
        """
      )
      _ <- ZIO.logInfo("✓ Table keyword_entity créée")
      
      _ <- executeDDL(
        """
        CREATE TABLE IF NOT EXISTS geo_indice_keyword_entity (
          geo_indice_id BIGINT NOT NULL REFERENCES geo_indice_entity(id) ON DELETE CASCADE,
          keyword_id BIGINT NOT NULL REFERENCES keyword_entity(id) ON DELETE CASCADE,
          PRIMARY KEY (geo_indice_id, keyword_id)
        )
        """
      )
      _ <- ZIO.logInfo("✓ Table geo_indice_keyword_entity créée")
      
      _ <- executeDDL(
        """
        CREATE INDEX IF NOT EXISTS idx_keyword_keyword 
        ON keyword_entity(keyword)
        """
      )
      _ <- ZIO.logInfo("✓ Index sur keyword créé")
      
    } yield ()
  }
}