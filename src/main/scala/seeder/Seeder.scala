package seeder

import zio._
import repository.GeoIndiceRepository
import data.InitialData

/**
 * Service responsable de l'initialisation des données.
 * 
 * Vérifie si la base de données est vide et insère les données
 * initiales si nécessaire au démarrage de l'application.
 */
object Seeder {
  
  /**
   * Initialise la base de données avec les données initiales si elle est vide.
   * 
   * Workflow :
   * 1. Vérifie le nombre d'indices existants
   * 2. Si la table est vide, insère tous les indices de InitialData
   * 3. Sinon, ne fait rien
   */
  def seed: ZIO[GeoIndiceRepository, Throwable, Unit] = {
    for {
      repo <- ZIO.service[GeoIndiceRepository]
      
      _ <- ZIO.logInfo("🌱 Vérification des données initiales...")
      
      count <- repo.count
      
      _ <- if (count == 0) {
        for {
          _ <- ZIO.logInfo(s"📦 Base de données vide. Insertion de ${InitialData.indices.size} indices...")
          
          // Insérer chaque indice
          _ <- ZIO.foreachDiscard(InitialData.indices) { indice =>
            for {
              id <- repo.add(indice)
              _ <- ZIO.logInfo(s"  ✓ Indice inséré : ${indice.country} (ID: $id)")
            } yield ()
          }
          
          newCount <- repo.count
          _ <- ZIO.logInfo(s"✅ Seeding terminé : $newCount indices en base")
          
        } yield ()
      } else {
        ZIO.logInfo(s"✓ Base de données déjà initialisée ($count indices)")
      }
      
    } yield ()
  }
}
