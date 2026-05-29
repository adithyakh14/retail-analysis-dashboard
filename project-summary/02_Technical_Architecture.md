# Technical Architecture

## High-Level Architecture

The project is designed as a separated frontend-backend system.

### Backend

- Technology: Java 21 + Spring Boot
- Role: exposes REST APIs, loads data, computes analytics, writes output CSV files

### Frontend

- Technology: HTML, CSS, Vanilla JavaScript
- Role: consumes backend APIs and displays the dashboard

### Testing

- Postman collection for manual API testing
- Spring Boot test suite for endpoint verification

## Final Runtime Model

The final runtime flow is:

`retail.csv` -> data reader -> retail records -> warehouse model -> analytics service -> REST API -> frontend dashboard

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

This is the API surface that Postman and the frontend call.

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

## Frontend Architecture

The frontend is intentionally simple and independent.

It uses:

- `index.html` for structure
- `styles.css` for layout and look
- `app.js` for API calls and rendering

### Frontend Responsibilities

- maintain filter state
- call backend endpoints
- render summary cards
- render charts
- render tables
- trigger business Q&A requests

### Backend Responsibilities

- all data loading
- all analytics logic
- all aggregation logic
- all answer-generation logic
- all export generation

This separation is important because it keeps the frontend focused on presentation and the backend focused on business logic.

## Testing Approach

### Manual Testing

Postman collection:

- `postman/RetailProject.postman_collection.json`

Used to test:

- endpoint availability
- response shape
- filtered queries
- downloads

### Automated Testing

Spring Boot test class:

- `src/test/java/com/retailproject/DashboardControllerTest.java`

Used to verify:

- endpoint success responses
- expected JSON structure
- download response behavior

## Build And Run Tooling

The project uses:

- Maven wrapper (`mvnw`, `mvnw.cmd`)
- project-local Maven config under `.mvn/`

This makes the project easier to run on machines that do not already have Maven installed globally.

## Deployment Model

Because the project is now API-based with a separate frontend, deployment is logically split into:

- backend API deployment
- frontend deployment

Typical production delivery:

- backend hosted on Render
- frontend hosted separately on a static hosting platform or served by another delivery layer

## Engineering Strengths

- clear separation of concerns
- API-first design
- understandable data pipeline
- warehouse-style thinking
- both manual and automated testing
- local reproducibility through Maven wrapper

## Important Limitation

The project uses a CSV file instead of a database.

This is acceptable and useful for:

- portfolio projects
- analytics demos
- interview presentations
- small-scale prototype delivery

But at larger scale, a database or warehouse platform would usually replace the flat file.
