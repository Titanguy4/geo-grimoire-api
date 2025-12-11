import data.InitialData
import routes.GeoIndiceRoutes
import service.GeoIndiceServiceLive
import zio.*
import zio.http.*

/**
 * Point d'entrée de l'application GeoGrimoire API.
 * 
 * Cette API REST permet aux joueurs de GeoGuessr d'apprendre et de rechercher
 * des "Meta" (astuces de reconnaissance géographique) comme les langues,
 * les drapeaux, les infrastructures, et le sens de conduite.
 * 
 * Architecture :
 * - model/ : Définition des données (GeoIndice)
 * - data/ : Données initiales pré-chargées
 * - service/ : Logique métier et gestion de l'état
 * - routes/ : Endpoints HTTP (GET /indices, GET /indices/random, POST /indices)
 * 
 * Stack technique :
 * - Scala 3
 * - ZIO 2 (programmation fonctionnelle pure)
 * - ZIO-Http 3.0.0-RC4
 * - ZIO-Json (sérialisation)
 * - ZIO-Logging (logs console)
 */
object GeoGrimoireApi extends ZIOAppDefault {
  
  /**
   * Configuration du système de logging.
   * 
   * Remplace le logger par défaut de ZIO par un console logger
   * pour afficher les logs dans la sortie standard.
   */
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> zio.logging.consoleLogger()

  /**
   * Point d'entrée de l'application.
   * 
   * Workflow :
   * 1. Chargement des données initiales
   * 2. Initialisation du service métier
   * 3. Configuration des routes HTTP
   * 4. Démarrage du serveur sur le port 8080
   */
  def run: ZIO[Any, Throwable, Unit] = {
    val app = for {
      _ <- ZIO.logInfo("=== Démarrage de GeoGrimoire API ===")
      _ <- ZIO.logInfo(s"Chargement de ${InitialData.indices.size} indices initiaux...")
      
      // Démarrage du serveur HTTP avec injection des dépendances
      _ <- Server
        .serve(GeoIndiceRoutes().toHttpApp)
        .provide(
          // Configuration du serveur (port 8080 par défaut)
          Server.default,
          // Injection du service avec les données initiales
          GeoIndiceServiceLive.layer(InitialData.indices)
        )
      
      _ <- ZIO.logInfo("Serveur HTTP démarré sur http://localhost:8080")
      _ <- ZIO.logInfo("Endpoints disponibles :")
      _ <- ZIO.logInfo("  - GET  /indices          : Liste des indices (avec filtres optionnels)")
      _ <- ZIO.logInfo("  - GET  /indices/random   : Indice aléatoire")
      _ <- ZIO.logInfo("  - POST /indices          : Ajouter un nouvel indice")
      
    } yield ()
    
    app
  }
}
