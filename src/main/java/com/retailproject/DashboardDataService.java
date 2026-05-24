package com.retailproject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class DashboardDataService {
    private static final List<String> SUGGESTED_QUESTIONS = List.of(
            "Which city contributes the highest sales?",
            "Show the bottom 5 products by revenue.",
            "Which category has the strongest average order value?",
            "How much revenue came from Card payments?",
            "What should the business owner pay attention to right now?");
    private static final DateTimeFormatter REFRESH_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final List<RetailRecord> records;
    private final RetailWarehouse warehouse;
    private final RetailWarehouseAnalyzer analyzer;
    private final Path outputDirectory;
    private final String refreshedAt;

    public DashboardDataService() throws IOException {
        RetailDataReader dataReader = new RetailDataReader();
        RetailWarehouseBuilder warehouseBuilder = new RetailWarehouseBuilder();
        this.analyzer = new RetailWarehouseAnalyzer();
        this.records = dataReader.readRecordsFromResource("retail.csv");
        this.warehouse = warehouseBuilder.buildWarehouse(records);
        this.outputDirectory = Path.of("output");
        this.refreshedAt = LocalDateTime.now().format(REFRESH_FORMAT);
        Files.createDirectories(outputDirectory);
        writeOutputs();
    }

    public Map<String, Object> getDashboardData(Map<String, String> filters) {
        List<RetailRecord> filteredRecords = filterRecords(filters);
        MetricSnapshot metrics = snapshot(filteredRecords);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("summary", Map.of(
                "totalSales", round(metrics.totalSales()),
                "totalOrders", metrics.totalOrders(),
                "totalQuantity", metrics.totalQuantity(),
                "averageOrderValue", round(metrics.averageOrderValue()),
                "averageDiscount", round(metrics.averageDiscount()),
                "activeCustomers", metrics.uniqueCustomers(),
                "totalProducts", metrics.uniqueProducts()));
        response.put("meta", Map.of(
                "refreshedAt", refreshedAt,
                "overallRecordCount", records.size(),
                "filteredRecordCount", filteredRecords.size()));
        response.put("charts", Map.of(
                "salesTrend", toSeries(aggregateMetric(filteredRecords, Dimension.DATE, Metric.SALES), true, 999, "date"),
                "categoryPerformance", toSeries(aggregateMetric(filteredRecords, Dimension.CATEGORY, Metric.SALES), false, 6, "category"),
                "topProducts", toSeries(aggregateMetric(filteredRecords, Dimension.PRODUCT, Metric.SALES), false, 5, "product"),
                "bottomProducts", toSeries(bottomEntries(aggregateMetric(filteredRecords, Dimension.PRODUCT, Metric.SALES), 5), "product"),
                "cityPerformance", toSeries(aggregateMetric(filteredRecords, Dimension.CITY, Metric.SALES), false, 6, "city"),
                "topCustomers", toSeries(aggregateMetric(filteredRecords, Dimension.CUSTOMER, Metric.SALES), false, 5, "customer"),
                "paymentPerformance", toSeries(aggregateMetric(filteredRecords, Dimension.PAYMENT, Metric.SALES), false, 5, "paymentMethod"),
                "discountImpact", toSeries(aggregateMetric(filteredRecords, Dimension.CATEGORY, Metric.AVG_DISCOUNT), false, 6, "category")));
        response.put("trend", buildTrendSection(filteredRecords, filters));
        response.put("insights", Map.of(
                "topInsights", buildTopInsights(filteredRecords, metrics),
                "watchouts", buildWatchouts(filteredRecords, metrics)));
        response.put("tables", Map.of(
                "categoryContribution", toContributionTable("Category", aggregateMetric(filteredRecords, Dimension.CATEGORY, Metric.SALES), metrics.totalSales(), 6),
                "cityContribution", toContributionTable("City", aggregateMetric(filteredRecords, Dimension.CITY, Metric.SALES), metrics.totalSales(), 6)));
        return response;
    }

    public Map<String, Object> getFilterOptions() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("dates", records.stream().map(RetailRecord::getOrderDate).distinct().sorted().toList());
        response.put("cities", sortedDistinct(records, RetailRecord::getCity));
        response.put("categories", sortedDistinct(records, RetailRecord::getCategory));
        response.put("products", sortedDistinct(records, RetailRecord::getProduct));
        response.put("paymentMethods", sortedDistinct(records, RetailRecord::getPaymentMethod));
        response.put("statuses", sortedDistinct(records, RetailRecord::getOrderStatus));
        response.put("customers", sortedDistinct(records, RetailRecord::getCustomer));
        return response;
    }

    public Map<String, Object> getTable(String name, Map<String, String> params) {
        CsvTable table = readTable(name);
        String search = params.getOrDefault("search", "").trim().toLowerCase(Locale.ROOT);
        int page = parsePositiveInt(params.get("page"), 1);
        int pageSize = parsePositiveInt(params.get("pageSize"), 10);

        List<List<String>> filteredRows = table.rows();
        if (!search.isEmpty()) {
            filteredRows = filteredRows.stream()
                    .filter(row -> row.stream().anyMatch(value -> value.toLowerCase(Locale.ROOT).contains(search)))
                    .toList();
        }

        int totalRows = filteredRows.size();
        int totalPages = Math.max(1, (int) Math.ceil(totalRows / (double) pageSize));
        int safePage = Math.min(page, totalPages);
        int fromIndex = Math.min((safePage - 1) * pageSize, totalRows);
        int toIndex = Math.min(fromIndex + pageSize, totalRows);
        List<List<String>> pageRows = filteredRows.subList(fromIndex, toIndex);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", name);
        response.put("label", table.label());
        response.put("columns", table.columns());
        response.put("rows", pageRows);
        response.put("page", safePage);
        response.put("pageSize", pageSize);
        response.put("totalRows", totalRows);
        response.put("totalPages", totalPages);
        response.put("downloadUrl", "/download/" + name);
        return response;
    }

    public Map<String, Object> askQuestion(String query, Map<String, String> filters) {
        String normalized = query == null ? "" : query.trim();
        List<RetailRecord> filteredRecords = filterRecords(filters);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", normalized);
        response.put("suggestions", SUGGESTED_QUESTIONS);
        response.put("recordCount", filteredRecords.size());

        if (normalized.isBlank()) {
            response.put("answer", "Ask any business question about revenue, orders, products, categories, customers, cities, payment methods, discounts, or performance trends.");
            response.put("interpretation", "The assistant stays grounded in the current retail dataset and respects the filters you apply.");
            return response;
        }

        String lowercase = normalized.toLowerCase(Locale.ROOT);
        if (containsAny(lowercase, "compare", "versus", "vs")) {
            Map<String, Object> comparison = comparisonQuestionResponse(normalized, lowercase, filteredRecords, inferMetric(lowercase), "the current filtered selection");
            if (comparison != null) {
                return comparison;
            }
        }

        QueryContext context = buildQueryContext(normalized, filteredRecords);
        List<RetailRecord> scopedRecords = context.records();
        MetricSnapshot metrics = snapshot(scopedRecords);
        int resultLimit = determineResultLimit(lowercase, context.dimension(), scopedRecords);

        if (scopedRecords.isEmpty()) {
            response.put("answer", "There is no matching data for that question under the current filters.");
            response.put("interpretation", "Try removing some filters or asking about a broader slice of the business.");
            return response;
        }

        String scopedLabel = context.scopeDescription().isBlank() ? "the current selection" : context.scopeDescription();

        if (containsAny(lowercase, "profit", "profits", "margin", "margins")) {
            return profitQuestionResponse(response, scopedRecords, metrics, scopedLabel);
        }

        if (containsAny(lowercase, "invest", "investment", "where should i invest", "where to focus", "focus area")) {
            return investmentQuestionResponse(response, scopedRecords, metrics, scopedLabel);
        }

        if (containsAny(lowercase, "best customer", "strongest customer", "top customer", "valuable customer", "highest customer")) {
            return bestCustomerResponse(response, scopedRecords, scopedLabel);
        }

        if (containsAny(lowercase, "best city", "best market", "strongest market", "strongest city")) {
            return bestMarketResponse(response, scopedRecords, scopedLabel);
        }

        if (containsAny(lowercase, "improve sales", "increase sales", "grow sales", "boost sales", "what can i do")) {
            return improvementQuestionResponse(response, scopedRecords, metrics, scopedLabel);
        }

        if (containsAny(lowercase, "recommend", "strategy", "strategic", "next step", "next steps", "plan")) {
            return strategyQuestionResponse(response, lowercase, scopedRecords, metrics, scopedLabel);
        }

        if (containsAny(lowercase, "where to buy", "where can i buy", "supplier", "suppliers", "procure", "procurement", "vendor")) {
            return sourcingQuestionResponse(response, lowercase, scopedRecords, metrics, scopedLabel);
        }

        if (containsAny(lowercase, "highest order", "largest order", "biggest order", "top order", "lowest order", "smallest order")) {
            return orderInsightResponse(response, lowercase, scopedRecords, scopedLabel);
        }

        if (containsAny(lowercase, "what should", "attention", "watchout", "risk", "important insight", "key insight")) {
            response.put("answer", buildNarrativeSummary(scopedRecords, metrics, scopedLabel));
            response.put("interpretation", buildRiskSummary(scopedRecords, metrics));
            response.put("table", toContributionTable("Driver", aggregateMetric(scopedRecords, Dimension.CATEGORY, Metric.SALES), metrics.totalSales(), 5));
            response.put("chart", buildChartPayload(
                    "Category contribution",
                    "bar",
                    aggregateMetric(scopedRecords, Dimension.CATEGORY, Metric.SALES),
                    "category",
                    Metric.SALES,
                    Math.min(resultLimit, 8),
                    false));
            return response;
        }

        if (containsAny(lowercase, "trend", "over time", "by date", "daily", "timeline")) {
            Map<String, Double> trend = aggregateMetric(scopedRecords, Dimension.DATE, Metric.SALES);
            response.put("answer", "Sales trend for " + scopedLabel + " is shown below.");
            response.put("interpretation", describeTrend(trend));
            response.put("table", toMetricTable("Date", "Sales", trend, Math.min(resultLimit, 30), true));
            response.put("chart", buildChartPayload("Sales trend", "line", trend, "date", Metric.SALES, Math.min(resultLimit, 30), true));
            return response;
        }

        if (context.dimension() == null) {
            if (containsAny(lowercase, "health", "healthy", "doing", "performing", "performance", "overview", "summary")) {
                response.put("answer", buildOpenEndedAnswer(lowercase, scopedRecords, metrics, scopedLabel));
                response.put("interpretation", buildOpenEndedInterpretation(lowercase, scopedRecords, metrics));
                response.put("chart", buildChartPayload(
                        "Revenue by category",
                        "bar",
                        aggregateMetric(scopedRecords, Dimension.CATEGORY, Metric.SALES),
                        "category",
                        Metric.SALES,
                        Math.min(resultLimit, 8),
                        false));
                response.put("table", toContributionTable("Category", aggregateMetric(scopedRecords, Dimension.CATEGORY, Metric.SALES), metrics.totalSales(), Math.min(resultLimit, 8)));
                return response;
            }

            if (containsAny(lowercase, "total", "overall", "how much", "how many", "average")) {
                response.put("answer", singleMetricAnswer(context.metric(), metrics, scopedLabel));
                response.put("interpretation", buildMetricInterpretation(context.metric(), scopedRecords, metrics));
                return response;
            }

            response.put("answer", buildOpenEndedAnswer(lowercase, scopedRecords, metrics, scopedLabel));
            response.put("interpretation", buildOpenEndedInterpretation(lowercase, scopedRecords, metrics));
            response.put("chart", buildChartPayload(
                    "Revenue by category",
                    "bar",
                    aggregateMetric(scopedRecords, Dimension.CATEGORY, Metric.SALES),
                    "category",
                    Metric.SALES,
                    Math.min(resultLimit, 8),
                    false));
            response.put("table", toContributionTable("Category", aggregateMetric(scopedRecords, Dimension.CATEGORY, Metric.SALES), metrics.totalSales(), Math.min(resultLimit, 8)));
            return response;
        }

        Map<String, Double> groupedMetric = aggregateMetric(scopedRecords, context.dimension(), context.metric());
        if (groupedMetric.isEmpty()) {
            response.put("answer", "No grouped data was available for that question.");
            return response;
        }

        int topN = containsNumber(lowercase) ? extractTopN(lowercase, resultLimit) : resultLimit;
        String dimensionLabel = context.dimension().label();
        String metricLabel = context.metric().label();
        boolean explicitListRequest = containsAny(lowercase, "top ", "bottom ", "show", "list", "compare", "breakdown", "split", "versus", "vs");
        boolean singularTopRequest = containsAny(lowercase, "highest", "strongest", "best", "leading", "most")
                && !explicitListRequest;
        boolean singularBottomRequest = containsAny(lowercase, "lowest", "weakest", "worst", "least")
                && !explicitListRequest;

        if (singularBottomRequest) {
            Map.Entry<String, Double> lowestValue = bottomEntry(groupedMetric);
            response.put("answer", lowestValue == null
                    ? "No grouped data was available for that question."
                    : lowestValue.getKey() + " has the lowest " + metricLabel.toLowerCase(Locale.ROOT)
                            + " for " + scopedLabel + " at " + formatMetricValue(lowestValue.getValue(), context.metric()) + ".");
            response.put("interpretation", summarizeExtremes(bottomEntries(groupedMetric, Math.min(resultLimit, 8)), false, context.metric()));
            response.put("table", toMetricTable(dimensionLabel, metricLabel, bottomEntries(groupedMetric, Math.min(resultLimit, 8)), Math.min(resultLimit, 8), false));
            response.put("chart", buildChartPayload(
                    "Lowest " + dimensionLabel + " values",
                    "bar",
                    bottomEntries(groupedMetric, Math.min(resultLimit, 8)),
                    dimensionFilterKey(context.dimension()),
                    context.metric(),
                    Math.min(resultLimit, 8),
                    false));
            return response;
        }

        if (containsAny(lowercase, "bottom") || explicitListRequest && containsAny(lowercase, "lowest", "worst", "least")) {
            Map<String, Double> bottomValues = bottomEntries(groupedMetric, resultLimit);
            response.put("answer", "These are the lowest " + topN + " " + dimensionLabel.toLowerCase(Locale.ROOT)
                    + " values by " + metricLabel.toLowerCase(Locale.ROOT) + " for " + scopedLabel + ".");
            response.put("interpretation", summarizeExtremes(bottomValues, false, context.metric()));
            response.put("table", toMetricTable(dimensionLabel, metricLabel, bottomValues, resultLimit, false));
            response.put("chart", buildChartPayload(
                    "Lowest " + dimensionLabel + " values",
                    "bar",
                    bottomValues,
                    dimensionFilterKey(context.dimension()),
                    context.metric(),
                    resultLimit,
                    false));
            return response;
        }

        if (singularTopRequest) {
            Map.Entry<String, Double> highestValue = topEntry(groupedMetric);
            response.put("answer", highestValue == null
                    ? "No grouped data was available for that question."
                    : highestValue.getKey() + " has the highest " + metricLabel.toLowerCase(Locale.ROOT)
                            + " for " + scopedLabel + " at " + formatMetricValue(highestValue.getValue(), context.metric()) + ".");
            response.put("interpretation", summarizeExtremes(topEntries(groupedMetric, Math.min(resultLimit, 8)), true, context.metric()));
            response.put("table", toMetricTable(dimensionLabel, metricLabel, topEntries(groupedMetric, Math.min(resultLimit, 8)), Math.min(resultLimit, 8), false));
            response.put("chart", buildChartPayload(
                    "Highest " + dimensionLabel + " values",
                    "bar",
                    topEntries(groupedMetric, Math.min(resultLimit, 8)),
                    dimensionFilterKey(context.dimension()),
                    context.metric(),
                    Math.min(resultLimit, 8),
                    false));
            return response;
        }

        if (containsAny(lowercase, "top") || explicitListRequest && containsAny(lowercase, "highest", "best", "leading", "strongest", "most")) {
            Map<String, Double> topValues = topEntries(groupedMetric, resultLimit);
            response.put("answer", "These are the top " + topN + " " + dimensionLabel.toLowerCase(Locale.ROOT)
                    + " values by " + metricLabel.toLowerCase(Locale.ROOT) + " for " + scopedLabel + ".");
            response.put("interpretation", summarizeExtremes(topValues, true, context.metric()));
            response.put("table", toMetricTable(dimensionLabel, metricLabel, topValues, resultLimit, false));
            response.put("chart", buildChartPayload(
                    "Top " + dimensionLabel + " values",
                    "bar",
                    topValues,
                    dimensionFilterKey(context.dimension()),
                    context.metric(),
                    resultLimit,
                    false));
            return response;
        }

        if (containsAny(lowercase, "share", "contribution", "contribute", "mix")) {
            response.put("answer", "This shows how each " + dimensionLabel.toLowerCase(Locale.ROOT)
                    + " contributes to total sales for " + scopedLabel + ".");
            response.put("interpretation", contributionInterpretation(groupedMetric, metrics.totalSales()));
            response.put("table", toContributionTable(dimensionLabel, groupedMetric, metrics.totalSales(), resultLimit));
            response.put("chart", buildChartPayload(
                    dimensionLabel + " contribution",
                    "bar",
                    topEntries(groupedMetric, resultLimit),
                    dimensionFilterKey(context.dimension()),
                    context.metric(),
                    resultLimit,
                    false));
            return response;
        }

        if (containsAny(lowercase, "compare", "versus", "vs", "breakdown", "split", "list", "show")) {
            response.put("answer", "Here is the " + metricLabel.toLowerCase(Locale.ROOT) + " breakdown by "
                    + dimensionLabel.toLowerCase(Locale.ROOT) + " for " + scopedLabel + ".");
            response.put("interpretation", summarizeExtremes(topEntries(groupedMetric, Math.min(resultLimit, 8)), true, context.metric()));
            response.put("table", toMetricTable(dimensionLabel, metricLabel, groupedMetric, resultLimit, false));
            response.put("chart", buildChartPayload(
                    metricLabel + " by " + dimensionLabel,
                    context.dimension() == Dimension.DATE ? "line" : "bar",
                    groupedMetric,
                    dimensionFilterKey(context.dimension()),
                    context.metric(),
                    resultLimit,
                    context.dimension() == Dimension.DATE));
            return response;
        }

        response.put("answer", singleMetricAnswer(context.metric(), metrics, scopedLabel));
        response.put("interpretation", "If you want the answer split further, ask by city, category, product, customer, payment method, or date.");
        return response;
    }

    public DownloadFile getDownloadFile(String name) {
        return switch (name) {
            case "raw-input" -> new DownloadFile("retail.csv", readResourceBytes("retail.csv"));
            case "fact-sales" -> fileDownload("FactSales.csv");
            case "dim-date" -> fileDownload("DimDate.csv");
            case "dim-customer" -> fileDownload("DimCustomer.csv");
            case "dim-product" -> fileDownload("DimProduct.csv");
            case "dim-city" -> fileDownload("DimCity.csv");
            case "dim-payment" -> fileDownload("DimPayment.csv");
            case "dim-status" -> fileDownload("DimStatus.csv");
            case "retail-results" -> fileDownload("RetailResults.csv");
            default -> throw new IllegalArgumentException("Unknown table: " + name);
        };
    }

    private void writeOutputs() throws IOException {
        Set<String> uniqueCustomers = analyzer.getUniqueCustomers(warehouse);
        Set<String> uniqueProducts = analyzer.getUniqueProducts(warehouse);
        double totalSales = analyzer.getTotalSales(warehouse);
        Map<String, Double> salesByProduct = analyzer.getSalesByProduct(warehouse);
        Map<String, List<RetailRecord>> ordersByCustomer = analyzer.getOrdersByCustomer(warehouse);
        Map<String, Double> salesByCity = analyzer.getSalesByCity(warehouse);
        Map<String, Double> salesByPaymentMethod = analyzer.getSalesByPaymentMethod(warehouse);
        Map<String, Double> salesTrendByDate = analyzer.getSalesTrendByDate(warehouse);
        Map<String, Integer> orderCountByStatus = analyzer.getOrderCountByStatus(warehouse);
        Map<String, Double> salesByStatus = analyzer.getSalesByStatus(warehouse);
        new RetailResultsWriter().writeResults(
                outputDirectory.resolve("RetailResults.csv").toString(),
                analyzer.getDetailedRecords(warehouse),
                uniqueCustomers,
                uniqueProducts,
                totalSales,
                salesByProduct,
                ordersByCustomer,
                salesByCity,
                salesByPaymentMethod,
                salesTrendByDate,
                orderCountByStatus,
                salesByStatus);
        new RetailWarehouseWriter().writeWarehouseTables(outputDirectory, warehouse);
    }

    private List<RetailRecord> filterRecords(Map<String, String> filters) {
        String city = filters.getOrDefault("city", "");
        String category = filters.getOrDefault("category", "");
        String product = filters.getOrDefault("product", "");
        String paymentMethod = filters.getOrDefault("paymentMethod", "");
        String status = filters.getOrDefault("status", "");
        String customer = filters.getOrDefault("customer", "");
        String dateFrom = filters.getOrDefault("dateFrom", "");
        String dateTo = filters.getOrDefault("dateTo", "");

        return records.stream()
                .filter(record -> city.isBlank() || record.getCity().equalsIgnoreCase(city))
                .filter(record -> category.isBlank() || record.getCategory().equalsIgnoreCase(category))
                .filter(record -> product.isBlank() || record.getProduct().equalsIgnoreCase(product))
                .filter(record -> paymentMethod.isBlank() || record.getPaymentMethod().equalsIgnoreCase(paymentMethod))
                .filter(record -> status.isBlank() || record.getOrderStatus().equalsIgnoreCase(status))
                .filter(record -> customer.isBlank() || record.getCustomer().equalsIgnoreCase(customer))
                .filter(record -> dateFrom.isBlank() || !LocalDate.parse(record.getOrderDate()).isBefore(LocalDate.parse(dateFrom)))
                .filter(record -> dateTo.isBlank() || !LocalDate.parse(record.getOrderDate()).isAfter(LocalDate.parse(dateTo)))
                .toList();
    }

    private QueryContext buildQueryContext(String query, List<RetailRecord> baseRecords) {
        String lowercase = query.toLowerCase(Locale.ROOT);
        Dimension dimension = inferDimension(lowercase);
        Metric metric = inferMetric(lowercase);
        List<RetailRecord> scopedRecords = baseRecords;
        List<String> scopeParts = new ArrayList<>();

        for (EntityMatch match : findEntityMatches(lowercase)) {
            scopedRecords = scopedRecords.stream()
                    .filter(record -> match.matches(record))
                    .toList();
            scopeParts.add(match.label() + " " + match.value());
        }

        String scopeDescription = scopeParts.isEmpty()
                ? "the current selection"
                : "the selection filtered to " + String.join(", ", scopeParts);

        return new QueryContext(dimension, metric, scopedRecords, scopeDescription);
    }

    private Dimension inferDimension(String query) {
        if (containsAny(query, "product", "products", "item", "items")) {
            return Dimension.PRODUCT;
        }
        if (containsAny(query, "category", "categories")) {
            return Dimension.CATEGORY;
        }
        if (containsAny(query, "city", "cities", "location", "locations", "market", "markets")) {
            return Dimension.CITY;
        }
        if (containsAny(query, "customer", "customers", "buyer", "buyers")) {
            return Dimension.CUSTOMER;
        }
        if (containsAny(query, "payment", "upi", "card", "cash")) {
            return Dimension.PAYMENT;
        }
        if (containsAny(query, "status", "delivered", "cancelled", "returned")) {
            return Dimension.STATUS;
        }
        if (containsAny(query, "date", "day", "month", "trend", "timeline", "daily")) {
            return Dimension.DATE;
        }
        return null;
    }

    private Metric inferMetric(String query) {
        if (containsAny(query, "quantity", "units")) {
            return Metric.QUANTITY;
        }
        if (containsAny(query, "discount")) {
            return Metric.AVG_DISCOUNT;
        }
        if (containsAny(query, "average order value", "aov")) {
            return Metric.AOV;
        }
        if (containsAny(query, "customers", "customer count")) {
            return Metric.CUSTOMERS;
        }
        if (containsAny(query, "products", "product count")) {
            return Metric.PRODUCTS;
        }
        if (containsAny(query, "order", "orders")) {
            return Metric.ORDERS;
        }
        return Metric.SALES;
    }

    private List<EntityMatch> findEntityMatches(String query) {
        List<EntityMatch> matches = new ArrayList<>();
        addEntityMatch(matches, query, sortedDistinct(records, RetailRecord::getCity), "city", MatchType.CITY);
        addEntityMatch(matches, query, sortedDistinct(records, RetailRecord::getCategory), "category", MatchType.CATEGORY);
        addEntityMatch(matches, query, sortedDistinct(records, RetailRecord::getProduct), "product", MatchType.PRODUCT);
        addEntityMatch(matches, query, sortedDistinct(records, RetailRecord::getCustomer), "customer", MatchType.CUSTOMER);
        addEntityMatch(matches, query, sortedDistinct(records, RetailRecord::getPaymentMethod), "payment method", MatchType.PAYMENT);
        addEntityMatch(matches, query, sortedDistinct(records, RetailRecord::getOrderStatus), "status", MatchType.STATUS);
        return matches;
    }

    private List<EntityMatch> findAllEntityMatches(String query) {
        List<EntityMatch> matches = new ArrayList<>();
        addAllEntityMatches(matches, query, sortedDistinct(records, RetailRecord::getCity), "city", MatchType.CITY);
        addAllEntityMatches(matches, query, sortedDistinct(records, RetailRecord::getCategory), "category", MatchType.CATEGORY);
        addAllEntityMatches(matches, query, sortedDistinct(records, RetailRecord::getProduct), "product", MatchType.PRODUCT);
        addAllEntityMatches(matches, query, sortedDistinct(records, RetailRecord::getCustomer), "customer", MatchType.CUSTOMER);
        addAllEntityMatches(matches, query, sortedDistinct(records, RetailRecord::getPaymentMethod), "payment method", MatchType.PAYMENT);
        addAllEntityMatches(matches, query, sortedDistinct(records, RetailRecord::getOrderStatus), "status", MatchType.STATUS);
        return matches;
    }

    private void addEntityMatch(List<EntityMatch> matches, String query, List<String> values, String label, MatchType type) {
        for (String value : values) {
            if (query.contains(value.toLowerCase(Locale.ROOT))) {
                matches.add(new EntityMatch(type, label, value));
                return;
            }
        }
    }

    private void addAllEntityMatches(List<EntityMatch> matches, String query, List<String> values, String label, MatchType type) {
        for (String value : values) {
            if (query.contains(value.toLowerCase(Locale.ROOT))) {
                matches.add(new EntityMatch(type, label, value));
            }
        }
    }

    private MetricSnapshot snapshot(List<RetailRecord> source) {
        double totalSales = source.stream().mapToDouble(RetailRecord::getSalesAmount).sum();
        int totalOrders = (int) source.stream().map(RetailRecord::getOrderId).distinct().count();
        int totalQuantity = source.stream().mapToInt(RetailRecord::getQuantity).sum();
        double averageOrderValue = totalOrders == 0 ? 0 : totalSales / totalOrders;
        double averageDiscount = source.isEmpty() ? 0 : source.stream().mapToDouble(RetailRecord::getDiscount).average().orElse(0);
        int uniqueCustomers = countDistinct(source, RetailRecord::getCustomer);
        int uniqueProducts = countDistinct(source, RetailRecord::getProduct);
        return new MetricSnapshot(totalSales, totalOrders, totalQuantity, averageOrderValue, averageDiscount, uniqueCustomers, uniqueProducts);
    }

    private Map<String, Double> aggregateMetric(List<RetailRecord> source, Dimension dimension, Metric metric) {
        Map<String, List<RetailRecord>> grouped = new TreeMap<>();
        for (RetailRecord record : source) {
            grouped.computeIfAbsent(getDimensionValue(record, dimension), key -> new ArrayList<>()).add(record);
        }

        Map<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<RetailRecord>> entry : grouped.entrySet()) {
            result.put(entry.getKey(), calculateMetric(entry.getValue(), metric));
        }
        return result;
    }

    private Map<Integer, Double> aggregateOrderValues(List<RetailRecord> source) {
        Map<Integer, Double> values = new TreeMap<>();
        for (RetailRecord record : source) {
            values.put(record.getOrderId(), values.getOrDefault(record.getOrderId(), 0.0) + record.getSalesAmount());
        }
        return values;
    }

    private double calculateMetric(List<RetailRecord> source, Metric metric) {
        return switch (metric) {
            case SALES -> source.stream().mapToDouble(RetailRecord::getSalesAmount).sum();
            case ORDERS -> source.stream().map(RetailRecord::getOrderId).distinct().count();
            case QUANTITY -> source.stream().mapToInt(RetailRecord::getQuantity).sum();
            case AVG_DISCOUNT -> source.isEmpty() ? 0 : source.stream().mapToDouble(RetailRecord::getDiscount).average().orElse(0);
            case AOV -> {
                int orderCount = (int) source.stream().map(RetailRecord::getOrderId).distinct().count();
                double totalSales = source.stream().mapToDouble(RetailRecord::getSalesAmount).sum();
                yield orderCount == 0 ? 0 : totalSales / orderCount;
            }
            case CUSTOMERS -> source.stream().map(RetailRecord::getCustomer).distinct().count();
            case PRODUCTS -> source.stream().map(RetailRecord::getProduct).distinct().count();
        };
    }

    private String getDimensionValue(RetailRecord record, Dimension dimension) {
        return switch (dimension) {
            case DATE -> record.getOrderDate();
            case CITY -> record.getCity();
            case CATEGORY -> record.getCategory();
            case PRODUCT -> record.getProduct();
            case CUSTOMER -> record.getCustomer();
            case PAYMENT -> record.getPaymentMethod();
            case STATUS -> record.getOrderStatus();
        };
    }

    private List<Map<String, Object>> buildTopInsights(List<RetailRecord> filteredRecords, MetricSnapshot metrics) {
        List<Map<String, Object>> insights = new ArrayList<>();
        Map.Entry<String, Double> topCategory = topEntry(aggregateMetric(filteredRecords, Dimension.CATEGORY, Metric.SALES));
        Map.Entry<String, Double> topCity = topEntry(aggregateMetric(filteredRecords, Dimension.CITY, Metric.SALES));
        Map.Entry<String, Double> topProduct = topEntry(aggregateMetric(filteredRecords, Dimension.PRODUCT, Metric.SALES));
        Map.Entry<String, Double> topCustomer = topEntry(aggregateMetric(filteredRecords, Dimension.CUSTOMER, Metric.SALES));

        if (topCategory != null) {
            insights.add(insight("Revenue Leader", topCategory.getKey(), formatCurrency(topCategory.getValue()) + " from the strongest category"));
        }
        if (topCity != null) {
            insights.add(insight("Best Market", topCity.getKey(), formatContribution(topCity.getValue(), metrics.totalSales()) + " of filtered sales"));
        }
        if (topProduct != null) {
            insights.add(insight("Hero Product", topProduct.getKey(), formatCurrency(topProduct.getValue()) + " in revenue"));
        }
        if (topCustomer != null) {
            insights.add(insight("Top Customer", topCustomer.getKey(), formatCurrency(topCustomer.getValue()) + " contributed"));
        }
        return insights;
    }

    private List<Map<String, Object>> buildWatchouts(List<RetailRecord> filteredRecords, MetricSnapshot metrics) {
        List<Map<String, Object>> watchouts = new ArrayList<>();
        Map<String, Double> productSales = aggregateMetric(filteredRecords, Dimension.PRODUCT, Metric.SALES);
        Map.Entry<String, Double> topProduct = topEntry(productSales);
        Map.Entry<String, Double> bottomProduct = bottomEntry(productSales);
        Map.Entry<String, Double> highDiscountCategory = topEntry(aggregateMetric(filteredRecords, Dimension.CATEGORY, Metric.AVG_DISCOUNT));

        if (topProduct != null && metrics.totalSales() > 0) {
            double concentration = topProduct.getValue() / metrics.totalSales();
            if (concentration >= 0.30) {
                watchouts.add(insight("Revenue Concentration", topProduct.getKey(), formatPercentage(concentration) + " of sales come from one product"));
            }
        }
        if (highDiscountCategory != null && highDiscountCategory.getValue() >= 10) {
            watchouts.add(insight("Heavy Discounting", highDiscountCategory.getKey(), round(highDiscountCategory.getValue()) + "% average discount"));
        }
        if (bottomProduct != null) {
            watchouts.add(insight("Low Performer", bottomProduct.getKey(), formatCurrency(bottomProduct.getValue()) + " in sales"));
        }
        if (metrics.averageOrderValue() < 10000) {
            watchouts.add(insight("Order Value Pressure", "Average order value", formatCurrency(metrics.averageOrderValue()) + " needs monitoring"));
        }
        return watchouts;
    }

    private Map<String, Object> buildTrendSection(List<RetailRecord> filteredRecords, Map<String, String> filters) {
        Map<String, Double> trend = aggregateMetric(filteredRecords, Dimension.DATE, Metric.SALES);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("summary", describeTrend(trend));
        payload.put("highlights", buildTrendHighlights(trend, filteredRecords, filters));
        String dateFrom = filters.getOrDefault("dateFrom", "");
        String dateTo = filters.getOrDefault("dateTo", "");
        if (!dateFrom.isBlank() && dateFrom.equals(dateTo)) {
            payload.put("focusedDate", dateFrom);
        }
        return payload;
    }

    private List<Map<String, Object>> buildTrendHighlights(Map<String, Double> trend, List<RetailRecord> filteredRecords, Map<String, String> filters) {
        List<Map<String, Object>> highlights = new ArrayList<>();
        if (trend.isEmpty()) {
            return highlights;
        }

        Map.Entry<String, Double> peakDay = topEntry(trend);
        Map.Entry<String, Double> weakestDay = bottomEntry(trend);
        List<Map.Entry<String, Double>> entries = new ArrayList<>(trend.entrySet());
        Map.Entry<String, Double> first = entries.get(0);
        Map.Entry<String, Double> last = entries.get(entries.size() - 1);

        if (peakDay != null) {
            highlights.add(insight("Peak Day", peakDay.getKey(), formatCurrency(peakDay.getValue()) + " in revenue"));
        }
        if (weakestDay != null) {
            highlights.add(insight("Lowest Day", weakestDay.getKey(), formatCurrency(weakestDay.getValue()) + " in revenue"));
        }
        highlights.add(insight(
                "Period Movement",
                changeDirection(last.getValue() - first.getValue()),
                "Moved from " + formatCurrency(first.getValue()) + " to " + formatCurrency(last.getValue())));

        String dateFrom = filters.getOrDefault("dateFrom", "");
        String dateTo = filters.getOrDefault("dateTo", "");
        if (!dateFrom.isBlank() && dateFrom.equals(dateTo)) {
            MetricSnapshot metrics = snapshot(filteredRecords);
            Map.Entry<String, Double> topCategory = topEntry(aggregateMetric(filteredRecords, Dimension.CATEGORY, Metric.SALES));
            Map.Entry<String, Double> topProduct = topEntry(aggregateMetric(filteredRecords, Dimension.PRODUCT, Metric.SALES));
            highlights.clear();
            highlights.add(insight("Focused Date Revenue", formatCurrency(metrics.totalSales()), "Revenue captured on " + dateFrom));
            highlights.add(insight("Orders On Date", String.valueOf(metrics.totalOrders()), "Distinct orders for the selected day"));
            if (topCategory != null) {
                highlights.add(insight("Leading Category", topCategory.getKey(), formatCurrency(topCategory.getValue()) + " on this date"));
            }
            if (topProduct != null) {
                highlights.add(insight("Top Product", topProduct.getKey(), formatCurrency(topProduct.getValue()) + " on this date"));
            }
        }
        return highlights;
    }

    private Map<String, Object> insight(String title, String value, String detail) {
        Map<String, Object> insight = new LinkedHashMap<>();
        insight.put("title", title);
        insight.put("value", value);
        insight.put("detail", detail);
        return insight;
    }

    private String buildNarrativeSummary(List<RetailRecord> source, MetricSnapshot metrics, String scopeLabel) {
        Map.Entry<String, Double> topCategory = topEntry(aggregateMetric(source, Dimension.CATEGORY, Metric.SALES));
        Map.Entry<String, Double> topCity = topEntry(aggregateMetric(source, Dimension.CITY, Metric.SALES));
        Map.Entry<String, Double> topPayment = topEntry(aggregateMetric(source, Dimension.PAYMENT, Metric.SALES));

        String categoryText = topCategory == null ? "No category leader is available" : topCategory.getKey() + " leads category revenue";
        String cityText = topCity == null ? "no city lead is available" : topCity.getKey() + " is the strongest market";
        String paymentText = topPayment == null ? "payment mix is unavailable" : topPayment.getKey() + " is the leading payment method";

        return "For " + scopeLabel + ", revenue is " + formatCurrency(metrics.totalSales())
                + " across " + metrics.totalOrders() + " orders. " + categoryText + ", "
                + cityText + ", and " + paymentText + ".";
    }

    private String buildRiskSummary(List<RetailRecord> source, MetricSnapshot metrics) {
        List<String> notes = new ArrayList<>();
        Map.Entry<String, Double> topProduct = topEntry(aggregateMetric(source, Dimension.PRODUCT, Metric.SALES));
        if (topProduct != null && metrics.totalSales() > 0) {
            double share = topProduct.getValue() / metrics.totalSales();
            if (share >= 0.30) {
                notes.add(topProduct.getKey() + " alone drives " + formatPercentage(share) + " of revenue");
            }
        }
        Map.Entry<String, Double> topDiscountCategory = topEntry(aggregateMetric(source, Dimension.CATEGORY, Metric.AVG_DISCOUNT));
        if (topDiscountCategory != null && topDiscountCategory.getValue() >= 10) {
            notes.add(topDiscountCategory.getKey() + " carries a high average discount of " + round(topDiscountCategory.getValue()) + "%");
        }
        if (notes.isEmpty()) {
            return "No immediate concentration or discount risk stands out from the current selection.";
        }
        return String.join(". ", notes) + ".";
    }

    private String buildOpenEndedAnswer(String lowercase, List<RetailRecord> source, MetricSnapshot metrics, String scopedLabel) {
        if (containsAny(lowercase, "health", "healthy", "doing", "performing", "performance", "overview", "summary")) {
            return buildExecutiveHealthSummary(source, metrics, scopedLabel);
        }
        if (containsAny(lowercase, "why", "explain", "insight", "summary", "overview", "performance")) {
            return buildNarrativeSummary(source, metrics, scopedLabel);
        }
        return buildNarrativeSummary(source, metrics, scopedLabel);
    }

    private String buildOpenEndedInterpretation(String lowercase, List<RetailRecord> source, MetricSnapshot metrics) {
        if (containsAny(lowercase, "health", "healthy", "doing", "performing", "performance", "overview", "summary")) {
            return buildExecutiveRecommendation(source, metrics);
        }
        if (containsAny(lowercase, "why", "explain")) {
            Map.Entry<String, Double> topCategory = topEntry(aggregateMetric(source, Dimension.CATEGORY, Metric.SALES));
            Map.Entry<String, Double> topCity = topEntry(aggregateMetric(source, Dimension.CITY, Metric.SALES));
            return "The main drivers appear to be "
                    + safeKey(topCategory) + " on the category side and "
                    + safeKey(topCity) + " on the market side.";
        }
        return buildRiskSummary(source, metrics);
    }

    private String buildExecutiveHealthSummary(List<RetailRecord> source, MetricSnapshot metrics, String scopedLabel) {
        Map.Entry<String, Double> topCategory = topEntry(aggregateMetric(source, Dimension.CATEGORY, Metric.SALES));
        Map.Entry<String, Double> topCity = topEntry(aggregateMetric(source, Dimension.CITY, Metric.SALES));
        Map.Entry<String, Double> topCustomer = topEntry(aggregateMetric(source, Dimension.CUSTOMER, Metric.SALES));
        return "For " + scopedLabel + ", the business is generating " + formatCurrency(metrics.totalSales())
                + " from " + metrics.totalOrders() + " orders and " + metrics.uniqueCustomers()
                + " active customers. The current leaders are " + safeKey(topCategory)
                + " by category, " + safeKey(topCity) + " by market, and " + safeKey(topCustomer)
                + " at the customer level.";
    }

    private String buildExecutiveRecommendation(List<RetailRecord> source, MetricSnapshot metrics) {
        Map.Entry<String, Double> topProduct = topEntry(aggregateMetric(source, Dimension.PRODUCT, Metric.SALES));
        Map.Entry<String, Double> topDiscountCategory = topEntry(aggregateMetric(source, Dimension.CATEGORY, Metric.AVG_DISCOUNT));
        String concentrationRisk = buildRiskSummary(source, metrics);
        return "The immediate priority is to protect the strongest revenue driver, currently "
                + safeKey(topProduct) + ", while also reducing over-reliance on any single winner. "
                + (topDiscountCategory == null ? ""
                : topDiscountCategory.getKey() + " should be watched for discount pressure. ")
                + concentrationRisk;
    }

    private Map<String, Object> profitQuestionResponse(
            Map<String, Object> response,
            List<RetailRecord> scopedRecords,
            MetricSnapshot metrics,
            String scopedLabel) {
        response.put("answer",
                "I cannot calculate true profit for " + scopedLabel
                        + " because the dataset contains revenue, quantity, price, discount, customer, city, and payment data, but no cost, margin, supplier, or expense fields.");
        response.put("interpretation",
                "What I can show reliably is revenue after discount, discount intensity, and the products or categories driving sales. Reported revenue here is "
                        + formatCurrency(metrics.totalSales()) + ".");
        response.put("table", toContributionTable("Category", aggregateMetric(scopedRecords, Dimension.CATEGORY, Metric.SALES), metrics.totalSales(), 8));
        response.put("chart", buildChartPayload(
                "Revenue by category",
                "bar",
                aggregateMetric(scopedRecords, Dimension.CATEGORY, Metric.SALES),
                "category",
                Metric.SALES,
                8,
                false));
        return response;
    }

    private Map<String, Object> investmentQuestionResponse(
            Map<String, Object> response,
            List<RetailRecord> scopedRecords,
            MetricSnapshot metrics,
            String scopedLabel) {
        Map.Entry<String, Double> topCategory = topEntry(aggregateMetric(scopedRecords, Dimension.CATEGORY, Metric.SALES));
        Map.Entry<String, Double> topCity = topEntry(aggregateMetric(scopedRecords, Dimension.CITY, Metric.SALES));
        Map.Entry<String, Double> topProduct = topEntry(aggregateMetric(scopedRecords, Dimension.PRODUCT, Metric.SALES));
        response.put("answer",
                "Based on revenue in " + scopedLabel + ", the strongest investment signals are "
                        + safeKey(topCategory) + " as a category, "
                        + safeKey(topCity) + " as a market, and "
                        + safeKey(topProduct) + " as a product focus.");
        response.put("interpretation",
                "This is a revenue-led recommendation, not a profitability recommendation, because the dataset does not include costs or margins.");
        response.put("table", toContributionTable("Category", aggregateMetric(scopedRecords, Dimension.CATEGORY, Metric.SALES), metrics.totalSales(), 8));
        response.put("chart", buildChartPayload(
                "Revenue by category",
                "bar",
                aggregateMetric(scopedRecords, Dimension.CATEGORY, Metric.SALES),
                "category",
                Metric.SALES,
                8,
                false));
        return response;
    }

    private Map<String, Object> bestCustomerResponse(
            Map<String, Object> response,
            List<RetailRecord> scopedRecords,
            String scopedLabel) {
        Map<String, Double> customerSales = aggregateMetric(scopedRecords, Dimension.CUSTOMER, Metric.SALES);
        Map.Entry<String, Double> topCustomer = topEntry(customerSales);
        response.put("answer",
                topCustomer == null
                        ? "No customer information is available for that question."
                        : topCustomer.getKey() + " is the best customer in " + scopedLabel + " with "
                                + formatCurrency(topCustomer.getValue()) + " in revenue.");
        response.put("interpretation",
                "This is a revenue-based view of customer value. If customer profitability or retention is important, that would require cost or repeat-purchase history beyond the current dataset.");
        response.put("table", toMetricTable("Customer", "Sales", customerSales, 8, false));
        response.put("chart", buildChartPayload(
                "Customer revenue",
                "bar",
                customerSales,
                "customer",
                Metric.SALES,
                8,
                false));
        return response;
    }

    private Map<String, Object> bestMarketResponse(
            Map<String, Object> response,
            List<RetailRecord> scopedRecords,
            String scopedLabel) {
        Map<String, Double> citySales = aggregateMetric(scopedRecords, Dimension.CITY, Metric.SALES);
        Map.Entry<String, Double> topCity = topEntry(citySales);
        response.put("answer",
                topCity == null
                        ? "No city information is available for that question."
                        : topCity.getKey() + " is the strongest market in " + scopedLabel + " with "
                                + formatCurrency(topCity.getValue()) + " in revenue.");
        response.put("interpretation",
                "This points to where demand is strongest. A business owner would usually protect this market first, then decide whether to replicate that performance in weaker cities.");
        response.put("table", toContributionTable("City", citySales, snapshot(scopedRecords).totalSales(), 8));
        response.put("chart", buildChartPayload(
                "City revenue",
                "bar",
                citySales,
                "city",
                Metric.SALES,
                8,
                false));
        return response;
    }

    private Map<String, Object> improvementQuestionResponse(
            Map<String, Object> response,
            List<RetailRecord> scopedRecords,
            MetricSnapshot metrics,
            String scopedLabel) {
        Map<String, Double> bottomCategories = bottomEntries(aggregateMetric(scopedRecords, Dimension.CATEGORY, Metric.SALES), 5);
        Map<String, Double> topCities = topEntries(aggregateMetric(scopedRecords, Dimension.CITY, Metric.SALES), 5);
        Map.Entry<String, Double> highDiscountCategory = topEntry(aggregateMetric(scopedRecords, Dimension.CATEGORY, Metric.AVG_DISCOUNT));
        response.put("answer",
                "To improve sales in " + scopedLabel
                        + ", the clearest moves are to scale what is already working, repair weak categories, and reduce unnecessary discount dependence.");
        response.put("interpretation",
                "The strongest cities currently include " + String.join(", ", topCities.keySet())
                + ". The weaker categories are " + String.join(", ", bottomCategories.keySet())
                        + ". " + (highDiscountCategory == null ? ""
                        : highDiscountCategory.getKey() + " is the most discount-dependent category right now.")
                        + " A practical next step is to push assortment and promotion in the best cities while reviewing why the weakest categories are lagging.");
        response.put("table", toMetricTable("Category", "Sales", bottomCategories, 5, false));
        response.put("chart", buildChartPayload(
                "Lowest category sales",
                "bar",
                bottomCategories,
                "category",
                Metric.SALES,
                5,
                false));
        return response;
    }

    private Map<String, Object> strategyQuestionResponse(
            Map<String, Object> response,
            String lowercase,
            List<RetailRecord> scopedRecords,
            MetricSnapshot metrics,
            String scopedLabel) {
        Map.Entry<String, Double> topCategory = topEntry(aggregateMetric(scopedRecords, Dimension.CATEGORY, Metric.SALES));
        Map.Entry<String, Double> topCity = topEntry(aggregateMetric(scopedRecords, Dimension.CITY, Metric.SALES));
        Map<String, Double> weakCategories = bottomEntries(aggregateMetric(scopedRecords, Dimension.CATEGORY, Metric.SALES), 3);
        Map.Entry<String, Double> topCustomer = topEntry(aggregateMetric(scopedRecords, Dimension.CUSTOMER, Metric.SALES));

        String focus = containsAny(lowercase, "customer") ? "customer retention and account expansion"
                : containsAny(lowercase, "city", "market") ? "market expansion"
                : containsAny(lowercase, "product", "category") ? "assortment strategy"
                : "overall growth strategy";

        response.put("answer",
                "For " + scopedLabel + ", the best immediate strategy is " + focus
                        + ": protect the revenue already coming from " + safeKey(topCategory) + " and "
                        + safeKey(topCity) + ", then improve the weakest parts of the mix.");
        response.put("interpretation",
                "The strongest customer signal is " + safeKey(topCustomer) + ", while the categories needing the most attention are "
                        + String.join(", ", weakCategories.keySet())
                        + ". This suggests a two-track plan: defend the winners and fix the laggards.");
        response.put("table", toMetricTable("Category", "Sales", weakCategories, 3, false));
        response.put("chart", buildChartPayload(
                "Weakest category sales",
                "bar",
                weakCategories,
                "category",
                Metric.SALES,
                3,
                false));
        return response;
    }

    private Map<String, Object> sourcingQuestionResponse(
            Map<String, Object> response,
            String lowercase,
            List<RetailRecord> scopedRecords,
            MetricSnapshot metrics,
            String scopedLabel) {
        response.put("answer",
                "The dataset does not include supplier, procurement, vendor, or store-location information, so I cannot tell you where to buy a product from.");
        response.put("interpretation",
                "What I can show instead is where demand appears strongest in " + scopedLabel + ", which can help with stocking or sourcing priorities.");

        if (containsAny(lowercase, "product")) {
            response.put("table", toContributionTable("City", aggregateMetric(scopedRecords, Dimension.CITY, Metric.SALES), metrics.totalSales(), 8));
            response.put("chart", buildChartPayload(
                    "Revenue by city",
                    "bar",
                    aggregateMetric(scopedRecords, Dimension.CITY, Metric.SALES),
                    "city",
                    Metric.SALES,
                    8,
                    false));
        }
        return response;
    }

    private Map<String, Object> orderInsightResponse(
            Map<String, Object> response,
            String lowercase,
            List<RetailRecord> scopedRecords,
            String scopedLabel) {
        Map<Integer, Double> orderValues = aggregateOrderValues(scopedRecords);
        if (orderValues.isEmpty()) {
            response.put("answer", "No order-level data is available for that question.");
            return response;
        }

        boolean lowest = containsAny(lowercase, "lowest order", "smallest order");
        Map.Entry<Integer, Double> selected = lowest ? bottomOrderEntry(orderValues) : topOrderEntry(orderValues);
        response.put("answer",
                "Order " + selected.getKey() + " is the " + (lowest ? "lowest" : "highest")
                        + " value order for " + scopedLabel + " at " + formatCurrency(selected.getValue()) + ".");
        response.put("interpretation",
                "The table shows the highest and lowest order values so you can spot basket-size extremes.");
        response.put("table", orderExtremesTable(orderValues));
        return response;
    }

    private Map<String, Object> comparisonQuestionResponse(
            String originalQuery,
            String lowercase,
            List<RetailRecord> scopedRecords,
            Metric metric,
            String scopedLabel) {
        List<EntityMatch> matches = findAllEntityMatches(lowercase);
        if (matches.size() < 2) {
            return null;
        }

        MatchType matchType = matches.get(0).type();
        List<EntityMatch> comparable = matches.stream()
                .filter(match -> match.type() == matchType)
                .limit(6)
                .toList();
        if (comparable.size() < 2) {
            return null;
        }

        Map<String, Double> comparisonValues = new LinkedHashMap<>();
        for (EntityMatch match : comparable) {
            List<RetailRecord> matchedRecords = scopedRecords.stream().filter(match::matches).toList();
            comparisonValues.put(match.value(), calculateMetric(matchedRecords, metric));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", originalQuery);
        response.put("suggestions", SUGGESTED_QUESTIONS);
        response.put("recordCount", scopedRecords.size());
        response.put("answer",
                "Here is the comparison by " + metric.label().toLowerCase(Locale.ROOT)
                        + " for " + labelForMatchType(matchType).toLowerCase(Locale.ROOT) + " in " + scopedLabel + ".");
        response.put("interpretation", summarizeExtremes(comparisonValues, true, metric));
        response.put("table", toMetricTable(labelForMatchType(matchType), metric.label(), comparisonValues, comparable.size(), false));
        response.put("chart", buildChartPayload(
                metric.label() + " comparison",
                "bar",
                comparisonValues,
                filterKeyForMatchType(matchType),
                metric,
                comparable.size(),
                false));
        return response;
    }

    private String buildMetricInterpretation(Metric metric, List<RetailRecord> source, MetricSnapshot metrics) {
        return switch (metric) {
            case SALES -> "The current selection is being driven most by "
                    + safeKey(topEntry(aggregateMetric(source, Dimension.CATEGORY, Metric.SALES))) + ".";
            case ORDERS -> "That represents purchasing activity across " + metrics.activeCustomers() + " customers.";
            case QUANTITY -> "Units sold can help distinguish volume-driven sales from value-driven sales.";
            case AVG_DISCOUNT -> "Average discount helps show how much pricing support is needed to drive the current sales mix.";
            case AOV -> "Average order value is a useful proxy for basket strength.";
            case CUSTOMERS -> "Customer count shows how broad the demand base is in this selection.";
            case PRODUCTS -> "Product count shows how wide the active assortment is in this selection.";
        };
    }

    private String singleMetricAnswer(Metric metric, MetricSnapshot metrics, String scopedLabel) {
        return switch (metric) {
            case SALES -> "Total revenue for " + scopedLabel + " is " + formatCurrency(metrics.totalSales()) + ".";
            case ORDERS -> "Total orders for " + scopedLabel + " are " + metrics.totalOrders() + ".";
            case QUANTITY -> "Total units sold for " + scopedLabel + " are " + metrics.totalQuantity() + ".";
            case AVG_DISCOUNT -> "Average discount for " + scopedLabel + " is " + round(metrics.averageDiscount()) + "%.";
            case AOV -> "Average order value for " + scopedLabel + " is " + formatCurrency(metrics.averageOrderValue()) + ".";
            case CUSTOMERS -> "Active customers for " + scopedLabel + " total " + metrics.uniqueCustomers() + ".";
            case PRODUCTS -> "Active products for " + scopedLabel + " total " + metrics.uniqueProducts() + ".";
        };
    }

    private String describeTrend(Map<String, Double> trend) {
        if (trend.isEmpty()) {
            return "There is no time-series data available.";
        }
        List<Map.Entry<String, Double>> entries = new ArrayList<>(trend.entrySet());
        Map.Entry<String, Double> first = entries.get(0);
        Map.Entry<String, Double> last = entries.get(entries.size() - 1);
        double change = last.getValue() - first.getValue();
        String direction = changeDirection(change);
        return "The visible trend is broadly " + direction + ", moving from "
                + formatCurrency(first.getValue()) + " on " + first.getKey()
                + " to " + formatCurrency(last.getValue()) + " on " + last.getKey() + ".";
    }

    private String changeDirection(double change) {
        return change > 0 ? "upward" : change < 0 ? "downward" : "flat";
    }

    private String summarizeExtremes(Map<String, Double> values, boolean top, Metric metric) {
        Map.Entry<String, Double> first = top ? topEntry(values) : bottomEntry(values);
        if (first == null) {
            return "No values are available for that comparison.";
        }
        return first.getKey() + " is the " + (top ? "leading" : "weakest") + " value at "
                + formatMetricValue(first.getValue(), metric) + ".";
    }

    private String contributionInterpretation(Map<String, Double> values, double totalSales) {
        Map.Entry<String, Double> top = topEntry(values);
        if (top == null || totalSales == 0) {
            return "No contribution split is available.";
        }
        return top.getKey() + " contributes " + formatContribution(top.getValue(), totalSales) + " of total sales.";
    }

    private int determineResultLimit(String lowercase, Dimension dimension, List<RetailRecord> scopedRecords) {
        if (containsAny(lowercase, "all", "every", "everything", "full", "complete", "detailed")) {
            return dimension == null ? Math.min(scopedRecords.size(), 25) : 20;
        }
        if (containsAny(lowercase, "top", "bottom", "list", "show", "breakdown", "compare")) {
            return Math.max(extractTopN(lowercase, 8), 8);
        }
        return dimension == null ? 8 : 10;
    }

    private String dimensionFilterKey(Dimension dimension) {
        return switch (dimension) {
            case DATE -> "date";
            case CITY -> "city";
            case CATEGORY -> "category";
            case PRODUCT -> "product";
            case CUSTOMER -> "customer";
            case PAYMENT -> "paymentMethod";
            case STATUS -> "status";
        };
    }

    private String filterKeyForMatchType(MatchType type) {
        return switch (type) {
            case CITY -> "city";
            case CATEGORY -> "category";
            case PRODUCT -> "product";
            case CUSTOMER -> "customer";
            case PAYMENT -> "paymentMethod";
            case STATUS -> "status";
        };
    }

    private String labelForMatchType(MatchType type) {
        return switch (type) {
            case CITY -> "City";
            case CATEGORY -> "Category";
            case PRODUCT -> "Product";
            case CUSTOMER -> "Customer";
            case PAYMENT -> "Payment Method";
            case STATUS -> "Status";
        };
    }

    private List<Map<String, Object>> toSeries(Map<String, Double> values, boolean preserveOrder, int limit, String filterKey) {
        List<Map.Entry<String, Double>> ordered = preserveOrder
                ? values.entrySet().stream().sorted(Map.Entry.comparingByKey()).limit(limit).toList()
                : values.entrySet().stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry.comparingByKey()))
                        .limit(limit)
                        .toList();
        return toSeries(ordered, filterKey);
    }

    private List<Map<String, Object>> toSeries(Map<String, Double> values, String filterKey) {
        return toSeries(values.entrySet().stream().toList(), filterKey);
    }

    private List<Map<String, Object>> toSeries(List<Map.Entry<String, Double>> orderedEntries, String filterKey) {
        return orderedEntries.stream()
                .map(entry -> {
                    Map<String, Object> point = new LinkedHashMap<>();
                    point.put("label", entry.getKey());
                    point.put("value", round(entry.getValue()));
                    point.put("filterKey", filterKey);
                    point.put("filterValue", entry.getKey());
                    return point;
                })
                .toList();
    }

    private Map<String, Object> buildChartPayload(
            String title,
            String type,
            Map<String, Double> values,
            String filterKey,
            Metric metric,
            int limit,
            boolean preserveOrder) {
        if (values.isEmpty()) {
            return null;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", title);
        payload.put("type", type);
        payload.put("metricLabel", metric.label());
        payload.put("format", chartFormat(metric));
        payload.put("points", toSeries(values, preserveOrder, limit, filterKey));
        return payload;
    }

    private String chartFormat(Metric metric) {
        return switch (metric) {
            case SALES, AOV -> "currency";
            case AVG_DISCOUNT -> "percent";
            case ORDERS, QUANTITY, CUSTOMERS, PRODUCTS -> "count";
        };
    }

    private Map<String, Object> toMetricTable(String keyColumn, String valueColumn, Map<String, Double> values, int limit, boolean preserveOrder) {
        List<Map.Entry<String, Double>> ordered = preserveOrder
                ? values.entrySet().stream().sorted(Map.Entry.comparingByKey()).limit(limit).toList()
                : values.entrySet().stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry.comparingByKey()))
                        .limit(limit)
                        .toList();
        List<List<String>> rows = ordered.stream()
                .map(entry -> List.of(entry.getKey(), valueColumn.equals("Average Discount") ? round(entry.getValue()) + "%" : formatMetricValue(entry.getValue(), inferMetricFromLabel(valueColumn))))
                .toList();
        return Map.of("columns", List.of(keyColumn, valueColumn), "rows", rows);
    }

    private Map<String, Object> toContributionTable(String keyColumn, Map<String, Double> values, double total, int limit) {
        List<List<String>> rows = topEntries(values, limit).entrySet().stream()
                .map(entry -> List.of(entry.getKey(), formatCurrency(entry.getValue()), formatContribution(entry.getValue(), total)))
                .toList();
        return Map.of("columns", List.of(keyColumn, "Sales", "Contribution"), "rows", rows);
    }

    private Metric inferMetricFromLabel(String label) {
        return switch (label) {
            case "Orders" -> Metric.ORDERS;
            case "Quantity" -> Metric.QUANTITY;
            case "Average Discount" -> Metric.AVG_DISCOUNT;
            case "Average Order Value" -> Metric.AOV;
            case "Customers" -> Metric.CUSTOMERS;
            case "Products" -> Metric.PRODUCTS;
            default -> Metric.SALES;
        };
    }

    private Map<String, Double> topEntries(Map<String, Double> values, int limit) {
        return values.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<String, Double> bottomEntries(Map<String, Double> values, int limit) {
        return values.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left, LinkedHashMap::new));
    }

    private Map.Entry<String, Double> topEntry(Map<String, Double> values) {
        return values.entrySet().stream()
                .max(Map.Entry.<String, Double>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .orElse(null);
    }

    private Map.Entry<String, Double> bottomEntry(Map<String, Double> values) {
        return values.entrySet().stream()
                .min(Map.Entry.<String, Double>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .orElse(null);
    }

    private Map.Entry<Integer, Double> topOrderEntry(Map<Integer, Double> values) {
        return values.entrySet().stream()
                .max(Map.Entry.<Integer, Double>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .orElse(null);
    }

    private Map.Entry<Integer, Double> bottomOrderEntry(Map<Integer, Double> values) {
        return values.entrySet().stream()
                .min(Map.Entry.<Integer, Double>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .orElse(null);
    }

    private Map<String, Object> orderExtremesTable(Map<Integer, Double> orderValues) {
        List<Map.Entry<Integer, Double>> sorted = orderValues.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .toList();
        List<List<String>> rows = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : sorted) {
            rows.add(List.of(String.valueOf(entry.getKey()), formatCurrency(entry.getValue())));
        }
        List<Map.Entry<Integer, Double>> bottom = orderValues.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue())
                .limit(5)
                .toList();
        for (Map.Entry<Integer, Double> entry : bottom) {
            rows.add(List.of(String.valueOf(entry.getKey()), formatCurrency(entry.getValue())));
        }
        return Map.of("columns", List.of("Order ID", "Sales"), "rows", rows);
    }

    private CsvTable readTable(String name) {
        return switch (name) {
            case "raw-input" -> parseCsv("Raw Input Retail CSV", readResourceText("retail.csv"));
            case "fact-sales" -> parseCsv("FactSales.csv", readFileText(outputDirectory.resolve("FactSales.csv")));
            case "dim-date" -> parseCsv("DimDate.csv", readFileText(outputDirectory.resolve("DimDate.csv")));
            case "dim-customer" -> parseCsv("DimCustomer.csv", readFileText(outputDirectory.resolve("DimCustomer.csv")));
            case "dim-product" -> parseCsv("DimProduct.csv", readFileText(outputDirectory.resolve("DimProduct.csv")));
            case "dim-city" -> parseCsv("DimCity.csv", readFileText(outputDirectory.resolve("DimCity.csv")));
            case "dim-payment" -> parseCsv("DimPayment.csv", readFileText(outputDirectory.resolve("DimPayment.csv")));
            case "dim-status" -> parseCsv("DimStatus.csv", readFileText(outputDirectory.resolve("DimStatus.csv")));
            case "retail-results" -> parseCsv("RetailResults.csv", readFileText(outputDirectory.resolve("RetailResults.csv")));
            default -> throw new IllegalArgumentException("Unknown table: " + name);
        };
    }

    private CsvTable parseCsv(String label, String content) {
        String[] lines = content.split("\\R");
        if (lines.length == 0) {
            return new CsvTable(label, List.of(), List.of());
        }

        List<String> columns = splitCsvLine(lines[0]);
        List<List<String>> rows = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            if (!lines[i].isBlank()) {
                rows.add(splitCsvLine(lines[i]));
            }
        }
        return new CsvTable(label, columns, rows);
    }

    private List<String> splitCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '"') {
                inQuotes = !inQuotes;
            } else if (character == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        values.add(current.toString());
        return values;
    }

    private String readFileText(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private byte[] readResourceBytes(String resourceName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String readResourceText(String resourceName) {
        return new String(readResourceBytes(resourceName), StandardCharsets.UTF_8);
    }

    private DownloadFile fileDownload(String fileName) {
        Path filePath = outputDirectory.resolve(fileName);
        try {
            return new DownloadFile(fileName, Files.readAllBytes(filePath));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> sortedDistinct(List<RetailRecord> source, FieldExtractor extractor) {
        return source.stream().map(extractor::extract).distinct().sorted().toList();
    }

    private int countDistinct(List<RetailRecord> source, FieldExtractor extractor) {
        return (int) source.stream().map(extractor::extract).distinct().count();
    }

    private int parsePositiveInt(String value, int defaultValue) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean containsAny(String query, String... candidates) {
        for (String candidate : candidates) {
            if (query.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private int extractTopN(String query, int defaultValue) {
        String[] tokens = query.split("\\s+");
        for (String token : tokens) {
            try {
                int value = Integer.parseInt(token);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private boolean containsNumber(String query) {
        for (int i = 0; i < query.length(); i++) {
            if (Character.isDigit(query.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private String safeKey(Map.Entry<String, Double> entry) {
        return entry == null ? "the current mix" : entry.getKey();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String formatCurrency(double value) {
        return String.format(Locale.US, "Rs %,.2f", value);
    }

    private String formatPercentage(double value) {
        return String.format(Locale.US, "%.1f%%", value * 100);
    }

    private String formatContribution(double part, double total) {
        if (total == 0) {
            return "0.0%";
        }
        return formatPercentage(part / total);
    }

    private String formatMetricValue(double value, Metric metric) {
        return switch (metric) {
            case SALES, AOV -> formatCurrency(value);
            case AVG_DISCOUNT -> round(value) + "%";
            case ORDERS, QUANTITY, CUSTOMERS, PRODUCTS -> String.valueOf((int) Math.round(value));
        };
    }

    private interface FieldExtractor {
        String extract(RetailRecord record);
    }

    private enum Metric {
        SALES("Sales"),
        ORDERS("Orders"),
        QUANTITY("Quantity"),
        AVG_DISCOUNT("Average Discount"),
        AOV("Average Order Value"),
        CUSTOMERS("Customers"),
        PRODUCTS("Products");

        private final String label;

        Metric(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    private enum Dimension {
        DATE("Date"),
        CITY("City"),
        CATEGORY("Category"),
        PRODUCT("Product"),
        CUSTOMER("Customer"),
        PAYMENT("Payment Method"),
        STATUS("Status");

        private final String label;

        Dimension(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    private enum MatchType {
        CITY,
        CATEGORY,
        PRODUCT,
        CUSTOMER,
        PAYMENT,
        STATUS
    }

    private record CsvTable(String label, List<String> columns, List<List<String>> rows) {
    }

    private record MetricSnapshot(
            double totalSales,
            int totalOrders,
            int totalQuantity,
            double averageOrderValue,
            double averageDiscount,
            int uniqueCustomers,
            int uniqueProducts) {
        int activeCustomers() {
            return uniqueCustomers;
        }
    }

    private record QueryContext(Dimension dimension, Metric metric, List<RetailRecord> records, String scopeDescription) {
    }

    private record EntityMatch(MatchType type, String label, String value) {
        boolean matches(RetailRecord record) {
            return switch (type) {
                case CITY -> record.getCity().equalsIgnoreCase(value);
                case CATEGORY -> record.getCategory().equalsIgnoreCase(value);
                case PRODUCT -> record.getProduct().equalsIgnoreCase(value);
                case CUSTOMER -> record.getCustomer().equalsIgnoreCase(value);
                case PAYMENT -> record.getPaymentMethod().equalsIgnoreCase(value);
                case STATUS -> record.getOrderStatus().equalsIgnoreCase(value);
            };
        }
    }

    public record DownloadFile(String fileName, byte[] content) {
    }
}
