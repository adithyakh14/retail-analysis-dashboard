# Retail Analysis Dashboard

Live demo:

- https://retail-analysis-dashboard.onrender.com/

## Overview

Retail Analysis Dashboard is a Java-based analytics web application that transforms retail transaction data into a business-facing dashboard with interactive visuals, data exploration, and AI-assisted insights.

The project starts from raw retail sales data, builds a warehouse-style reporting model, computes business metrics, and presents the output through a deployed web interface designed for business owners, analysts, and interview demos.

## What This Project Does

- reads raw retail transaction data from CSV
- builds warehouse-style fact and dimension tables
- calculates business metrics and trend summaries
- serves a live dashboard with filters and interactive visuals
- provides a dynamic AI business assistant grounded in the dataset
- allows raw and transformed data exploration through tables and downloads

## Key Features

- Executive KPI summary:
  - Total Revenue
  - Total Orders
  - Total Units Sold
  - Average Order Value
  - Average Discount
  - Active Customers
  - Total Products
- Interactive revenue trend analysis with date drill focus
- Category, city, customer, payment, and product performance views
- Top and bottom product insights
- Watchouts and business risk indicators
- AI business assistant for:
  - comparisons
  - best/worst performers
  - growth recommendations
  - investment focus
  - customer and market insights
  - trend interpretation
  - operational limitations such as missing profit-cost data
- Data explorer for raw input and warehouse output CSV files
- Docker-ready deployment setup

## Tech Stack

- Java 21
- built-in Java HTTP server
- HTML
- CSS
- JavaScript
- CSV-based data source
- Render deployment via Docker

## Project Structure

- `src/main/java/com/retailproject`
  - dashboard server
  - analytics service
  - warehouse builder
  - data reader
  - result writers
- `src/main/resources`
  - `retail.csv`
  - web assets
- `output`
  - generated fact and dimension CSV files

## Main Files

- `src/main/java/com/retailproject/RetailDashboardServer.java`
- `src/main/java/com/retailproject/DashboardDataService.java`
- `src/main/resources/web/index.html`
- `src/main/resources/web/app.js`
- `src/main/resources/web/styles.css`

## Local Run

From the project folder:

```powershell
.\start-dashboard.ps1
```

Then open:

```text
http://localhost:8080
```

## Deployment

The project is prepared for Docker-based deployment.

See:

- [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)

Current deployed version:

- https://retail-analysis-dashboard.onrender.com/

## Business Value

This project demonstrates how raw operational retail data can be turned into:

- business-ready reporting
- warehouse-style analytics outputs
- executive dashboard insights
- AI-assisted decision support

It is suitable for:

- portfolio showcase
- resume project work
- academic submission
- analytics and business intelligence demos

## Resume Summary

Built and deployed a retail analytics dashboard that converts raw sales CSV data into interactive business insights using Java, warehouse modeling, dynamic visualizations, and an AI-powered business assistant.
