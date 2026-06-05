# Testing Textbook

This document explains the testing work in the project in a textbook-style format.

## 1. Why Testing Was Added

The project already had strong manual verification through:

- browser login testing
- dashboard testing
- Swagger testing
- Postman API testing

That was useful, but it still left an important gap:

- repeated automated checking of small logic units

Unit tests were added to make the project more stable without changing runtime behavior.

## 2. What Kind Of Testing Exists In This Project

The project now uses two main automated testing layers.

### Integration-Style Testing

These tests start Spring Boot and verify that multiple layers work together.

Example:

- API endpoints return the expected status codes
- bearer-token login works
- login page and dashboard security behave correctly

Main file:

- `src/test/java/com/retailproject/DashboardControllerTest.java`

### Unit Testing

These tests do not start the whole application.

They focus on one class at a time and verify its rules directly.

Example:

- lockout logic
- token creation
- warehouse analytics
- CSV reading

## 3. Why Unit Tests Matter

Unit tests are useful because they:

- catch regressions early
- make refactoring safer
- prove important logic works in isolation
- run quickly
- reduce the need to retest everything manually after every code change

## 4. Main Unit Test Groups Added

### Security Rule Tests

#### `LoginAttemptServiceTest`

Purpose:

- verifies lockout logic for repeated failed logins

What it checks:

- first and second failures do not lock the user
- third failure locks the user
- reset clears the lock
- username normalization works
- blank usernames are ignored safely

#### `AppUserDetailsServiceTest`

Purpose:

- verifies security account loading and role assignment

What it checks:

- admin loads with `ROLE_ADMIN` and `ROLE_USER`
- analyst loads with `ROLE_USER`
- prefixed encoded passwords remain unchanged
- locked users are rejected
- unknown users are rejected

#### `CustomAuthenticationFailureHandlerTest`

Purpose:

- verifies how failed browser login attempts are redirected

What it checks:

- invalid login redirects to `/login?error=invalid`
- locked login redirects to `/login?error=locked`
- third failed attempt escalates to lockout

### API Authentication Tests

#### `ApiTokenServiceTest`

Purpose:

- verifies token creation and revocation logic

What it checks:

- token is issued with username, roles, and expiry
- token lookup succeeds for valid tokens
- revoked token becomes invalid
- blank tokens return no session

#### `ApiTokenAuthenticationFilterTest`

Purpose:

- verifies bearer-token authentication at the filter level

What it checks:

- valid bearer tokens populate the Spring Security context
- invalid tokens do not authenticate the user
- auth endpoints are skipped by the filter
- non-protected paths are skipped by the filter

#### `ApiAuthenticationControllerTest`

Purpose:

- verifies API login and logout behavior in isolation

What it checks:

- locked users get `423 LOCKED`
- valid login returns token payload
- invalid credentials return `401 UNAUTHORIZED`
- repeated failures escalate to `423 LOCKED`
- logout revokes tokens and still succeeds safely without a header

### Data Pipeline And Analytics Tests

#### `RetailDataReaderTest`

Purpose:

- verifies CSV loading and helper aggregations

What it checks:

- real `retail.csv` loads successfully
- missing CSV resource throws an error
- unique customers and products are collected correctly
- total sales calculation is correct
- sales by product are grouped correctly
- orders can be filtered/grouped by customer

#### `RetailWarehouseBuilderTest`

Purpose:

- verifies transformation from records into a warehouse-style model

What it checks:

- correct dimension counts are created
- repeated natural keys reuse the same warehouse keys
- date dimensions contain year, month, quarter, and day-of-week values

#### `RetailWarehouseAnalyzerTest`

Purpose:

- verifies analytics calculations on top of the warehouse

What it checks:

- total sales
- unique customers and products
- sales by city
- sales by payment method
- order counts by status
- sales trend by date
- sales by product

## 5. How These Tests Protect The Project

These tests protect the project in different ways:

- security tests protect login and authentication logic
- API auth tests protect the bearer-token flow used in Postman
- data reader tests protect CSV ingestion and simple aggregations
- warehouse builder tests protect transformation logic
- analyzer tests protect business calculations
- integration tests protect the full endpoint contracts

This means a change in one part of the project is much more likely to be caught automatically before it becomes a user-facing problem.

## 6. How To Run The Tests

Run the entire suite:

```powershell
cd D:\vscprojects\RetailProject
.\mvnw.cmd test
```

Expected result:

- all tests execute
- `BUILD SUCCESS`

Run a single unit test class:

```powershell
.\mvnw.cmd "-Dtest=LoginAttemptServiceTest" test
```

Other examples:

```powershell
.\mvnw.cmd "-Dtest=ApiTokenServiceTest" test
.\mvnw.cmd "-Dtest=RetailWarehouseAnalyzerTest" test
.\mvnw.cmd "-Dtest=CustomAuthenticationFailureHandlerTest" test
.\mvnw.cmd "-Dtest=ApiAuthenticationControllerTest" test
```

## 7. Current Test Position

At the end of today’s testing work, the project has:

- `59` passing automated tests
- integration coverage for the main secured endpoint flow
- unit coverage for the key security, authentication, CSV, warehouse, and analytics classes

## 8. Final Teaching Summary

If you need one short explanation:

Unit tests check one class at a time, integration tests check multiple parts together, and this project now uses both so that security behavior, token flow, data transformation, and analytics logic can be verified repeatedly without changing the runtime application itself.
