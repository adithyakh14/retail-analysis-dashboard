# Retail Analysis Dashboard

Retail Analysis Dashboard is a secured Spring Boot application that serves a protected browser dashboard and its backing REST APIs from the same runtime. It reads a CSV dataset, builds an in-memory warehouse-style model, generates report exports, and exposes typed endpoints behind authenticated access.

## What The Project Does

- Loads retail transactions from `src/main/resources/retail.csv`
- Converts the raw rows into fact and dimension structures
- Calculates KPIs, trend summaries, contribution tables, and insight cards
- Exposes dashboard data through REST endpoints
- Serves a protected dashboard UI from the backend at `/dashboard/`
- Generates downloadable CSV outputs for raw and transformed data

## Architecture

This project has one supported runtime: a Spring Boot application started from `RetailProjectApplication`.

The end-to-end flow is:

`retail.csv` -> `RetailRecord` objects -> warehouse model -> filtered aggregations -> secured REST API -> protected dashboard UI

### Runtime Layers

- `src/main/java/com/retailproject/RetailProjectApplication.java`
  Spring Boot entry point.
- `src/main/java/com/retailproject/web/DashboardController.java`
  HTTP layer for dashboard, filter, table, ask, and download endpoints.
- `src/main/java/com/retailproject/DashboardDataService.java`
  Core application service. Loads data once, builds analytics, writes CSV outputs, and answers API requests.
- `src/main/java/com/retailproject/security/`
  Authentication, lockout, bearer-token, and audit components.
- `src/main/java/com/retailproject/config/`
  Security and OpenAPI configuration, plus typed application properties.
- `src/main/java/com/retailproject/RetailDataReader.java`
  Reads the source CSV into domain records.
- `src/main/java/com/retailproject/RetailWarehouseBuilder.java`
  Builds the warehouse-style fact and dimension model.
- `src/main/java/com/retailproject/RetailWarehouseAnalyzer.java`
  Provides reusable warehouse analytics used by the service layer.
- `src/main/java/com/retailproject/RetailResultsWriter.java`
  Writes the consolidated analytics export.
- `src/main/java/com/retailproject/RetailWarehouseWriter.java`
  Writes the fact and dimension CSV outputs.
- `src/main/resources/static/dashboard/`
  Protected dashboard assets served by Spring Boot after login.
- `frontend/`
  Legacy standalone frontend source kept for reference during development.

### Data Model

Input:

- `src/main/resources/retail.csv`

Warehouse-style output generated into `output/` at runtime:

- `FactSales.csv`
- `DimCustomer.csv`
- `DimProduct.csv`
- `DimDate.csv`
- `DimCity.csv`
- `DimPayment.csv`
- `DimStatus.csv`
- `RetailResults.csv`

### API Endpoints

- `GET /api/filter-options`
  Returns the selectable values used by frontend filters.
- `GET /api/dashboard`
  Returns summary cards, charts, trend data, insights, and contribution tables.
- `GET /api/table`
  Returns paginated table data for a named dataset.
- `GET /api/ask`
  Returns an answer, interpretation, and optional chart or table for a business question.
- `GET /download/{name}`
  Downloads a CSV file for the selected raw or generated table.
- `GET /login`
  Custom login page for dashboard and API access.
- `GET /swagger-ui.html`
  Interactive API documentation, protected by login.

Supported dashboard filters:

- `dateFrom`
- `dateTo`
- `city`
- `category`
- `product`
- `paymentMethod`
- `status`
- `customer`

## Key Features

- Executive KPI summary for revenue, orders, units sold, discount, customers, and products
- Revenue trend exploration with date focus from chart clicks
- Category, city, customer, payment, and product performance views
- Top insights and watchout cards based on the filtered slice
- Data explorer for raw input and generated warehouse/report tables
- Business Q&A assistant grounded in the current filtered dataset
- Downloadable CSV outputs for validation and BI-style inspection

## Project Structure

```text
RetailProject/
|-- frontend/
|-- postman/
|-- .mvn/
|   `-- wrapper/
|-- mvnw
|-- mvnw.cmd
|-- pom.xml
|-- Dockerfile
|-- start-dashboard.ps1
`-- src/
    |-- main/
    |   |-- java/com/retailproject/
    |   |   |-- config/
    |   |   |-- dto/
    |   |   |-- dto/response/
    |   |   |-- security/
    |   |   |-- web/
    |   |   |-- DashboardDataService.java
    |   |   |-- RetailProjectApplication.java
    |   |   `-- warehouse and writer classes
    |   `-- resources/
    |       `-- retail.csv
    `-- test/
        `-- java/com/retailproject/DashboardControllerTest.java
```

## Local Development

### Requirements

- Java 21

### Run The Backend

From the project root:

```powershell
.\start-dashboard.ps1
```

Or directly:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend API:

```text
http://localhost:8080
```

Protected dashboard:

```text
http://localhost:8080/dashboard/
```

Login page:

```text
http://localhost:8080/login
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

### Default Credentials

The application now requires login for the dashboard, APIs, downloads, and Swagger.

Local defaults are in `src/main/resources/application.properties`:

```text
admin username: admin
admin password: change-me-now
user username: analyst
user password: change-me-user
```

Override them before deployment with environment variables:

```text
APP_SECURITY_USERNAME=your-admin-username
APP_SECURITY_PASSWORD=your-admin-password
APP_SECURITY_USER_USERNAME=your-user-username
APP_SECURITY_USER_PASSWORD=your-user-password
```

### API Privacy Defaults

The project now uses two authentication flows on purpose:

- browser users sign in through `/login` and use the protected dashboard session
- API clients sign in through `POST /api/auth/login` and use a bearer token

Protected API paths behave like APIs:

- anonymous requests to `/api/**`, `/download/**`, and `/v3/api-docs/**` return `401 Unauthorized`
- same-origin dashboard calls still work through the browser login session
- Postman and similar API tools should use the bearer token flow, not browser cookies

### Role Access

- `USER`
  Can access the dashboard and standard `/api/**` endpoints.
- `ADMIN`
  Can access everything a `USER` can, plus `/download/**`, `/swagger-ui.html`, `/swagger-ui/**`, and `/v3/api-docs/**`.

Cross-origin API access is disabled unless you explicitly allow trusted origins:

```text
APP_SECURITY_ALLOWED_ORIGINS=https://your-frontend.example.com,https://admin.yourcompany.com
```

Cookie defaults are also tightened:

```text
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=lax
```

### API Authentication Flow

For API tools such as Postman:

1. `POST /api/auth/login` with JSON credentials
2. copy the returned `accessToken`
3. send `Authorization: Bearer <accessToken>` on protected API requests

Example login request:

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "analyst",
  "password": "change-me-user"
}
```

Example successful response:

```json
{
  "accessToken": "token-value",
  "tokenType": "Bearer",
  "username": "user",
  "roles": ["ROLE_USER"],
  "expiresAt": "2026-06-03T12:00:00Z"
}
```

API logout is also available:

- `POST /api/auth/logout`
  Revokes the current bearer token when sent with `Authorization: Bearer <accessToken>`.

### Run The Frontend

The recommended frontend is now the protected backend-served dashboard:

- `http://localhost:8080/dashboard/`

The old standalone file-based frontend still exists in `frontend/`, but it is no longer the primary access path.

If you still open it directly:

- `frontend/index.html`

By default the frontend calls:

```text
http://localhost:8080
```

If you host the API elsewhere, set this before loading the page:

```html
<script>
window.RETAIL_API_BASE_URL = "https://your-api-host";
</script>
```

## Testing

Run:

```powershell
.\mvnw.cmd test
```

Primary automated coverage is in:

- `src/test/java/com/retailproject/DashboardControllerTest.java`

These tests exercise the protected REST endpoints and their basic response contracts.

## API Exploration

Manual API requests can also be explored with:

- `postman/RetailProject.postman_collection.json`

Recommended Postman flow:

1. import the collection
2. set collection variables for `baseUrl`, `userUsername`, `userPassword`, `adminUsername`, and `adminPassword`
3. run `API Login -> User Login` or `API Login -> Admin Login`
4. let the collection save `userToken` or `adminToken`
5. call the protected requests with the saved bearer token

Expected examples:

- `User Access -> Filter Options` -> `200`
- `User Access -> Dashboard Summary` -> `200`
- `User Access -> Download CSV Should Be Blocked` -> `403`
- `Admin Access -> API Docs` -> `200`
- `Anonymous Access -> Filter Options Without Token` -> `401`

## Generated Folders

These folders are runtime or build artifacts and are not part of the source structure:

- `.maven/`
  Maven wrapper cache and local repository created on demand.
- `target/`
  Maven build output and test reports.
- `output/`
  Generated warehouse and report CSV files.

They are safe to delete when no Java or Maven process is using the project.

## Deployment

The project is containerized through `Dockerfile` and is suitable for platforms such as Render.

### Build And Run With Docker

```powershell
docker build -t retail-analysis-dashboard .
docker run -p 8080:8080 retail-analysis-dashboard
```

The container builds the Spring Boot jar in a Maven stage and runs it on Java 21.

### Frontend And Backend On Render

The recommended deployment is now a single secured Spring Boot web service. The application root redirects to `/dashboard/`, which in turn requires login.

## Important Implementation Notes

- The protected dashboard is now served directly by Spring Boot.
- The application is CSV-backed and does not use a database.
- The warehouse is built in memory during startup.
- The business assistant is rule-based logic inside `DashboardDataService`; it is not backed by an external LLM.
- Runtime CSV exports are regenerated into `output/` when the application starts.
- Maven wrapper caches and build artifacts are isolated to project-local generated folders.
- Anonymous API access is blocked. Protected API and download routes return `401`, while dashboard users authenticate through the `/login` page.
- Browser login and API login are intentionally separated so dashboard sessions and Postman testing do not interfere with each other.
- Cross-origin access is deny-by-default and must be explicitly allowlisted through `APP_SECURITY_ALLOWED_ORIGINS`.

## Tech Stack

- Java 21
- Spring Boot 3
- Spring MVC
- Spring Validation
- springdoc OpenAPI / Swagger UI
- Lombok
- HTML
- CSS
- Vanilla JavaScript
- CSV as the source dataset
- Docker for containerized deployment

## Current Scope

This project is well-suited for:

- analytics portfolio work
- dashboard demos
- business intelligence prototypes
- API and frontend integration examples

It is intentionally optimized for clarity and demonstrability rather than large-scale data processing.
