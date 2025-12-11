package repository

import database.Schema.*
import io.getquill.*
import io.getquill.jdbczio.Quill
import model.GeoIndice
import zio.*

/**
 * Repository pour les opérations CRUD sur les indices géographiques.
 * 
 * Gère la persistance en base de données PostgreSQL via Quill,
 * incluant la gestion de la relation many-to-many avec les keywords.
 */
trait GeoIndiceRepository {
  
  /**
   * Récupère tous les indices géographiques.
   */
  def getAll: Task[List[GeoIndice]]
  
  /**
   * Récupère les indices filtrés selon les critères.
   */
  def getFiltered(
    country: Option[String],
    region: Option[String],
    category: Option[String],
    searchQuery: Option[String]
  ): Task[List[GeoIndice]]
  
  /**
   * Récupère un indice aléatoire.
   */
  def getRandom: Task[Option[GeoIndice]]
  
  /**
   * Ajoute un nouvel indice avec ses keywords.
   * Retourne l'ID du nouvel indice.
   */
  def add(indice: GeoIndice): Task[Long]
  
  /**
   * Compte le nombre total d'indices.
   */
  def count: Task[Long]
}

/**
 * Implémentation du repository utilisant Quill.
 */
final case class GeoIndiceRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends GeoIndiceRepository {
  
  import quill._
  
  /**
   * Récupère tous les indices avec leurs keywords.
   */
  override def getAll: Task[List[GeoIndice]] = {
    for {
      // Récupérer tous les indices
      entities <- run(quote { query[GeoIndiceEntity] })
      
      // Pour chaque indice, récupérer ses keywords
      indicesWithKeywords <- ZIO.foreach(entities) { entity =>
        getKeywordsForIndice(entity.id.get).map { keywords =>
          GeoIndice(
            country = entity.country,
            region = entity.region,
            category = entity.category,
            content = entity.content,
            keywords = keywords
          )
        }
      }
    } yield indicesWithKeywords
  }
  
  /**
   * Récupère les indices filtrés.
   */
  override def getFiltered(
    country: Option[String],
    region: Option[String],
    category: Option[String],
    searchQuery: Option[String]
  ): Task[List[GeoIndice]] = {
    // Prédicats de filtrage en style fonctionnel
    def matchesCountry(indice: GeoIndice): Boolean =
      country.forall(c => indice.country.toLowerCase.contains(c.toLowerCase))
    
    def matchesRegion(indice: GeoIndice): Boolean =
      region.forall(r => indice.region.toLowerCase.contains(r.toLowerCase))
    
    def matchesCategory(indice: GeoIndice): Boolean =
      category.forall(cat => indice.category.toLowerCase.contains(cat.toLowerCase))
    
    def matchesQuery(indice: GeoIndice): Boolean =
      searchQuery.forall { q =>
        val qLower = q.toLowerCase
        indice.content.toLowerCase.contains(qLower) ||
        indice.keywords.exists(_.toLowerCase.contains(qLower))
      }
    
    // Composition de tous les prédicats
    val matchesAll: GeoIndice => Boolean = indice =>
      matchesCountry(indice) && matchesRegion(indice) && 
      matchesCategory(indice) && matchesQuery(indice)
    
    getAll.map(_.filter(matchesAll))
  }
  
  /**
   * Récupère un indice aléatoire.
   */
  override def getRandom: Task[Option[GeoIndice]] =
    count.flatMap { totalCount =>
      ZIO.when(totalCount > 0) {
        for {
          randomOffset <- Random.nextLongBounded(totalCount)
          offsetInt = randomOffset.toInt
          entities <- run {
            quote {
              query[GeoIndiceEntity]
                .drop(lift(offsetInt))
                .take(1)
            }
          }
          maybeIndice <- ZIO.foreach(entities.headOption) { entity =>
            getKeywordsForIndice(entity.id.get).map { keywords =>
              GeoIndice(
                country = entity.country,
                region = entity.region,
                category = entity.category,
                content = entity.content,
                keywords = keywords
              )
            }
          }
        } yield maybeIndice
      }.map(_.flatten)
    }
  
  /**
   * Ajoute un nouvel indice avec ses keywords en transaction.
   */
  override def add(indice: GeoIndice): Task[Long] = {
    transaction {
      for {
        // Insérer l'indice principal
        indiceId <- run {
          quote {
            query[GeoIndiceEntity]
              .insertValue(lift(GeoIndiceEntity(
                id = None,
                country = indice.country,
                region = indice.region,
                category = indice.category,
                content = indice.content
              )))
              .returningGenerated(_.id)
          }
        }
        
        // Insérer les keywords et créer les associations
        _ <- ZIO.foreach(indice.keywords) { keyword =>
          insertKeywordAndLink(indiceId.getOrElse(0L), keyword)
        }
        
      } yield indiceId.getOrElse(0L)
    }
  }
  
  /**
   * Compte le nombre total d'indices.
   */
  override def count: Task[Long] = {
    run(quote { query[GeoIndiceEntity].size })
  }
  
  /**
   * Récupère les keywords pour un indice donné.
   */
  private def getKeywordsForIndice(indiceId: Long): Task[List[String]] = {
    run {
      quote {
        query[GeoIndiceKeywordEntity]
          .filter(_.geoIndiceId == lift(indiceId))
          .join(query[KeywordEntity])
          .on((junction, keyword) => junction.keywordId == keyword.id.getOrElse(0L))
          .map(_._2.keyword)
      }
    }
  }
  
  /**
   * Insère un keyword (ou récupère son ID s'il existe) et crée l'association.
   */
  private def insertKeywordAndLink(indiceId: Long, keyword: String): Task[Unit] =
    for {
      existingKeywords <- run {
        quote {
          query[KeywordEntity]
            .filter(_.keyword == lift(keyword))
        }
      }
      
      keywordId <- existingKeywords.headOption.fold(
        // Aucun keyword existant : on l'insère
        run {
          quote {
            query[KeywordEntity]
              .insertValue(lift(KeywordEntity(None, keyword)))
              .returningGenerated(_.id)
          }
        }.map(_.getOrElse(0L))
      )(
        // Keyword existant : on réutilise son ID
        existing => ZIO.succeed(existing.id.get)
      )
      
      _ <- run {
        quote {
          query[GeoIndiceKeywordEntity]
            .insertValue(lift(GeoIndiceKeywordEntity(indiceId, keywordId)))
        }
      }
    } yield ()
}

object GeoIndiceRepositoryLive {
  
  val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, GeoIndiceRepository] =
    ZLayer.fromFunction(GeoIndiceRepositoryLive.apply)
}