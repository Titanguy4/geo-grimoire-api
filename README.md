# Geo Grimoire API

Scala 3 / ZIO HTTP service exposing geographic tips (“indices”) stored in PostgreSQL via Quill.

## Stack

- Scala 3.3.1, sbt
- ZIO 2, ZIO HTTP 3.0.0-RC4, ZIO JSON
- Quill JDBC ZIO 4.8.1, PostgreSQL, HikariCP
- ZIO Test

## Prerequisites

- JDK 17+

## Setup

1. Run docker compose for initialize the database
2. Fetch dependencies and compile: `sbt clean compile`.

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
