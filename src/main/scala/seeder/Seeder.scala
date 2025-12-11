package seeder

import zio.*
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
   */
  def seed: ZIO[GeoIndiceRepository, Throwable, Unit] =
    ZIO.serviceWithZIO[GeoIndiceRepository] { repo =>
      ZIO.logInfo("🌱 Vérification des données initiales...") *>
      repo.count.flatMap { count =>
        ZIO.ifZIO(ZIO.succeed(count == 0))(
          onTrue = 
            ZIO.logInfo(s"📦 Base de données vide. Insertion de ${InitialData.indices.size} indices...") *>
            ZIO.foreachDiscard(InitialData.indices) { indice =>
              repo.add(indice).flatMap { id =>
                ZIO.logInfo(s"  ✓ Indice inséré : ${indice.country} (ID: $id)")
              }
            } *>
            repo.count.flatMap { newCount =>
              ZIO.logInfo(s"✅ Seeding terminé : $newCount indices en base")
            },
          onFalse =
            ZIO.logInfo(s"✓ Base de données déjà initialisée ($count indices)")
        )
      }
    }
}
