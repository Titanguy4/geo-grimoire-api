import zio._
import zio.http._

import database.{DatabaseLayer, Schema}
import repository.GeoIndiceRepositoryLive
import service.GeoIndiceServiceLive
import routes.GeoIndiceRoutes
import seeder.Seeder

/**
 * Point d'entrée de l'application GeoGrimoire API.
 * 
 * Cette API REST permet aux joueurs de GeoGuessr d'apprendre et de rechercher
 * des "Meta" (astuces de reconnaissance géographique) comme les langues,
 * les drapeaux, les infrastructures, et le sens de conduite.
 * 
 * Architecture V2 (PostgreSQL) :
 * - database/ : Configuration Quill et schéma SQL
 * - model/ : Définition des données (GeoIndice)
 * - repository/ : Couche d'accès aux données (Quill queries)
 * - service/ : Logique métier
 * - routes/ : Endpoints HTTP + Interface web
 * - seeder/ : Initialisation des données
 * 
 * Stack technique :
 * - Scala 3
 * - ZIO 2 (programmation fonctionnelle pure)
 * - ZIO-Http 3.0.0-RC4 (serveur + routes)
 * - ZIO-Json (sérialisation)
 * - ZIO-Quill 4.8.0 (accès base de données)
 * - PostgreSQL 15 (persistance)
 * - ZIO-Logging (logs console)
 */
object GeoGrimoireApi extends ZIOAppDefault {
  
  /**
   * Configuration du système de logging.
   */
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> zio.logging.consoleLogger()

  /**
   * Point d'entrée de l'application.
   * 
   * Workflow :
   * 1. Connexion à la base de données
   * 2. Création des tables si nécessaire
   * 3. Seeding des données initiales (si vide)
   * 4. Démarrage du serveur HTTP sur le port 8080
   */
  def run: ZIO[Any, Throwable, Unit] = {
    // Route pour servir le fichier HTML statique
    val staticRoutes = Routes(
      Method.GET / "" -> handler {
        ZIO.succeed(Response.redirect(URL.decode("/index.html").toOption.get))
      },
      Method.GET / "index.html" -> handler {
        ZIO.attempt {
          val stream = getClass.getClassLoader.getResourceAsStream("public/index.html")
          if (stream == null) throw new RuntimeException("index.html not found")
          val content = scala.io.Source.fromInputStream(stream, "UTF-8").mkString
          stream.close()
          Response(
            status = Status.Ok,
            headers = Headers(Header.ContentType(MediaType.text.html)),
            body = Body.fromString(content)
          )
        }.catchAll { error =>
          ZIO.logError(s"Error loading index.html: ${error.getMessage}") *>
          ZIO.succeed(Response.text("Error loading page").status(Status.NotFound))
        }
      }
    )
    
    val allRoutes = (staticRoutes ++ GeoIndiceRoutes()).toHttpApp
    
    val app = for {
      _ <- ZIO.logInfo("=== Démarrage de GeoGrimoire API ===")
      
      // Étape 1 : Création des tables
      _ <- ZIO.logInfo("📊 Initialisation du schéma de base de données...")
      _ <- Schema.createTables
      
      // Étape 2 : Seeding
      _ <- Seeder.seed
      
      // Étape 3 : Démarrage du serveur HTTP
      _ <- ZIO.logInfo("🚀 Démarrage du serveur HTTP sur le port 8080...")
      _ <- Server.serve(allRoutes)
      
      _ <- ZIO.logInfo("✅ Serveur HTTP démarré")
      _ <- ZIO.logInfo("📍 Endpoints disponibles :")
      _ <- ZIO.logInfo("  - GET  /                 : Interface web")
      _ <- ZIO.logInfo("  - GET  /index.html       : Interface web")
      _ <- ZIO.logInfo("  - GET  /health           : Health check")
      _ <- ZIO.logInfo("  - GET  /indices          : Liste des indices (avec filtres)")
      _ <- ZIO.logInfo("  - GET  /indices/random   : Indice aléatoire")
      _ <- ZIO.logInfo("  - POST /indices          : Ajouter un indice")
      
    } yield ()
    
    // Composition des layers
    app.provide(
      Server.default,
      DatabaseLayer.live,
      GeoIndiceRepositoryLive.layer,
      GeoIndiceServiceLive.layer
    )
  }
}
