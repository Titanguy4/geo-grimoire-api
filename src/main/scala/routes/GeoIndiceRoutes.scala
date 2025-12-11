package routes

import model.GeoIndice
import service.GeoIndiceService
import zio.*
import zio.http.*
import zio.json.*

/**
 * Définition des routes HTTP de l'API GeoGrimoire.
 * 
 * Expose trois endpoints :
 * - GET /indices : Liste des indices avec filtrage optionnel
 * - GET /indices/random : Un indice aléatoire
 * - POST /indices : Ajout d'un nouvel indice
 */
object GeoIndiceRoutes {
  
  /**
   * Construit les routes HTTP de l'API.
   * 
   * Toutes les routes incluent du logging pour faciliter le débogage
   * et le monitoring des requêtes.
   * 
   * @return Les routes configurées pour l'application
   */
  def apply(): Routes[GeoIndiceService, Response] = Routes(
    
    // ========================================
    // GET /indices - Liste avec filtrage
    // ========================================
    Method.GET / "indices" -> handler { (req: Request) =>
      for {
        _ <- ZIO.logInfo("GET /indices - Requête reçue")
        
        // Extraction des paramètres de query string
        params = req.url.queryParams
        country = extractQueryParam(params, "country")
        region = extractQueryParam(params, "region")
        category = extractQueryParam(params, "category")
        query = extractQueryParam(params, "q")
        
        _ <- ZIO.logInfo(s"Filtres: country=$country, region=$region, category=$category, query=$query")
        
        // Récupération des indices filtrés via le service
        filtered <- GeoIndiceService.getFiltered(country, region, category, query)
        
        _ <- ZIO.logInfo(s"${filtered.size} résultat(s) trouvé(s)")
        
        // Conversion en JSON et envoi de la réponse
        response <- ZIO.succeed(Response.json(filtered.toJson))
      } yield response
    },
    
    // ========================================
    // GET /indices/random - Indice aléatoire
    // ========================================
    Method.GET / "indices" / "random" -> handler {
      for {
        _ <- ZIO.logInfo("GET /indices/random - Requête reçue")
        
        // Récupération d'un indice aléatoire
        maybeIndice <- GeoIndiceService.getRandom
        
        // Gestion du cas où la liste est vide
        response <- maybeIndice match {
          case Some(indice) =>
            ZIO.logInfo(s"Élément aléatoire sélectionné: ${indice.country}") *>
            ZIO.succeed(Response.json(indice.toJson))
          
          case None =>
            ZIO.logWarning("Liste vide, impossible de retourner un élément aléatoire") *>
            ZIO.succeed(
              Response
                .text("Aucun indice disponible")
                .status(Status.NotFound)
            )
        }
      } yield response
    },
    
    // ========================================
    // POST /indices - Ajout d'un nouvel indice
    // ========================================
    Method.POST / "indices" -> handler { (req: Request) =>
      (for {
        _ <- ZIO.logInfo("POST /indices - Requête reçue")
        
        // Lecture et parsing du body JSON
        bodyString <- req.body.asString
        _ <- ZIO.logDebug(s"Body reçu: $bodyString")
        
        // Tentative de désérialisation
        result <- bodyString.fromJson[GeoIndice] match {
          
          // Erreur de parsing : retour 400 Bad Request
          case Left(error) =>
            ZIO.logError(s"Erreur de parsing JSON: $error") *>
            ZIO.succeed(
              Response
                .text(s"Erreur de parsing: $error")
                .status(Status.BadRequest)
            )
          
          // Parsing réussi : ajout de l'indice
          case Right(newIndice) =>
            for {
              _ <- ZIO.logInfo(s"Nouvel indice valide: ${newIndice.country} - ${newIndice.category}")
              
              // Ajout via le service
              count <- GeoIndiceService.add(newIndice)
              
              _ <- ZIO.logInfo(s"Indice ajouté avec succès. Total: $count indices")
            } yield Response
              .text("Indice ajouté avec succès")
              .status(Status.Created)
        }
      } yield result).orDie // En cas d'erreur inattendue, crash du programme
    }
  )
  
  /**
   * Extrait un paramètre de query string.
   * 
   * Récupère la première valeur du paramètre et la convertit en String.
   * Retourne None si le paramètre n'existe pas.
   * 
   * @param params Les paramètres de la requête
   * @param name Le nom du paramètre à extraire
   * @return Option[String] contenant la valeur ou None
   */
  private def extractQueryParam(params: QueryParams, name: String): Option[String] =
    params.get(name).flatMap(_.headOption).map(_.toString)
}
