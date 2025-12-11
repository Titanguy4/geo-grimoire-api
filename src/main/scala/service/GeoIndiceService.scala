package service

import model.GeoIndice
import zio.*
import repository.GeoIndiceRepository

/**
 * Service métier pour la gestion des indices géographiques.
 * 
 * Délègue les opérations de persistance au repository
 * et fournit une couche d'abstraction pour la logique métier.
 */
trait GeoIndiceService {
  
  /**
   * Récupère tous les indices stockés.
   */
  def getAll: Task[List[GeoIndice]]
  
  /**
   * Récupère les indices filtrés selon les critères fournis.
   * 
   * Les filtres sont appliqués de manière cumulative (AND logique).
   * Tous les filtres sont insensibles à la casse.
   */
  def getFiltered(
    country: Option[String],
    region: Option[String],
    category: Option[String],
    query: Option[String]
  ): Task[List[GeoIndice]]
  
  /**
   * Récupère un indice aléatoire parmi tous les indices disponibles.
   */
  def getRandom: Task[Option[GeoIndice]]
  
  /**
   * Ajoute un nouvel indice à la collection.
   * 
   * @return L'ID du nouvel indice créé
   */
  def add(indice: GeoIndice): Task[Long]
  
  /**
   * Compte le nombre total d'indices.
   */
  def count: Task[Long]
}

/**
 * Implémentation du service utilisant le repository PostgreSQL.
 */
final case class GeoIndiceServiceLive(repository: GeoIndiceRepository) extends GeoIndiceService {
  
  override def getAll: Task[List[GeoIndice]] = 
    repository.getAll
  
  override def getFiltered(
    country: Option[String],
    region: Option[String],
    category: Option[String],
    query: Option[String]
  ): Task[List[GeoIndice]] = 
    repository.getFiltered(country, region, category, query)
  
  override def getRandom: Task[Option[GeoIndice]] = 
    repository.getRandom
  
  override def add(indice: GeoIndice): Task[Long] = 
    repository.add(indice)
  
  override def count: Task[Long] = 
    repository.count
}

object GeoIndiceServiceLive {
  
  val layer: ZLayer[GeoIndiceRepository, Nothing, GeoIndiceService] =
    ZLayer.fromFunction(GeoIndiceServiceLive.apply)
}

/**
 * Accesseurs pour utiliser le service via ZIO.
 */
object GeoIndiceService {
  
  def getAll: ZIO[GeoIndiceService, Throwable, List[GeoIndice]] =
    ZIO.serviceWithZIO[GeoIndiceService](_.getAll)
  
  def getFiltered(
    country: Option[String],
    region: Option[String],
    category: Option[String],
    query: Option[String]
  ): ZIO[GeoIndiceService, Throwable, List[GeoIndice]] =
    ZIO.serviceWithZIO[GeoIndiceService](_.getFiltered(country, region, category, query))
  
  def getRandom: ZIO[GeoIndiceService, Throwable, Option[GeoIndice]] =
    ZIO.serviceWithZIO[GeoIndiceService](_.getRandom)
  
  def add(indice: GeoIndice): ZIO[GeoIndiceService, Throwable, Long] =
    ZIO.serviceWithZIO[GeoIndiceService](_.add(indice))
  
  def count: ZIO[GeoIndiceService, Throwable, Long] =
    ZIO.serviceWithZIO[GeoIndiceService](_.count)
}
