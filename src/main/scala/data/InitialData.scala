package data

import model.GeoIndice

/**
 * Données initiales de l'application GeoGrimoire.
 * 
 * Contient une collection d'exemples réels d'indices géographiques
 * pour différents pays et régions, couvrant plusieurs catégories
 * (Langue, Conduite, Infrastructure, Meta).
 */
object InitialData {
  
  /**
   * Liste des indices géographiques pré-chargés au démarrage.
   * 
   * Ces exemples couvrent différentes régions du monde et catégories :
   * - Conduite : Sens de circulation (gauche/droite)
   * - Langue : Alphabets et langues parlées
   * - Infra : Infrastructure typique (poteaux, routes)
   * - Meta : Éléments visuels caractéristiques
   */
  val indices: List[GeoIndice] = List(
    GeoIndice(
      country = "Japon",
      region = "Asie de l'Est",
      category = "Conduite",
      content = "Au Japon, on conduit à gauche (comme au Royaume-Uni)",
      keywords = List("gauche", "left-hand", "volant droit")
    ),
    
    GeoIndice(
      country = "Bulgarie",
      region = "Europe de l'Est",
      category = "Langue",
      content = "La Bulgarie utilise l'alphabet cyrillique",
      keywords = List("cyrillique", "cyrillic", "Б", "Д", "Ж")
    ),
    
    GeoIndice(
      country = "Brésil",
      region = "Amérique du Sud",
      category = "Langue",
      content = "Au Brésil, on parle portugais (seul pays lusophone d'Amérique du Sud)",
      keywords = List("português", "lusophone", "ão", "nh")
    ),
    
    GeoIndice(
      country = "Suède",
      region = "Europe du Nord",
      category = "Infra",
      content = "Poteaux électriques suédois: souvent peints en blanc/rouge ou jaune, forme triangulaire à la base",
      keywords = List("poteaux", "poles", "triangulaire", "blanc rouge")
    ),
    
    GeoIndice(
      country = "Australie",
      region = "Océanie",
      category = "Conduite",
      content = "Conduite à gauche en Australie (héritage britannique)",
      keywords = List("gauche", "left-hand", "commonwealth")
    ),
    
    GeoIndice(
      country = "Grèce",
      region = "Europe du Sud",
      category = "Langue",
      content = "Alphabet grec utilisé en Grèce: Α, Β, Γ, Δ, Ω",
      keywords = List("grec", "greek", "alpha", "omega", "Π")
    ),
    
    GeoIndice(
      country = "Afrique du Sud",
      region = "Afrique australe",
      category = "Meta",
      content = "Pickup Toyota Hilux typique sur les routes sud-africaines",
      keywords = List("hilux", "pickup", "voiture", "véhicule")
    )
  )
}
