# Architecture diagram (import to draw.io)

File `architecture.drawio` is an XML diagram export ready to import into draw.io (diagrams.net). It describes the SketchNote backend components found in the repository's Docker Compose files.

Quick notes about components shown:
- API Gateway: exposes external endpoints to clients (port 8888 in docker-compose).
- Eureka: service registry (port 8761).
- Keycloak: identity/auth server (port 8090), connected to Postgres via PgBouncer.
- PgBouncer: Postgres connection pooler (port 6432).
- Postgres: single Postgres instance hosting multiple DBs (learningdb, ordersdb, keycloak, ...).
- Kafka: message broker used by multiple microservices.
- Redis: cache used by multiple services.
- Microservices: identity, payment, order, project, blog, learning, interaction — each with configured ports in `docker-compose-services.yml`.

How to import into draw.io (diagrams.net):
1. Open https://app.diagrams.net/ (or the desktop app).
2. File -> Import From -> Device and choose `architecture.drawio` from this repository.
3. The diagram will load. You can move nodes, change colors, and add labels.

Suggested edits after import:
- Add arrows and annotations for external integrations (e.g., third-party payment gateway) if you use them.
- Group microservices inside a container or box named "Microservices" for clarity.
- Add network boundaries (public vs internal) if needed.

If you want a different layout (left-to-right, swimlanes, or more details per service like DB/schema), tell me which layout and I'll produce a revised `.drawio` file.
