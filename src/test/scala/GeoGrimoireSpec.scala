import model.GeoIndice
import service.GeoIndiceServiceLive
import zio.*
import zio.test.*
import zio.test.Assertion.*

/**
 * Suite de tests pour l'API GeoGrimoire.
 * 
 * Tests couverts :
 * - Filtrage par région, catégorie, et mots-clés
 * - Recherche dans le contenu et les keywords
 * - Intégration POST → GET
 * - Insensibilité à la casse
 * - Filtrage combiné (critères multiples)
 */
object GeoGrimoireSpec extends ZIOSpecDefault {

  /**
   * Données de test standardisées utilisées dans plusieurs tests.
   */
  val testData: List[GeoIndice] = List(
    GeoIndice(
      country = "Bulgarie",
      region = "Europe de l'Est",
      category = "Langue",
      content = "Alphabet cyrillique utilisé en Bulgarie",
      keywords = List("cyrillique", "bulgare")
    ),
    GeoIndice(
      country = "Pologne",
      region = "Europe de l'Est",
      category = "Langue",
      content = "Alphabet latin avec diacritiques",
      keywords = List("latin")
    ),
    GeoIndice(
      country = "Japon",
      region = "Asie de l'Est",
      category = "Conduite",
      content = "Conduite à gauche",
      keywords = List("gauche", "left-hand")
    ),
    GeoIndice(
      country = "Grèce",
      region = "Europe du Sud",
      category = "Langue",
      content = "Alphabet grec",
      keywords = List("grec")
    )
  )

  def spec = suite("GeoGrimoire API Tests")(
    
    // ============================================
    // Test : Filtrage par région
    // ============================================
    test("Filtrage par région doit retourner uniquement les indices correspondants") {
      val filtered = GeoIndiceServiceLive.filterIndices(
        testData,
        country = None,
        region = Some("Europe de l'Est"),
        category = None,
        query = None
      )

      assertTrue(filtered.size == 2) &&
      assertTrue(filtered.forall(_.region == "Europe de l'Est"))
    },

    // ============================================
    // Test : Filtrage par catégorie
    // ============================================
    test("Filtrage par catégorie doit retourner tous les indices de cette catégorie") {
      val filtered = GeoIndiceServiceLive.filterIndices(
        testData,
        country = None,
        region = None,
        category = Some("Langue"),
        query = None
      )

      assertTrue(filtered.size == 3) &&
      assertTrue(filtered.forall(_.category == "Langue"))
    },

    // ============================================
    // Test : Recherche dans le contenu
    // ============================================
    test("Recherche par mot-clé doit trouver les indices correspondants dans le contenu") {
      val filtered = GeoIndiceServiceLive.filterIndices(
        testData,
        country = None,
        region = None,
        category = None,
        query = Some("alphabet")
      )

      assertTrue(filtered.size == 3) &&
      assertTrue(filtered.map(_.country).toSet == Set("Bulgarie", "Pologne", "Grèce"))
    },

    // ============================================
    // Test : Recherche dans les keywords
    // ============================================
    test("Recherche par mot-clé dans keywords doit trouver les indices correspondants") {
      val filtered = GeoIndiceServiceLive.filterIndices(
        testData,
        country = None,
        region = None,
        category = None,
        query = Some("cyrillique")
      )

      assertTrue(filtered.size == 1) &&
      assertTrue(filtered.head.country == "Bulgarie")
    },

    // ============================================
    // Test : Intégration POST → GET
    // ============================================
    test("POST d'un nouvel indice puis GET doit retourner l'indice ajouté") {
      for {
        // Initialiser un Ref avec des données de test
        indicesRef <- Ref.make(List(
          GeoIndice(
            country = "France",
            region = "Europe de l'Ouest",
            category = "Langue",
            content = "Français parlé en France",
            keywords = List("français", "french")
          )
        ))
        
        // Créer le service
        service = GeoIndiceServiceLive(indicesRef)
        
        // Créer un nouvel indice
        newIndice = GeoIndice(
          country = "Espagne",
          region = "Europe du Sud",
          category = "Langue",
          content = "Espagnol parlé en Espagne",
          keywords = List("español", "spanish")
        )
        
        // Ajouter l'indice via le service
        count <- service.add(newIndice)
        
        // Récupérer tous les indices
        allIndices <- service.getAll
        
        // Vérifier que le nouvel indice est présent
        containsNewIndice = allIndices.exists(i => 
          i.country == "Espagne" && i.category == "Langue"
        )
      } yield assertTrue(count == 2) && 
              assertTrue(allIndices.size == 2) && 
              assertTrue(containsNewIndice)
    },

    // ============================================
    // Test : Insensibilité à la casse
    // ============================================
    test("Le filtrage doit être insensible à la casse") {
      val filtered1 = GeoIndiceServiceLive.filterIndices(
        testData,
        country = Some("JAPON"),
        region = None,
        category = None,
        query = None
      )

      val filtered2 = GeoIndiceServiceLive.filterIndices(
        testData,
        country = Some("japon"),
        region = None,
        category = None,
        query = None
      )

      assertTrue(filtered1.size == 1) && 
      assertTrue(filtered2.size == 1) &&
      assertTrue(filtered1.head.country == "Japon") &&
      assertTrue(filtered2.head.country == "Japon")
    },

    // ============================================
    // Test : Filtrage combiné
    // ============================================
    test("Filtrage combiné doit appliquer tous les critères (AND logique)") {
      val filtered = GeoIndiceServiceLive.filterIndices(
        testData,
        country = None,
        region = Some("Europe de l'Est"),
        category = Some("Langue"),
        query = None
      )

      assertTrue(filtered.size == 2) &&
      assertTrue(filtered.forall(i => i.region == "Europe de l'Est" && i.category == "Langue"))
    },

    // ============================================
    // Test : Élément aléatoire
    // ============================================
    test("getRandom doit retourner None si la liste est vide") {
      for {
        emptyRef <- Ref.make(List.empty[GeoIndice])
        service = GeoIndiceServiceLive(emptyRef)
        result <- service.getRandom
      } yield assertTrue(result.isEmpty)
    },

    // ============================================
    // Test : Élément aléatoire (liste non vide)
    // ============================================
    test("getRandom doit retourner un élément si la liste n'est pas vide") {
      for {
        ref <- Ref.make(testData)
        service = GeoIndiceServiceLive(ref)
        result <- service.getRandom
      } yield assertTrue(result.isDefined) &&
              assertTrue(testData.contains(result.get))
    }
  )
}
