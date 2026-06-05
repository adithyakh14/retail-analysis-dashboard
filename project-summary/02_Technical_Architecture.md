# Technical Architecture

## High-Level Architecture

The project is designed as a secured Spring Boot application that serves both the protected dashboard UI and the backend APIs from the same runtime.

### Application Runtime

- Technology: Java 21 + Spring Boot
- Role: serves the login page, protected dashboard assets, REST APIs, security rules, and generated CSV downloads

### Testing

- Postman collection for manual API testing
- Spring Boot test suite for endpoint verification

## Final Runtime Model

The final runtime flow is:

`retail.csv` -> data reader -> retail records -> warehouse model -> analytics service -> protected REST API -> backend-served dashboard UI

## Core Backend Components

### `RetailProjectApplication`

The Spring Boot entry point.

Its purpose is to start the application and initialize the backend runtime.

### `DashboardController`

The REST controller layer.

It exposes the backend endpoints for:

- dashboard data
- filter options
- paginated table data
- business question answering
- CSV download

This is the API surface that the dashboard and Postman call.

### `DashboardDataService`

The central business service.

It is responsible for:

- loading the dataset
- filtering records
- building dashboard responses
- producing charts and tables
- generating AI-style answers
- creating downloadable outputs

This class is effectively the analytics engine of the project.

### `RetailDataReader`

Reads the CSV input and converts each row into `RetailRecord` objects.

### `RetailWarehouseBuilder`

Transforms raw records into a warehouse-style structure with:

- fact table
- dimension tables

### `RetailWarehouseAnalyzer`

Provides reusable analytics calculations on top of the warehouse structure.

### Writers

- `RetailResultsWriter`
- `RetailWarehouseWriter`

These create generated CSV outputs in `output/`.

## Data Modeling Approach

The project uses a star-schema-inspired reporting model.

### Fact Table

- `SalesFact`

This stores measurable business activity such as:

- order id
- quantity
- price
- discount
- sales amount

### Dimension Tables

- `CustomerDimension`
- `ProductDimension`
- `DateDimension`
- `CityDimension`
- `PaymentDimension`
- `StatusDimension`

These store descriptive attributes used for slicing and grouping the facts.

## API Design

### Main Endpoints

- `GET /api/filter-options`
- `GET /api/dashboard`
- `GET /api/table`
- `GET /api/ask`
- `GET /download/{name}`

### API Characteristics

- stateless request/response design
- JSON responses for analytics
- CSV download support for exports
- DTO-based request and response layer

## Dashboard UI Architecture

The dashboard UI is intentionally lightweight and is now served directly by Spring Boot after login.

It uses:

- `src/main/resources/static/dashboard/index.html` for structure
- `src/main/resources/static/dashboard/styles.css` for layout and look
- `src/main/resources/static/dashboard/app.js` for API calls and rendering

### Dashboard Responsibilities

- maintain filter state
- call protected backend endpoints
- render summary cards
- render charts
- render tables
- trigger business Q&A requests

### Backend Responsibilities

- login and role-based access control
- all data loading
- all analytics logic
- all aggregation logic
- all answer-generation logic
- all export generation

The old standalone `frontend/` folder is still present as a legacy reference, but it is no longer the primary runtime path.

## Testing Approach

### Manual Testing

Postman collection:

- `postman/RetailProject.postman_collection.json`

Used to test:

- endpoint availability
- response shape
- filtered queries
- downloads
- bearer-token login flow

### Automated Testing

Spring Boot test class:

- `src/test/java/com/retailproject/DashboardControllerTest.java`

Used to verify:

- endpoint success responses
- expected JSON structure
- download response behavior

Additional focused unit tests:

- `LoginAttemptServiceTest`
- `ApiTokenServiceTest`
- `AppUserDetailsServiceTest`
- `CustomAuthenticationFailureHandlerTest`
- `ApiTokenAuthenticationFilterTest`
- `ApiAuthenticationControllerTest`
- `RetailDataReaderTest`
- `RetailWarehouseBuilderTest`
- `RetailWarehouseAnalyzerTest`

These verify the core rule-heavy classes without starting the full application.

## Build And Run Tooling

The project uses:

- Maven wrapper (`mvnw`, `mvnw.cmd`)
- project-local Maven config under `.mvn/`

This makes the project easier to run on machines that do not already have Maven installed globally.

## Deployment Model

Because the project now serves the dashboard and APIs from the same secured Spring Boot runtime, deployment is primarily a single web-service deployment.

Typical production delivery:

- backend and dashboard hosted together on Render
- API testing still available independently through Postman with bearer-token login

## Engineering Strengths

- clear separation of concerns
- protected browser and API access model
- understandable data pipeline
- warehouse-style thinking
- both manual and automated testing
- layered coverage through both unit and integration tests
- local reproducibility through Maven wrapper

## Important Limitation

The project uses a CSV file instead of a database.

This is acceptable and useful for:

- portfolio projects
- analytics demos
- interview presentations
- small-scale prototype delivery

But at larger scale, a database or warehouse platform would usually replace the flat file.
