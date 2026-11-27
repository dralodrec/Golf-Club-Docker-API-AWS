# Golf Club Membership & Tournament REST API

**Java 27 · Spring Boot 4.2 · MySQL 8.4 · Spring Data JPA/Hibernate · Docker Compose · Amazon RDS · JUnit 5/Mockito · GitHub Actions**

A backend REST API for managing golf club members, creating tournaments, searching records, and registering members for tournaments. The project demonstrates layered Java application design, relational database modeling, local and containerized execution, cloud database integration, automated unit tests, and a pull-request-based continuous integration workflow.

**Repository:** [Golf-Club-Docker-API-AWS](https://github.com/dralodrec/Golf-Club-Docker-API-AWS)

> **Deployment scope:** The Spring Boot API was run locally (Ubuntu) and inside Docker (Windows 11). **Amazon RDS hosts the MySQL database**, not the Java application. Cloud database connectivity and persistence were verified from the locally running API. This is a course/portfolio project, not a production-hosted web service.

---

## Contents

1. [Project objectives and features](#project-objectives-and-features)
2. [Technology stack](#technology-stack)
3. [Architecture and request flow](#architecture-and-request-flow)
4. [Project structure](#project-structure)
5. [Data model and relationships](#data-model-and-relationships)
6. [Quick start: Docker Compose](#quick-start-docker-compose)
7. [Run locally without Docker](#run-locally-without-docker)
8. [API reference](#api-reference)
9. [Testing with Postman](#testing-with-postman)
10. [Automated tests with JUnit and Mockito](#automated-tests-with-junit-and-mockito)
11. [Continuous integration with GitHub Actions](#continuous-integration-with-github-actions)
12. [Amazon RDS MySQL integration](#amazon-rds-mysql-integration)
13. [Verification evidence](#verification-evidence)
14. [Troubleshooting](#troubleshooting)
15. [Security and responsible cloud usage](#security-and-responsible-cloud-usage)
16. [Current limitations and future work](#current-limitations-and-future-work)
17. [Development workflow and lessons learned](#development-workflow-and-lessons-learned)

---

## Project objectives and features

The application supports:

- **Member management:** register members, retrieve all members, and retrieve a member by ID.
- **Membership data:** name, address, email, phone, membership start date, and type (`ANNUAL`, `MONTHLY`, `LIFETIME`).
- **Tournament management:** create tournaments and retrieve tournament records with dates, location, entry fee, and cash prize amount.
- **Six search operations:** member by name, membership type, phone, or tournament start date; tournament by start date or location.
- **Tournament registration:** associate an existing member with an existing tournament; retrieve tournament participants.
- **Business rules:** reject a tournament ending before it starts; prevent duplicate member registration in the same tournament.
- **HTTP error handling:** meaningful responses for missing records, invalid requests, and registration conflicts.
- **Persistence:** MySQL through Spring Data JPA and Hibernate; a join table enforces tournament/member relationships.
- **Execution environments:** local Ubuntu/MySQL; Docker Desktop with API + MySQL containers; Ubuntu API connected securely to Amazon RDS MySQL.
- **Quality controls:** JUnit/Mockito service tests and GitHub Actions Maven verification on pull requests.

The project provides an HTTP API tested using Postman; it does **not** include a browser-based frontend.

## Technology stack

| Area | Technology | Role |
| --- | --- | --- |
| Language | Java 27 | Backend application |
| Framework | Spring Boot 4.2.0 | Web application and dependency configuration |
| HTTP API | Spring Web MVC | REST controllers and response handling |
| Persistence | Spring Data JPA and Hibernate | Entities, repositories, relational mapping |
| Database | MySQL 8.4 | Local, Docker, and Amazon RDS persistence |
| Database driver | MySQL Connector/J | JDBC connections, including TLS to RDS |
| Build | Apache Maven | Dependency resolution, packaging, tests |
| Validation | Jakarta Bean Validation | Request validation |
| Containers | Docker + Docker Compose | Multi-container API and MySQL deployment |
| Cloud database | Amazon RDS for MySQL | Managed database instance in `ca-central-1` |
| API client | Postman | Manual endpoint and error-case testing |
| Automated testing | JUnit Jupiter + Mockito | Unit tests without a running database |
| CI | GitHub Actions | Maven build/test checks on pull requests |
| Source control | Git + GitHub | Feature branches, commits, pull requests |
| IDEs | IntelliJ IDEA / VS Code | Development and configuration |

The versions above reflect the project's Maven configuration and the environments exercised during development. Other compatible versions may require adjustments.

## Architecture and request flow

```text
Postman / HTTP client
         |
         v
Spring MVC REST Controllers
(MemberController, TournamentController)
         |
         v
Service layer (business rules, transactions)
(MemberService, TournamentService)
         |
         v
Spring Data JPA repositories
         |
         v
Hibernate ORM / MySQL Connector/J
         |
         v
MySQL database
  | local Ubuntu MySQL
  | Docker MySQL 8.4
  + Amazon RDS MySQL 8.4 (JDBC/TLS)
```

**Responsibilities:**

- **Controller:** maps HTTP methods and paths, receives validated request DTOs, returns response DTOs and HTTP status codes.
- **Service:** applies business rules and coordinates repository operations.
- **Repository:** uses Spring Data JPA to query and persist entities.
- **Entity/model:** maps Java state and relationships to relational tables.
- **Exception handler:** translates service exceptions to HTTP responses.

For example, registering a tournament participant follows:

```text
POST /api/tournaments/{id}/members/{memberId}
    -> TournamentController
    -> TournamentService (check IDs and duplicate registration)
    -> MemberRepository + TournamentRepository
    -> Hibernate many-to-many association
    -> MySQL tournament_members
    -> HTTP 204 No Content
```

## Project structure

```text
Golf-Club-Docker-API-AWS/
├── .github/
│   └── workflows/
│       └── java-ci.yml
├── src/
│   ├── main/
│   │   ├── java/com/golfclub/api/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-aws.properties
│   └── test/java/com/golfclub/api/service/
│       ├── MemberServiceTest.java
│       └── TournamentServiceTest.java
├── docs/
│   └── screenshots/
│       ├── api/
│       ├── database/
│       ├── error-handling/
│       ├── docker/
│       └── aws/
├── .dockerignore
├── .env.example
├── .gitignore
├── Dockerfile
├── compose.yaml
├── pom.xml
└── README.md
```

The private `.env` and `.env.aws` files are intentionally **not** in the repository. Test and evidence folders may be extended as additional verification is added.

## Data model and relationships

The schema contains three tables:

| Table | Key fields | Purpose |
| --- | --- | --- |
| `members` | `id` (PK), name, email (unique), address, phone, start date, membership type | Store golf club members |
| `tournaments` | `id` (PK), start/end dates, location, entry fee, cash prize amount | Store tournaments |
| `tournament_members` | `tournament_id` + `member_id` (composite PK, two FKs) | Register members in tournaments |

```text
members                 tournament_members             tournaments
-------                 ------------------             -----------
id (PK)  <-------------  member_id (PK, FK)             id (PK)
                        tournament_id (PK, FK)  ---->  id (PK)
```

The association is **many-to-many**: a member may participate in multiple tournaments; a tournament may have multiple members. Hibernate maps this association using `@ManyToMany` and `@JoinTable(name = "tournament_members")`.

The composite primary key prevents a duplicated `(tournament_id, member_id)` pair at the database level. The service also checks for an existing registration and returns a conflict response rather than attempting to insert a duplicate.

**Verify the schema in MySQL Workbench:**

```sql
USE golf_club_db;
SHOW TABLES;
SHOW CREATE TABLE tournament_members;
SELECT * FROM tournament_members;
```

For a focused foreign-key check:

```sql
SELECT COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'golf_club_db'
  AND TABLE_NAME = 'tournament_members'
  AND REFERENCED_TABLE_NAME IS NOT NULL;
```

Expected references: `member_id -> members.id` and `tournament_id -> tournaments.id`.

## Quick start: Docker Compose

**Recommended for reproducing the API without installing Java, Maven, or MySQL directly.** Install Git and Docker Desktop (or Docker Engine + Compose plugin), with the Docker engine running in **Linux containers** mode.

### 1. Clone the repository

```bash
git clone https://github.com/dralodrec/Golf-Club-Docker-API-AWS.git
cd Golf-Club-Docker-API-AWS
```

### 2. Create local Compose credentials

Copy the sample configuration:

**Linux/macOS:**

```bash
cp .env.example .env
```

**Windows PowerShell:**

```powershell
Copy-Item .env.example .env
```

For Docker Compose, make sure the **project-root** `.env` contains at least:

```dotenv
DB_USERNAME=golfclub_app
DB_PASSWORD=CHOOSE_A_STRONG_LOCAL_APP_PASSWORD
MYSQL_ROOT_PASSWORD=CHOOSE_A_DIFFERENT_STRONG_ROOT_PASSWORD
```

Any `DB_HOST`, `DB_PORT`, and `DB_NAME` entries in `.env` are ignored by the API service in this Compose file because Compose explicitly sets those values. Inside the container network, `DB_HOST=db` refers to the MySQL service. Use real locally chosen passwords; **do not commit `.env`**.

### 3. Build and run

```bash
docker compose up --build -d
docker compose ps
```

The Compose file builds the Spring Boot image, starts MySQL 8.4, waits for its health check, and starts the API. The API listens at **http://localhost:8080** and is bound to the host loopback interface.

```bash
docker compose logs --tail=80 api
```

Look for startup messages such as `HikariPool-1 - Start completed` and `Started GolfClubApiApplication`.

### 4. Verify

```bash
curl http://localhost:8080/api/members
```

Expected: HTTP **200 OK** and a JSON array (`[]` for a new database).

### 5. Stop the environment

```bash
docker compose down
```

This preserves the **named volume** containing MySQL data. **Do not run `docker compose down -v` unless you deliberately want to delete that database volume.**

If you use a custom project name (as in the Windows verification), use the same name for all commands:

```bash
docker compose -p golfclub-qap4-v2 up --build -d
docker compose -p golfclub-qap4-v2 ps
docker compose -p golfclub-qap4-v2 logs --tail=80 api
```

**Note:** Docker's database is independent of the Ubuntu local database and Amazon RDS. Existing records do not automatically move between environments.

## Run locally without Docker

### Prerequisites

- JDK **27** and Apache Maven installed (`java -version`, `mvn -version`).
- MySQL 8.x running and reachable on port `3306`.
- A database named `golf_club_db` and a MySQL user with permissions to manage its application tables.

### Prepare MySQL

Connect to your local MySQL server using an administrative account:

```sql
CREATE DATABASE IF NOT EXISTS golf_club_db;
```

Create a dedicated application user through MySQL Workbench **Users and Privileges** or another administrative interface. Grant it `SELECT`, `INSERT`, `UPDATE`, `DELETE`, `CREATE`, `ALTER`, `INDEX`, and `REFERENCES` on `golf_club_db` for development. Do not use the MySQL root account as your application login.

### Configure local variables

Create a project-root `.env` file with your **local** database details:

```dotenv
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=golf_club_db
DB_USERNAME=golfclub_app
DB_PASSWORD=YOUR_LOCAL_DATABASE_PASSWORD
```

`application.properties` imports the `.env` file optionally and maps these variables to the JDBC URL, username, and password. The file is ignored by Git.

### Launch the API

```bash
mvn clean test
mvn spring-boot:run
```

Then request `http://localhost:8080/api/members` via Postman or `curl`.

Development uses `spring.jpa.hibernate.ddl-auto=update`, which can create/update tables for a demonstration. For production, use explicit, versioned database migrations rather than relying on Hibernate to change schemas at runtime.

## API reference

**Base URL (local or Docker):** `http://localhost:8080`  
**Content type for POST:** `application/json`

### Member endpoints

| HTTP | Endpoint | Description | Typical success |
| --- | --- | --- | --- |
| `POST` | `/api/members` | Create member | `201 Created` |
| `GET` | `/api/members` | List members | `200 OK` |
| `GET` | `/api/members/{id}` | Get member by ID | `200 OK` |
| `GET` | `/api/members/search/name?name=John` | Partial, case-insensitive name search | `200 OK` |
| `GET` | `/api/members/search/type?type=ANNUAL` | Filter membership type | `200 OK` |
| `GET` | `/api/members/search/phone?phone=709` | Partial phone search | `200 OK` |
| `GET` | `/api/members/search/tournament-date?startDate=2026-12-10` | Members in tournaments on a given start date | `200 OK` |

Valid membership types: `ANNUAL`, `MONTHLY`, `LIFETIME`.

### Tournament endpoints

| HTTP | Endpoint | Description | Typical success |
| --- | --- | --- | --- |
| `POST` | `/api/tournaments` | Create tournament | `201 Created` |
| `GET` | `/api/tournaments` | List tournaments | `200 OK` |
| `GET` | `/api/tournaments/{id}` | Get tournament by ID | `200 OK` |
| `GET` | `/api/tournaments/search/date?startDate=2026-12-10` | Search by start date | `200 OK` |
| `GET` | `/api/tournaments/search/location?location=St.` | Partial, case-insensitive location search | `200 OK` |
| `POST` | `/api/tournaments/{id}/members/{memberId}` | Register existing member | `204 No Content` |
| `GET` | `/api/tournaments/{id}/members` | List registered participants | `200 OK` |

**There are no PUT or DELETE endpoints in the current implementation.** Do not send POST to `/api/members/{id}`; that path is for GET requests and may produce `405 Method Not Allowed`.

### Example: create a member

`POST /api/members` with `Body -> raw -> JSON` in Postman:

```json
{
  "memberName": "Example Member",
  "memberAddress": "St. John's, NL",
  "memberEmailAddress": "example.member@example.com",
  "memberPhoneNumber": "7095550101",
  "membershipStartDate": "2026-11-27",
  "membershipType": "ANNUAL"
}
```

Expected: `201 Created`, a response containing the assigned `id`, and a `Location` header referencing the new member.

The email field is required, validated for email format, and unique in the entity mapping. Use a distinct test email when creating more than one member.

### Example: create a tournament

`POST /api/tournaments`:

```json
{
  "startDate": "2026-12-10",
  "endDate": "2026-12-11",
  "location": "St. John's Golf Club",
  "entryFee": 50.00,
  "cashPrizeAmount": 500.00
}
```

Expected: `201 Created` with a generated tournament `id`. The service rejects an `endDate` preceding `startDate`.

### Example: register a member for a tournament

After creating a member and tournament, use their **actual returned IDs**:

```http
POST /api/tournaments/1/members/1
```

No JSON request body is required. Expected: `204 No Content`. To confirm:

```http
GET /api/tournaments/1/members
```

Expected: `200 OK` and a list of registered members. A repeated registration returns `409 Conflict`.

### Error responses

| Scenario | Expected HTTP status | Explanation |
| --- | --- | --- |
| Member ID does not exist | `404 Not Found` | Member service raises a missing-record exception |
| Tournament ID does not exist | `404 Not Found` | Tournament service raises a missing-record exception |
| Invalid membership enum (e.g., `INVALID`) | `400 Bad Request` | Request cannot bind to the enum |
| Tournament end date before start date | `400 Bad Request` | Business rule rejects invalid dates |
| Duplicate tournament registration | `409 Conflict` | Business rule prevents duplicates |
| Unsupported method on an endpoint | `405 Method Not Allowed` | Wrong HTTP verb for a mapped path |

Some exception handlers return JSON shaped like `{"error":"..."}`. Framework-generated validation errors may use a different structure.

## Testing with Postman

For each request, record the method, URL, request body if applicable, HTTP status, and response JSON. The manual testing sequence used in this project was:

1. Create a member; retrieve it and list all members.
2. Create a tournament; retrieve it and list all tournaments.
3. Register a member; list participants; inspect `tournament_members` in MySQL.
4. Repeat the registration and verify `409 Conflict`.
5. Exercise all six search endpoints with sample data and verify `200 OK`.
6. Test nonexistent member and tournament IDs (`404`) and invalid membership type (`400`).
7. Repeat representative POST and GET requests while connected to **Amazon RDS**, then verify persistence using MySQL Workbench.

**Important:** A `200 OK` from `localhost:8080` alone does not prove that AWS was used. Confirm the **AWS Spring profile/JDBC URL** in the startup logs and query the matching AWS RDS database to establish the full chain.

## Automated tests with JUnit and Mockito

The repository has **seven service-level unit tests** across two classes:

| Class | Test coverage |
| --- | --- |
| `MemberServiceTest` | save member; retrieve by ID; missing member; search by name |
| `TournamentServiceTest` | reject invalid tournament dates; register member; reject duplicate registration |

Mockito simulates repository dependencies so these tests can run without a MySQL instance or AWS account. They verify **service behavior**, not end-to-end database connectivity.

Execute locally:

```bash
mvn clean test
```

Or run the entire Maven verification lifecycle:

```bash
mvn --batch-mode --no-transfer-progress clean verify
```

Inspect test reports under `target/surefire-reports/`. The expected condition is **zero failures and zero errors**. The Dockerfile's image-build stage currently uses `-DskipTests`; GitHub Actions executes tests separately.

## Continuous integration with GitHub Actions

Workflow: [`.github/workflows/java-ci.yml`](.github/workflows/java-ci.yml)

The `Golf Club API - Java CI` workflow:

- Runs on pull requests targeting `main` and pushes to the configured branches.
- Checks out the repository on an Ubuntu runner.
- Configures Temurin Java 27 and Maven dependency caching.
- Runs `mvn --batch-mode --no-transfer-progress clean verify`.
- Reports a `build-and-test` status check to the Pull Request.

**Development process:** create a feature branch -> implement changes -> run local tests -> commit/push -> open PR into `main` -> review CI check -> merge.

[View workflow runs](https://github.com/dralodrec/Golf-Club-Docker-API-AWS/actions/workflows/java-ci.yml)

The CI workflow does **not** deploy the API to AWS and does **not** run live RDS integration tests. Only claim those capabilities if they are added later. A repository ruleset can require the `build-and-test` check before merge; confirm the rule remains active in GitHub settings.

## Amazon RDS MySQL integration

### Scope

A MySQL 8.4 database instance (`golf-club-rds`) was provisioned in **Canada (Central), `ca-central-1`**. The Spring Boot API ran on Ubuntu, connected over JDBC/TLS, and persisted members, tournaments, and tournament registrations in RDS.

### Secure connectivity

- Use an RDS database instance with a database named `golf_club_db`.
- Create an application account `golfclub_app` rather than using the RDS administrator from Spring Boot.
- Limit the account to its application schema. Initial Hibernate schema creation requires DDL-related permissions, including `REFERENCES` on parent tables.
- Prefer a **private** RDS instance with access through a controlled network path. If temporarily enabling public accessibility for a demonstration, allow TCP `3306` from **only the authorized client public IP (`/32`)**, not `0.0.0.0/0`.
- Require TLS. The AWS JDBC profile uses `sslMode=VERIFY_IDENTITY` with a trusted RDS certificate store to verify the server certificate and hostname.
- **Do not commit** database credentials, truststore passwords, local `.env.aws`, or private key material.

### Configure the AWS Spring profile

The repository includes [`src/main/resources/application-aws.properties`](src/main/resources/application-aws.properties), which reads the following locally defined variables from `.env.aws`:

```dotenv
AWS_DB_HOST=YOUR_RDS_ENDPOINT.rds.amazonaws.com
AWS_DB_PORT=3306
AWS_DB_NAME=golf_club_db
AWS_DB_USERNAME=golfclub_app
AWS_DB_PASSWORD=YOUR_OWN_RDS_APPLICATION_PASSWORD
AWS_TRUSTSTORE_PATH=/absolute/path/to/rds-truststore.p12
AWS_TRUSTSTORE_PASSWORD=YOUR_OWN_TRUSTSTORE_PASSWORD
```

Use the **exact endpoint generated by your own AWS account**, not the example string. `.env.aws` is excluded by `.gitignore`.

The Java truststore should contain the trusted CA certificates from the appropriate AWS RDS regional CA bundle. For `ca-central-1`, AWS publishes the regional bundle at:

```text
https://truststore.pki.rds.amazonaws.com/ca-central-1/ca-central-1-bundle.pem
```

Download the public certificate bundle, import its CA certificates into a local PKCS12 truststore using Java `keytool`, and set `AWS_TRUSTSTORE_PATH` and `AWS_TRUSTSTORE_PASSWORD` to match. **Do not put the truststore password or RDS application password in committed files.**

Run Spring Boot from the repository root with the AWS profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=aws
```

Verify the application is using the RDS JDBC hostname and reaches a successful startup. Hibernate currently uses `ddl-auto=update` to create/update the demonstration schema. It created `members`, `tournaments`, and `tournament_members`; foreign-key constraints were verified separately.

### Verify RDS persistence

Using a **Workbench connection to RDS**, not the local database:

```sql
USE golf_club_db;
SHOW TABLES;
SHOW CREATE TABLE tournament_members;
SELECT * FROM members;
SELECT * FROM tournaments;
SELECT * FROM tournament_members;
```

Cloud verification demonstrated:

| Operation | Observed result |
| --- | --- |
| `POST /api/members` | `201 Created` |
| `GET /api/members` | `200 OK` |
| AWS query of `members` | Newly created member persisted |
| `POST /api/tournaments` | `201 Created` |
| `POST /api/tournaments/1/members/1` | `204 No Content` |
| AWS query of `tournament_members` | Row linking tournament 1 to member 1 |
| Foreign-key metadata query | Two references to parent member and tournament tables |

Actual IDs vary based on existing data. The application and cloud tests used dummy data rather than real personal member records.

### AWS resource costs

RDS, public IPv4 addresses, storage, backups, and related AWS features **may incur charges**. Review account-specific Free Tier/credit eligibility and billing alerts. A database can accrue costs while idle. Remove demonstration resources when no longer needed, after preserving any necessary data, and disable public access when external testing is finished.

## Verification evidence

Screenshots are stored in `docs/screenshots/`. They are project evidence, **not a substitute for reproducible commands or automated tests**. Some screenshots show example records or AWS configuration; redact secrets and unnecessary account identifiers before publishing.

### API and database (Ubuntu)

- [Member POST](docs/screenshots/api/01-member-post.png) and [Member GET](docs/screenshots/api/02-member-get.png)
- [Tournament POST](docs/screenshots/api/03-tournament-post.png) and [Tournament GET](docs/screenshots/api/04-tournament-get.png)
- [Register participant](docs/screenshots/api/05-member-registration.png) and [List participants](docs/screenshots/api/06-tournament-participants.png)
- [Database tables](docs/screenshots/database/01-database-tables.png), [foreign-key relationships](docs/screenshots/database/02-database-relationships.png), and [registration records](docs/screenshots/database/03-tournament-members.png)
- Error cases: [missing member](docs/screenshots/error-handling/01-member-not-found.png), [missing tournament](docs/screenshots/error-handling/02-tournament-not-found.png), [invalid membership](docs/screenshots/error-handling/03-invalid-membership.png), [duplicate registration](docs/screenshots/error-handling/04-duplicate-registration.png)

The six search endpoints are documented in the API reference; related Postman screenshots are in `docs/screenshots/api/`.

### Docker (Windows 11)

| Evidence | Screenshot |
| --- | --- |
| Running API and MySQL containers | [01-docker-containers.png](docs/screenshots/docker/01-docker-containers.png) |
| `docker compose ps` | [02-docker-compose-ps.png](docs/screenshots/docker/02-docker-compose-ps.png) |
| Spring Boot container startup logs | [03-docker-api-logs.png](docs/screenshots/docker/03-docker-api-logs.png) |
| GET members: `200 OK` | [04-docker-get-members.png](docs/screenshots/docker/04-docker-get-members.png) |
| POST member: `201 Created` | [05-docker-post-member.png](docs/screenshots/docker/05-docker-post-member.png) |
| GET saved member | [06-docker-get-saved-member.png](docs/screenshots/docker/06-docker-get-saved-member.png) |

### AWS RDS (Ubuntu client)

| Evidence | Screenshot |
| --- | --- |
| RDS MySQL instance | [01-rds-instance.png](docs/screenshots/aws/01-rds-instance.png) |
| Instance configuration | [02-rds-configuration.png](docs/screenshots/aws/02-rds-configuration.png) |
| Connectivity and security group | [03-rds-connectivity.png](docs/screenshots/aws/03-rds-connectivity.png) |
| Successful Workbench connection | [04-mysql-workbench-rds.png](docs/screenshots/aws/04-mysql-workbench-rds.png) |
| Database/schema creation | [05-rds-database-schema.png](docs/screenshots/aws/05-rds-database-schema.png) |
| RDS server verification | [06-rds-server-verification.png](docs/screenshots/aws/06-rds-server-verification.png) |
| Active database selection | [07-rds-schema-selected.png](docs/screenshots/aws/07-rds-schema-selected.png) |
| Encrypted TLS session | [08-rds-tls-verification.png](docs/screenshots/aws/08-rds-tls-verification.png) |
| Hibernate-generated tables | [09-rds-hibernate-tables.png](docs/screenshots/aws/09-rds-hibernate-tables.png) |
| Verified foreign keys | [10-rds-foreign-key-relationships.png](docs/screenshots/aws/10-rds-foreign-key-relationships.png) |
| Member POST `201` | [11-aws-postman-member-post.png](docs/screenshots/aws/11-aws-postman-member-post.png) |
| Member GET `200` | [12-aws-postman-member-get.png](docs/screenshots/aws/12-aws-postman-member-get.png) |
| Member record in RDS | [13-aws-rds-member-persistence.png](docs/screenshots/aws/13-aws-rds-member-persistence.png) |
| Tournament POST `201` | [14-aws-tournament-post.png](docs/screenshots/aws/14-aws-tournament-post.png) |
| Tournament registration `204` | [15-aws-tournament-registration.png](docs/screenshots/aws/15-aws-tournament-registration.png) |
| Registration row in RDS | [16-aws-tournament-persistence.png](docs/screenshots/aws/16-aws-tournament-persistence.png) |

> GitHub only displays an image link if its file exists at the specified path and filename. Verify the links on the documentation Pull Request's **Files changed** view; rename or remove any references that differ from your actual screenshot filenames.

## Troubleshooting

| Problem | Cause / resolution |
| --- | --- |
| `docker ... dockerDesktopLinuxEngine` pipe not found | Start Docker Desktop, check Linux engine/WSL 2, retry. |
| Docker API container exits: `.env` file missing | Use optional Spring config import and Compose environment variables; rebuild image. |
| Docker MySQL `Access denied` | Database volume may retain credentials from initial initialization. Restore the original credentials or deliberately create a fresh, isolated Compose project. Avoid deleting a volume containing important data. |
| `405 Method Not Allowed` | Confirm method and route; e.g., POST to `/api/members`, not `/api/members/1`. |
| `409 Conflict` on participant POST | Member is already registered for the specified tournament. |
| RDS Workbench fails hostname resolution | Use actual RDS-generated endpoint, not `AWS RDS endpoint` placeholder. |
| RDS network connection fails | Check DB status, VPC route/public setting or private connection path, security group TCP `3306` rule, and client public IP. |
| RDS MySQL authentication denied | Check username (`golfclub_admin` versus `golfclub_app`) and the corresponding password. |
| TLS/truststore error | Confirm PKCS12 file exists, matching truststore password, trusted AWS CA, and RDS hostname. |
| Spring Boot starts but foreign keys absent | Inspect Hibernate DDL warnings. During development, MySQL denied `REFERENCES` on parent tables; explicit grants and restart resolved this. |
| JUnit build fails | Read the first compiler/test exception and `target/surefire-reports/` before making changes. |
| `git push` says `src refspec ... does not match any` | Check `git branch --show-current`; create the intended branch at the current commit and review `origin/main..HEAD` before pushing. |

## Security and responsible cloud usage

- Keep `.env`, `.env.aws`, actual passwords, token files, and truststore secrets out of Git. The `.env.example` file must contain **placeholders only**, never usable credentials.
- If a password has appeared in a screenshot or commit, **rotate it**. Removing the file from a later commit does not erase it from Git history.
- Inspect screenshots before uploading: redact passwords, secret strings, tokens, and unnecessary account identifiers.
- Prefer private RDS networking; do not leave public access enabled indefinitely. Restrict temporary MySQL ingress to the authorized client IP on TCP `3306`.
- Grant database users only the permissions they need. Schema migration rights should be separated from application runtime rights for production deployments.
- Use verified TLS to RDS (`VERIFY_IDENTITY`); TLS cipher output alone proves encryption, not hostname identity verification.
- The project uses `ddl-auto=update` for demonstrations. Move to Flyway/Liquibase migrations, lower-privilege runtime users, and production-oriented configuration before real deployment.
- Do not use actual personal information in demo test records.

## Current limitations and future work

The current project provides a functional **backend demonstration**. It does not yet include:

- Browser-based UI or authentication/authorization for public use.
- PUT/PATCH/DELETE API operations.
- End-to-end automated integration tests against a disposable MySQL environment.
- Automatic deployment to an AWS compute service; only the **database** is in AWS.
- Production-ready secrets management, schema migrations, monitoring, and rate limiting.

Potential enhancements include Testcontainers-based integration tests, an OpenAPI specification, database migration scripts, security controls, more comprehensive validation, and a separate deployment pipeline.

## Development workflow and lessons learned

The project was completed incrementally:

1. Initialized the Maven/Spring Boot project and modeled `Member` and `Tournament` entities.
2. Implemented repositories, service business rules, DTO validation, controllers, and error handling.
3. Connected to local MySQL; inspected schema generation, foreign keys, and persisted records.
4. Exercised REST endpoints and negative cases with Postman.
5. Added Dockerfile/Compose and verified the API and database containers on Windows 11.
6. Added JUnit/Mockito tests and GitHub Actions checks, using feature branches and pull requests.
7. Provisioned Amazon RDS MySQL, configured scoped credentials and verified TLS, corrected DDL permissions, and verified cloud persistence.
8. Collected screenshots and merged the implementation through pull requests into `main`.

**Key takeaways:** clean separation between API, business logic, and persistence; relational integrity through explicit foreign keys; environment-specific configuration; the distinction between build success and runtime success; interpreting JDBC/Hibernate errors; and using CI checks to support reproducible changes.

---

**Maintainer:** [dralodrec](https://github.com/dralodrec)  
**Purpose:** Software development coursework and portfolio demonstration.
