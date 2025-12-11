# 🤖 Instructions pour l'Assistant IA - Version 2 (Persistence & UI)

## CONTEXTE

L'API GeoGuessr fonctionne actuellement en mémoire (`Ref`).
Nous devons la migrer vers une base de données **PostgreSQL** en utilisant **ZIO-Quill**, ajouter un **Seeder** pour les données initiales, et créer une interface **Web (Frontend)** simple servie par l'API elle-même.

## STACK TECHNIQUE MISE À JOUR

- **DB Access :** ZIO-Quill (JDBC)
- **Database :** PostgreSQL (via Docker)
- **Frontend :** HTML/JS natif (servi par ZIO-Http comme ressource statique ou String)

---

## 📅 TASKS V2 (Feuille de route)

### TÂCHE 1 : Configuration ZIO-Quill & Datasource

**Objectif :** Connecter l'application à la base de données définie dans `docker-compose.yml`.

1. Créer un objet `DatabaseLayer`.
2. Configurer le `Quill.Postgres.fromNamingStrategy(SnakeCase)`
3. Configurer le `DataSourceLayer` avec les identifiants :
   - User: `geouser`
   - Password: `geopassword`
   - Url: `jdbc:postgresql://localhost:5432/geogrimoire`
   - Class: `org.postgresql.Driver`

### TÂCHE 2 : Migration du Modèle vers SQL

**Objectif :** Créer la table et adapter le code.

1. Dans `Main.scala` (ou un fichier `Repository.scala`), écrire une fonction `createTable` qui exécute la requête SQL :
   `CREATE TABLE IF NOT EXISTS geo_indice (country VARCHAR(255), region VARCHAR(255), category VARCHAR(255), content TEXT, keywords TEXT);`
   _(Note: Pour simplifier, on stockera les keywords sous forme de string séparée par des virgules)._
2. Remplacer la logique `Ref` par des requêtes Quill (`run(query[GeoIndice]...)`) pour :
   - `insert`
   - `filter` (avec les critères dynamiques)

### TÂCHE 3 : Le Seeder (Initialisation)

**Objectif :** Remplir la base au démarrage si elle est vide.

1. Créer un service `Seeder`.
2. Logique :
   - Vérifier si la table `geo_indice` est vide (`count == 0`).
   - Si oui, insérer la liste `initialData` (que nous avions en V1).
   - Si non, ne rien faire.
3. Intégrer ce `Seeder` dans le `run` principal de l'application (juste après la création de table).

### TÂCHE 4 : Le Frontend Basique (Single Page)

**Objectif :** Une interface pour visualiser et ajouter des indices sans utiliser Postman.

1. Créer une nouvelle route `GET /ui` dans ZIO-Http.
2. Cette route doit renvoyer une réponse `Response.html(...)`.
3. Le HTML doit contenir :
   - Un **Tableau** HTML qui liste les indices (chargés via un `fetch('/indices')` en JS au chargement de la page).
   - Un **Formulaire** simple (Pays, Région, Catégorie, Contenu) qui fait un `fetch('/indices', { method: 'POST' ... })` lors de la soumission.
   - Du CSS minimaliste (utiliser une librairie CDN comme Tailwind ou simple CSS style) pour que ce soit lisible.

### TÂCHE 5 : Assemblage Final

**Objectif :** Tout faire marcher ensemble.

1. Mettre à jour le `Main.scala` pour composer les layers : `app.provide(DatabaseLayer, Server.default)`.
2. S'assurer que l'ordre est : Création Table -> Seeder -> Lancement Serveur HTTP.
