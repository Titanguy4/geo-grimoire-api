package model

import zio.json.*

/**
 * Représente une connaissance géographique pour GeoGuessr.
 * 
 * Un indice géographique contient des informations sur un pays ou une région
 * spécifique, classées par catégorie (Langue, Conduite, Infra, etc.).
 * 
 * @param country Le nom du pays concerné (ex: "Japon", "Bulgarie")
 * @param region La région géographique (ex: "Asie de l'Est", "Europe du Sud")
 * @param category La catégorie de l'indice (Langue, Conduite, Drapeau, Infra, Meta)
 * @param content La description détaillée de l'indice
 * @param keywords Les mots-clés facilitant la recherche (ex: "cyrillique", "gauche")
 */
case class GeoIndice(
  country: String,
  region: String,
  category: String,
  content: String,
  keywords: List[String]
)

object GeoIndice {
  /**
   * Codec JSON implicite pour la sérialisation/désérialisation automatique
   * des instances de GeoIndice.
   */
  implicit val codec: JsonCodec[GeoIndice] = DeriveJsonCodec.gen[GeoIndice]
}
