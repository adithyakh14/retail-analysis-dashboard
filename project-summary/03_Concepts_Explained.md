# Concepts Explained

This document explains the important concepts used in the project in simple and presentation-friendly language.

## 1. What Is An API?

An API, or Application Programming Interface, is a way for one software system to ask another software system for data or actions.

In this project:

- the frontend asks the backend API for dashboard data
- Postman can also ask the backend API for the same data

Example:

- frontend sends request to `/api/dashboard`
- backend returns dashboard information in JSON format

## 2. What Does API-Based Mean?

API-based means the frontend and backend communicate through defined endpoints instead of being tightly mixed together.

This gives cleaner structure and makes the system easier to:

- test
- deploy
- maintain
- extend

## 3. Frontend And Backend Separation

### Frontend

The frontend is what the user sees.

In this project, it includes:

- layout
- styling
- buttons
- tables
- charts

### Backend

The backend is the logic engine behind the scenes.

In this project, it:

- reads the dataset
- computes metrics
- filters records
- prepares API responses

### Why Separation Matters

Separating them is a professional design choice because:

- the UI can change without rewriting the business engine
- the API can be tested independently
- different frontend clients can reuse the same backend

## 4. What Is Spring Boot?

Spring Boot is a Java framework used to create web applications and APIs quickly.

In this project, Spring Boot is used to:

- start the web server
- define API endpoints
- manage components and services
- simplify backend development

## 5. What Is A REST API?

A REST API is a common web API style where data is requested through URLs and HTTP methods.

This project mainly uses `GET` requests to retrieve:

- dashboard results
- filters
- tables
- answers
- CSV downloads

## 6. What Is JSON?

JSON is a lightweight data format used to exchange structured data between systems.

It is easy for both JavaScript and backend applications to work with.

This project returns JSON for most API responses.

## 7. What Is A CSV?

CSV means Comma-Separated Values.

It is a flat file where data is stored in rows and columns.

This project uses a CSV file as the source dataset:

- `retail.csv`

This file acts as the raw transaction input.

## 8. What Is A Data Pipeline?

A data pipeline is the step-by-step flow that turns raw data into useful output.

In this project, the pipeline is:

1. read raw CSV
2. create record objects
3. build warehouse-style structures
4. compute metrics and aggregates
5. expose results through APIs
6. render them on the dashboard

## 9. What Is Warehouse-Style Modeling?

Warehouse-style modeling is a way of organizing analytical data for reporting.

It usually separates:

- measurable events
- descriptive categories

This makes reporting easier and more structured.

## 10. What Is A Fact Table?

A fact table stores measurable business events.

In this project, `SalesFact` represents the sales activity.

Examples of facts:

- order id
- quantity
- price
- discount
- total sales amount

## 11. What Are Dimension Tables?

Dimension tables store descriptive information used to group and analyze facts.

In this project, dimensions include:

- customer
- product
- date
- city
- payment
- status

These help answer questions like:

- sales by city
- sales by product
- sales by customer

## 12. What Is Filtering?

Filtering means narrowing the data to a selected subset.

Example:

- only Mumbai
- only Electronics
- only Card payments

The dashboard and API both support filtering.

## 13. What Are DTOs?

DTO means Data Transfer Object.

A DTO is a class used to send or receive data in a controlled structure.

In this project, DTOs help keep API input/output organized and predictable.

## 14. What Is Postman?

Postman is a tool used to test APIs without needing the frontend.

It is useful because it lets you:

- send requests manually
- inspect responses
- test endpoints independently
- validate API behavior

## 15. What Is Lombok?

Lombok is a Java library that reduces boilerplate code.

Instead of manually writing many getters and constructors, annotations can generate them automatically.

This project uses Lombok in model classes to keep the code cleaner.

## 16. What Is The Maven Wrapper?

The Maven wrapper is a project-local way to run Maven commands.

Instead of requiring the user to install Maven globally, the project includes:

- `mvnw`
- `mvnw.cmd`

This makes local setup easier and more consistent.

## 17. What Is Swagger UI?

Swagger UI is a browser-based API documentation tool.

It shows:

- available endpoints
- request parameters
- response structure

In this project it helps explore the backend interactively.

## 18. What Is Deployment?

Deployment means making the project available outside your local machine.

In this project:

- backend can be deployed as a web service
- frontend can be hosted separately

That means the project can be delivered to a client as a working online solution.

## 19. What Is A Dashboard?

A dashboard is a visual interface for monitoring key business information.

In this project, the dashboard includes:

- KPI cards
- charts
- tables
- filters
- business Q&A

## 20. What Does AI-Style Assistant Mean Here?

The project includes a business question feature that behaves like a simple analytics assistant.

It is not a large language model integration.

Instead, it uses rule-based logic to interpret business questions and return:

- an answer
- an interpretation
- sometimes a chart or table

## 21. Why This Project Is Good For Presentation

This project is presentation-friendly because it includes both business and technical value.

Business side:

- clear insight generation
- usable dashboard
- decision-support framing

Technical side:

- separated architecture
- API design
- testability
- structured data transformation

## 22. One Simple Way To Explain The Whole Project

If you need one simple sentence:

This project reads retail sales data, transforms it into structured analytics, exposes those analytics through a backend API, and presents them through a separate frontend dashboard for business decision-making.
