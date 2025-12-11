# 🤖 Instructions pour l'Assistant IA (Projet GeoGuessr Grimoire)

## CONTEXTE DU PROJET
Nous construisons une API REST en Scala pour aider les joueurs de GeoGuessr à apprendre les "Meta" (astuces de reconnaissance : langues, drapeaux, poteaux, sens de conduite).

## STACK TECHNIQUE (Strict)
- **Langage :** Scala 3
- **Framework Effets :** ZIO 2 (Pure Functional Programming)
- **Framework Web :** ZIO-Http (Version 3.0.0-RC4)
- **JSON :** ZIO-Json
- **Tests :** ZIO-Test

## RÈGLES DE CODAGE
1. **Pas de `var` :** Utiliser uniquement des valeurs immuables (`val`).
2. **Gestion d'état :** Utiliser `Ref` (ZIO) pour l'état en mémoire. Pas de variables globales mutables.
3. **Types forts :** Utiliser des `case class` pour modéliser les données.
4. **Imports :** Toujours grouper les imports ZIO (`import zio._`, `import zio.http._`).

---

## 📅 TASKS (Feuille de route)

### TÂCHE 1 : Modèle de données & Setup
**Objectif :** Mettre en place la structure de données définie dans les spécifications.
1. Créer le fichier `src/main/scala/Main.scala`.
2. Définir la case class `GeoIndice` avec les champs :
   - `country`: String
   - `region`: String
   - `category`: String (Valeurs attendues: "Langue", "Conduite", "Drapeau", "Infra", "Meta")
   - `content`: String
   - `keywords`: List[String]
3. Générer le codec JSON automatique avec `DeriveJsonCodec`.
4. Créer une liste `initialData` contenant au moins 5 exemples réels (ex: Conduite à gauche au Japon, Cyrillique en Bulgarie...).

### TÂCHE 2 : Logique Métier & État (State)
**Objectif :** Gérer la mémoire et le filtrage.
1. Dans `Main.scala`, créer une application `ZIOAppDefault`.
2. Initialiser un `Ref[List[GeoIndice]]` avec les données initiales.
3. Implémenter une fonction pure (ou locale) pour filtrer la liste selon :
   - `country` (insensible à la casse)
   - `region` (insensible à la casse)
   - `category` (correspondance partielle)
   - `q` (recherche mot-clé dans `content` ou `keywords`).

### TÂCHE 3 : Implémentation API (GET)
**Objectif :** Exposer les données via HTTP.
1. Définir les `Routes` ZIO-Http.
2. Implémenter `GET /indices` :
   - Récupérer les query params (`req.url.queryParams`).
   - Appliquer le filtrage de la Tâche 2.
   - Retourner le JSON.
3. Implémenter `GET /indices/random` :
   - Retourner un élément aléatoire de la liste.
   - Gérer le cas où la liste est vide.

### TÂCHE 4 : Implémentation API (POST)
**Objectif :** Permettre l'ajout de connaissances.
1. Implémenter `POST /indices`.
2. Décoder le Body (`req.body.asString.map(_.fromJson[GeoIndice])`).
3. Gérer les erreurs de parsing (400 Bad Request).
4. Ajouter le nouvel indice dans le `Ref` (atomiquement).
5. Retourner un message de succès (201 Created).

### TÂCHE 5 : Tests Unitaires
**Objectif :** Valider le fonctionnement avant le rendu.
1. Créer `src/test/scala/GeoGrimoireSpec.scala`.
2. Écrire un test pour le filtrage (Vérifier qu'on filtre bien par Région).
3. Écrire un test pour le POST (Ajouter un élément et vérifier qu'il est présent au GET suivant).
