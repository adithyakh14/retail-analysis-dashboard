# Retail Project Overview

## Executive Summary

This project is a retail analytics and reporting solution built to convert raw sales transactions into structured business insights. It takes retail sales data as input, organizes that data into a warehouse-style model, performs analysis on top of it, and produces report files that help a business owner understand performance across products, customers, cities, payment methods, order status, and time.

In simple terms, the project answers this business question:

"Given a set of daily retail transactions, how can we turn them into useful management information for better decision-making?"

## Business Problem

Retail businesses often store day-to-day transaction data, but raw transaction rows alone are not enough for business review. A business owner typically wants to know:

- how much revenue the business generated
- which products are performing best
- which customers are contributing most
- which locations are generating stronger sales
- how discounts are affecting actual sales value
- which payment methods customers prefer
- how sales are changing over time

Without a structured reporting layer, answering these questions manually can be slow, repetitive, and error-prone.

## Business Input

The input to this project is a retail transaction dataset stored in:

- `src/main/resources/retail.csv`

This file represents the operational sales activity of the retail business. Each record contains information such as:

- order ID
- order date
- customer
- city
- product
- category
- price
- quantity
- discount
- payment method
- order status

From a business perspective, this is the raw sales ledger of the business.

## Expected Business Output

The expected output is a set of report and warehouse CSV files written to:

- `output/`

These outputs are intended to support both business reporting and future analytics use cases.

### Business Report Output

- `RetailResults.csv`

This file contains consolidated analytical results that summarize business performance.

### Warehouse Output

- `FactSales.csv`
- `DimCustomer.csv`
- `DimProduct.csv`
- `DimDate.csv`
- `DimCity.csv`
- `DimPayment.csv`
- `DimStatus.csv`

These files create a structured reporting model that can later support dashboards, BI tools, and advanced analysis.

## Business Benefits

This project provides the following benefits to a business owner or retail manager:

- Better visibility into overall sales performance
- Faster access to product-level and customer-level insights
- Improved understanding of city-wise and market-wise sales contribution
- Better tracking of discounts and their effect on realized sales
- Clearer view of payment behavior and order status distribution
- Easier trend analysis across dates and reporting periods
- A stronger reporting foundation for future dashboards and business intelligence systems

## Business Value to the Owner

The main value of this project is that it transforms raw operational data into business-ready information. Instead of only storing transaction records, it helps the business owner:

- monitor sales performance
- compare results across dimensions
- identify trends and patterns
- improve reporting quality
- support data-driven business decisions

In practice, this means the owner can make better decisions related to sales strategy, product focus, discounting, customer engagement, and location-based performance.

## Solution Overview

The solution works in five stages:

### 1. Read Raw Input

Entry point:

- `src/main/java/com/retailproject/RetailMain.java`

The application starts in `RetailMain`.

`RetailDataReader` loads `retail.csv` from the classpath and converts each row into a `RetailRecord`.

Main classes involved:

- `RetailMain`
- `RetailDataReader`
- `RetailRecord`

### 2. Build Warehouse Model

`RetailWarehouseBuilder` converts raw `RetailRecord` rows into a star-schema style structure:

- Dimension tables
  - `CustomerDimension`
  - `ProductDimension`
  - `DateDimension`
  - `CityDimension`
  - `PaymentDimension`
  - `StatusDimension`
- Fact table
  - `SalesFact`

These are grouped inside:

- `RetailWarehouse`

This step separates descriptive business attributes from measurable sales data.

### 3. Run Analytics

`RetailWarehouseAnalyzer` reads the warehouse model and generates metrics such as:

- unique customers
- unique products
- total sales
- sales by product
- orders by customer
- sales by city
- sales by payment method
- sales trend by date
- order count by status
- sales by status

### 4. User Interaction

`RetailMain` provides a menu-driven console interface where the user can:

- view summary
- view product sales
- view customer orders
- view warehouse analytics
- export result files
- export dimension and fact tables

### 5. Write Output

Two writers generate output:

- `RetailResultsWriter`
  - writes business report output
- `RetailWarehouseWriter`
  - writes warehouse dimension and fact tables

Generated files are written to:

- `output/`

## End-to-End Flow

The overall project flow is:

`retail.csv` -> `RetailRecord` objects -> warehouse dimensions and facts -> analytics -> report files in `output/`

## Output Questions Answered

The generated outputs help answer questions such as:

- What are total sales for the available period?
- Which products are driving the most revenue?
- Which customers are contributing the most value?
- Which cities are generating the highest sales?
- Which payment methods are most common?
- How is sales performance changing over time?
- What is the order count and sales value by status?

## Technical Structure

Main source folder:

- `src/main/java/com/retailproject`

Resource folder:

- `src/main/resources`

Important files:

- `RetailMain.java`
- `RetailDataReader.java`
- `RetailRecord.java`
- `RetailWarehouseBuilder.java`
- `RetailWarehouseAnalyzer.java`
- `RetailResultsWriter.java`
- `RetailWarehouseWriter.java`
