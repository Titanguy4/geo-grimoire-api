package service

import model.GeoIndice
import zio.*

/**
 * Service métier pour la gestion des indices géographiques.
 * 
 * Gère l'état en mémoire des indices et fournit des opérations
 * pour les manipuler de manière thread-safe avec ZIO Ref.
 */
trait GeoIndiceService {
  
  /**
   * Récupère tous les indices stockés.
   * @return Une liste de tous les indices géographiques
   */
  def getAll: UIO[List[GeoIndice]]
  
  /**
   * Récupère les indices filtrés selon les critères fournis.
   * 
   * Les filtres sont appliqués de manière cumulative (AND logique).
   * Tous les filtres sont insensibles à la casse.
   * 
   * @param country Filtre par nom de pays (correspondance partielle)
   * @param region Filtre par région (correspondance partielle)
   * @param category Filtre par catégorie (correspondance partielle)
   * @param query Recherche dans le contenu et les mots-clés
   * @return Une liste filtrée d'indices
   */
  def getFiltered(
    country: Option[String],
    region: Option[String],
    category: Option[String],
    query: Option[String]
  ): UIO[List[GeoIndice]]
  
  /**
   * Récupère un indice aléatoire parmi tous les indices disponibles.
   * 
   * @return Un Option contenant un indice aléatoire, ou None si la liste est vide
   */
  def getRandom: UIO[Option[GeoIndice]]
  
  /**
   * Ajoute un nouvel indice à la collection.
   * 
   * @param indice Le nouvel indice à ajouter
   * @return Le nombre total d'indices après l'ajout
   */
  def add(indice: GeoIndice): UIO[Int]
}

/**
 * Implémentation du service utilisant un Ref pour l'état en mémoire.
 */
final case class GeoIndiceServiceLive(indicesRef: Ref[List[GeoIndice]]) extends GeoIndiceService {
  
  override def getAll: UIO[List[GeoIndice]] = 
    indicesRef.get
  
  override def getFiltered(
    country: Option[String],
    region: Option[String],
    category: Option[String],
    query: Option[String]
  ): UIO[List[GeoIndice]] = 
    indicesRef.get.map { allIndices =>
      GeoIndiceServiceLive.filterIndices(allIndices, country, region, category, query)
    }
  
  override def getRandom: UIO[Option[GeoIndice]] = 
    for {
      allIndices <- indicesRef.get
      maybeIndice <- if (allIndices.isEmpty) {
        ZIO.succeed(None)
      } else {
        Random.nextIntBounded(allIndices.size).map(index => Some(allIndices(index)))
      }
    } yield maybeIndice
  
  override def add(indice: GeoIndice): UIO[Int] = 
    indicesRef.updateAndGet(list => list :+ indice).map(_.size)
}

object GeoIndiceServiceLive {
  
  /**
   * Crée une instance du service avec les données initiales.
   * 
   * @param initialData Les indices à charger au démarrage
   * @return Un ZLayer fournissant le service
   */
  def layer(initialData: List[GeoIndice]): ZLayer[Any, Nothing, GeoIndiceService] =
    ZLayer {
      for {
        ref <- Ref.make(initialData)
      } yield GeoIndiceServiceLive(ref)
    }
  
  /**
   * Fonction pure de filtrage des indices.
   * 
   * Applique les critères de filtrage de manière cumulative.
   * Tous les filtrages sont insensibles à la casse.
   * 
   * @param indices La liste des indices à filtrer
   * @param country Filtre optionnel par pays
   * @param region Filtre optionnel par région
   * @param category Filtre optionnel par catégorie
   * @param query Recherche optionnelle dans le contenu et les mots-clés
   * @return La liste filtrée
   */
  def filterIndices(
    indices: List[GeoIndice],
    country: Option[String],
    region: Option[String],
    category: Option[String],
    query: Option[String]
  ): List[GeoIndice] = {
    indices.filter { indice =>
      // Filtre par pays (insensible à la casse)
      val matchesCountry = country.forall(c => 
        indice.country.toLowerCase.contains(c.toLowerCase)
      )
      
      // Filtre par région (insensible à la casse)
      val matchesRegion = region.forall(r => 
        indice.region.toLowerCase.contains(r.toLowerCase)
      )
      
      // Filtre par catégorie (insensible à la casse)
      val matchesCategory = category.forall(cat => 
        indice.category.toLowerCase.contains(cat.toLowerCase)
      )
      
      // Recherche dans le contenu et les mots-clés (insensible à la casse)
      val matchesQuery = query.forall { q =>
        val qLower = q.toLowerCase
        indice.content.toLowerCase.contains(qLower) ||
        indice.keywords.exists(_.toLowerCase.contains(qLower))
      }
      
      // Tous les critères doivent être satisfaits (AND logique)
      matchesCountry && matchesRegion && matchesCategory && matchesQuery
    }
  }
}

/**
 * Accesseurs pour utiliser le service via ZIO.
 */
object GeoIndiceService {
  
  def getAll: ZIO[GeoIndiceService, Nothing, List[GeoIndice]] =
    ZIO.serviceWithZIO[GeoIndiceService](_.getAll)
  
  def getFiltered(
    country: Option[String],
    region: Option[String],
    category: Option[String],
    query: Option[String]
  ): ZIO[GeoIndiceService, Nothing, List[GeoIndice]] =
    ZIO.serviceWithZIO[GeoIndiceService](_.getFiltered(country, region, category, query))
  
  def getRandom: ZIO[GeoIndiceService, Nothing, Option[GeoIndice]] =
    ZIO.serviceWithZIO[GeoIndiceService](_.getRandom)
  
  def add(indice: GeoIndice): ZIO[GeoIndiceService, Nothing, Int] =
    ZIO.serviceWithZIO[GeoIndiceService](_.add(indice))
}
