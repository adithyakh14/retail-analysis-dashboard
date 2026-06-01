# Retail Analysis Dashboard

Retail Analysis Dashboard is an API-first Spring Boot backend with a separate browser frontend. It reads a CSV dataset, builds an in-memory warehouse-style model, generates report exports, exposes typed REST APIs, and supports a standalone dashboard client that consumes those APIs.

## Live Deployment

- Frontend dashboard:
  `https://retail-analysis-dashboard-frontend.onrender.com`
- Backend API:
  `https://retail-analysis-dashboard.onrender.com`

Client-facing link:

`https://retail-analysis-dashboard-frontend.onrender.com`

## What The Project Does

- Loads retail transactions from `src/main/resources/retail.csv`
- Converts the raw rows into fact and dimension structures
- Calculates KPIs, trend summaries, contribution tables, and insight cards
- Exposes dashboard data through REST endpoints
- Provides a standalone frontend client in `frontend/`
- Generates downloadable CSV outputs for raw and transformed data

## Architecture

This project has one supported runtime: a Spring Boot application started from `RetailProjectApplication`.

The end-to-end flow is:

`retail.csv` -> `RetailRecord` objects -> warehouse model -> filtered aggregations -> REST API -> standalone dashboard UI

### Runtime Layers

- `src/main/java/com/retailproject/RetailProjectApplication.java`
  Spring Boot entry point.
- `src/main/java/com/retailproject/DashboardController.java`
  HTTP layer for dashboard, filter, table, ask, and download endpoints.
- `src/main/java/com/retailproject/DashboardDataService.java`
  Core application service. Loads data once, builds analytics, writes CSV outputs, and answers API requests.
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
- `frontend/`
  Standalone frontend that calls the backend API over HTTP.

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
- `GET /swagger-ui.html`
  Interactive API documentation.

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
    |   |   |-- dto/
    |   |   |-- dto/response/
    |   |   |-- DashboardController.java
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

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

### Run The Frontend

After the backend is running, open:

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

These tests exercise the main REST endpoints and basic response contracts.

## API Exploration

Manual API requests can also be explored with:

- `postman/RetailProject.postman_collection.json`

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

The deployed setup uses two Render services:

- a `Static Site` for the frontend dashboard
- a `Web Service` for the Spring Boot backend API

The frontend should be treated as the main user-facing deliverable. The backend URL is primarily for API access, Swagger, and technical testing.

### Avoiding The Whitelabel Error Page

If someone opens the backend root URL directly, the project now redirects to the deployed frontend when `app.frontend-url` is configured. Otherwise it shows a friendly landing page instead of Spring Boot's default error page.

If you want the backend root URL to open the deployed frontend automatically, configure:

```text
app.frontend-url=https://your-frontend-url
```

On Render, this can be added as an environment variable:

```text
APP_FRONTEND_URL=https://your-frontend-url
```

Current deployed value:

```text
APP_FRONTEND_URL=https://retail-analysis-dashboard-frontend.onrender.com
```

## Important Implementation Notes

- The backend is API-only; the frontend is a separate client folder.
- The application is CSV-backed and does not use a database.
- The warehouse is built in memory during startup.
- The business assistant is rule-based logic inside `DashboardDataService`; it is not backed by an external LLM.
- Runtime CSV exports are regenerated into `output/` when the application starts.
- Maven wrapper caches and build artifacts are isolated to project-local generated folders.

## Tech Stack

- Java 21
- Spring Boot 3
- Spring MVC
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
