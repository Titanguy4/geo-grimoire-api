# Geo Grimoire API

Scala 3 / ZIO HTTP service exposing geographic tips (“indices”) stored in PostgreSQL via Quill.

## Stack

- Scala 3.3.1, sbt
- ZIO 2, ZIO HTTP 3.0.0-RC4, ZIO JSON
- Quill JDBC ZIO 4.8.1, PostgreSQL, HikariCP
- ZIO Test

## Prerequisites

- JDK 17+
- PostgreSQL reachable (Docker Compose provided)

## Setup

1. Start the DB (Docker Compose): `docker compose up -d`
2. Install dependencies and compile: `sbt clean compile`
3. (Optional) Generate Bloop files for faster IDE builds: `sbt bloopInstall`

## Run

- Start the API: `sbt run` (HTTP server on port 8080).
- On startup the app creates tables and seeds initial data when needed.
- Example endpoints:
  - GET /health
  - GET /indices (filters supported)
  - GET /indices/random
  - POST /indices
  - GET /index.html

## Tests

- Run the suite: `sbt test`.

## Notes

- Bloop is optional. If you do not use it, you can ignore step 3 above; sbt alone is enough.
