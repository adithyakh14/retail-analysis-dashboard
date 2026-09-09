const API_BASE_URL = (() => {
    if (window.RETAIL_API_BASE_URL) {
        return window.RETAIL_API_BASE_URL;
    }

    if (window.location.protocol === "file:") {
        return "http://localhost:8080";
    }

    return window.location.origin;
})();

const tableDefinitions = [
    { name: "raw-input", label: "Raw Input" },
    { name: "fact-sales", label: "Fact Sales" },
    { name: "dim-date", label: "Date" },
    { name: "dim-customer", label: "Customer" },
    { name: "dim-product", label: "Product" },
    { name: "dim-city", label: "City" },
    { name: "dim-payment", label: "Payment" },
    { name: "dim-status", label: "Status" },
    { name: "retail-results", label: "Results" }
];

const state = {
    filters: {
        dateFrom: "",
        dateTo: "",
        city: "",
        category: "",
        product: "",
        paymentMethod: "",
        status: "",
        customer: ""
    },
    trendFocusBackup: null,
    currentTable: "raw-input",
    currentPage: 1,
    pageSize: 10,
    search: ""
};

function apiUrl(path, params = {}) {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value !== "") {
            query.set(key, value);
        }
    });
    const suffix = query.toString();
    return `${API_BASE_URL}${path}${suffix ? `?${suffix}` : ""}`;
}

async function fetchJson(url) {
    const response = await fetch(url, {
        credentials: "same-origin"
    });
    if (!response.ok) {
        const contentType = response.headers.get("content-type") || "";
        if (contentType.includes("application/json")) {
            const payload = await response.json();
            throw new Error(payload.message || `Request failed: ${response.status}`);
        }
        throw new Error(`Request failed: ${response.status}`);
    }
    return response.json();
}

function formatCurrency(value) {
    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 2
    }).format(value);
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function createOptions(selectId, values) {
    const select = document.getElementById(selectId);
    select.innerHTML = "";
    const allOption = document.createElement("option");
    allOption.value = "";
    allOption.textContent = "All";
    select.appendChild(allOption);

    values.forEach((value) => {
        const option = document.createElement("option");
        option.value = value;
        option.textContent = value;
        select.appendChild(option);
    });

    select.value = state.filters[selectId];
}

function renderSummary(summary) {
    const cards = [
        ["Total Revenue", formatCurrency(summary.totalSales), "Net sales after discount"],
        ["Total Orders", summary.totalOrders, "Distinct completed order count"],
        ["Total Units Sold", summary.totalQuantity, "Volume sold across the filtered selection"],
        ["Average Order Value", formatCurrency(summary.averageOrderValue), "Revenue per order"],
        ["Average Discount", `${summary.averageDiscount}%`, "Average discount level"],
        ["Active Customers", summary.activeCustomers, "Customers represented in the current slice"],
        ["Total Products", summary.totalProducts, "Products represented in the current slice"]
    ];

    document.getElementById("summaryCards").innerHTML = cards.map(([title, value, detail]) => `
        <article class="summary-card">
            <span>${escapeHtml(title)}</span>
            <strong>${escapeHtml(value)}</strong>
            <small>${escapeHtml(detail)}</small>
        </article>
    `).join("");
}

function renderInsightList(targetId, items) {
    const target = document.getElementById(targetId);
    if (!items || !items.length) {
        target.innerHTML = "<article class=\"insight-card\"><strong>No standout signals</strong><small>The current filter selection does not surface a strong insight here.</small></article>";
        return;
    }

    target.innerHTML = items.map((item) => `
        <article class="insight-card">
            <span>${escapeHtml(item.title)}</span>
            <strong>${escapeHtml(item.value)}</strong>
            <small>${escapeHtml(item.detail)}</small>
        </article>
    `).join("");
}

function applyChartFilter(filterKey, filterValue) {
    if (!filterKey || !Object.hasOwn(state.filters, filterKey)) {
        return;
    }
    state.filters[filterKey] = state.filters[filterKey] === filterValue ? "" : filterValue;
    document.getElementById(filterKey).value = state.filters[filterKey];
    refreshAll();
}

function applyExactDate(dateValue) {
    if (state.trendFocusBackup === null) {
        state.trendFocusBackup = {
            dateFrom: state.filters.dateFrom,
            dateTo: state.filters.dateTo
        };
    }
    state.filters.dateFrom = dateValue;
    state.filters.dateTo = dateValue;
    document.getElementById("dateFrom").value = dateValue;
    document.getElementById("dateTo").value = dateValue;
    refreshAll();
}

function clearTrendFocus() {
    const backup = state.trendFocusBackup || { dateFrom: "", dateTo: "" };
    state.filters.dateFrom = backup.dateFrom;
    state.filters.dateTo = backup.dateTo;
    state.trendFocusBackup = null;
    document.getElementById("dateFrom").value = state.filters.dateFrom;
    document.getElementById("dateTo").value = state.filters.dateTo;
    refreshAll();
}

function renderBarChart(targetId, points, formatter = formatCurrency) {
    const target = document.getElementById(targetId);
    if (!points || !points.length) {
        target.innerHTML = "<p class=\"empty-state\">No data available for this view.</p>";
        return;
    }

    const maxValue = Math.max(...points.map((point) => point.value), 1);
    target.innerHTML = points.map((point) => `
        <button class="bar-row interactive-row" data-filter-key="${escapeHtml(point.filterKey || "")}" data-filter-value="${escapeHtml(point.filterValue || "")}">
            <div class="bar-label">${escapeHtml(point.label)}</div>
            <div class="bar-track">
                <div class="bar-fill" style="width: ${(point.value / maxValue) * 100}%"></div>
            </div>
            <div class="bar-value">${escapeHtml(formatter(point.value))}</div>
        </button>
    `).join("");

    target.querySelectorAll(".interactive-row").forEach((button) => {
        button.addEventListener("click", () => applyChartFilter(button.dataset.filterKey, button.dataset.filterValue));
    });
}

function renderLineChart(points) {
    const target = document.getElementById("trendChart");
    if (!points || !points.length) {
        target.innerHTML = "<p class=\"empty-state\">No trend data available for this selection.</p>";
        return;
    }

    const width = 920;
    const height = 250;
    const padding = 28;
    const maxValue = Math.max(...points.map((point) => point.value), 1);
    const stepX = points.length === 1 ? 0 : (width - padding * 2) / (points.length - 1);
    const coordinates = points.map((point, index) => {
        const x = padding + index * stepX;
        const y = height - padding - ((point.value / maxValue) * (height - padding * 2));
        return { x, y, label: point.label, value: point.value };
    });

    target.innerHTML = `
        <svg viewBox="0 0 ${width} ${height}" role="img" aria-label="Revenue trend chart">
            <defs>
                <linearGradient id="trendStroke" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" stop-color="#c95c2b"></stop>
                    <stop offset="100%" stop-color="#1f7a72"></stop>
                </linearGradient>
            </defs>
            <line x1="${padding}" y1="${height - padding}" x2="${width - padding}" y2="${height - padding}" stroke="rgba(31,36,48,0.18)" />
            <polyline fill="none" stroke="url(#trendStroke)" stroke-width="4" points="${coordinates.map((point) => `${point.x},${point.y}`).join(" ")}"></polyline>
            ${coordinates.map((point) => `
                <g class="trend-point" data-date="${escapeHtml(point.label)}">
                    <title>${escapeHtml(point.label)}: ${escapeHtml(formatCurrency(point.value))}</title>
                    <circle cx="${point.x}" cy="${point.y}" r="4.5" fill="#8f3d17"></circle>
                </g>
            `).join("")}
        </svg>
        <div class="axis-labels">
            <span>${escapeHtml(points[0].label)}</span>
            <span>Click any point to focus on that date</span>
            <span>${escapeHtml(points[points.length - 1].label)}</span>
        </div>
    `;

    target.querySelectorAll(".trend-point").forEach((point) => {
        point.addEventListener("click", () => applyExactDate(point.dataset.date));
    });
}

function renderTrendNarrative(trend) {
    document.getElementById("trendNarrative").textContent = trend.summary || "No trend summary is available.";
    const highlights = trend.highlights || [];
    document.getElementById("trendHighlights").innerHTML = highlights.map((item) => `
        <article class="trend-highlight">
            <span>${escapeHtml(item.title)}</span>
            <strong>${escapeHtml(item.value)}</strong>
            <small>${escapeHtml(item.detail)}</small>
        </article>
    `).join("");
    const focusButton = document.getElementById("clearTrendFocus");
    focusButton.hidden = !trend.focusedDate;
    focusButton.textContent = trend.focusedDate
        ? `Back From ${trend.focusedDate}`
        : "Back To Previous View";
}

function renderTableElement(tableId, tableData) {
    const table = document.getElementById(tableId);
    if (!tableData || !tableData.columns || !tableData.rows) {
        table.innerHTML = "";
        return;
    }

    table.innerHTML = `
        <thead>
            <tr>${tableData.columns.map((column) => `<th>${escapeHtml(column)}</th>`).join("")}</tr>
        </thead>
        <tbody>
            ${tableData.rows.map((row) => `<tr>${row.map((value) => `<td>${escapeHtml(value)}</td>`).join("")}</tr>`).join("")}
        </tbody>
    `;
}

function renderTableLegacy(tableData) {
    renderTableElement("dataTable", tableData);
    document.getElementById("pageLabel").textContent = `Page ${tableData.page} of ${tableData.totalPages} · ${tableData.totalRows} rows`;
    document.getElementById("downloadTable").href = `${API_BASE_URL}${tableData.downloadUrl}`;
}

function renderAiChart(chart) {
    const wrap = document.getElementById("aiChartWrap");
    const chartTarget = document.getElementById("aiChart");
    if (!chart || !chart.points || !chart.points.length) {
        wrap.style.display = "none";
        chartTarget.innerHTML = "";
        return;
    }

    wrap.style.display = "grid";
    if (chart.type === "line") {
        renderLineChartInto(chartTarget, chart.points);
    } else {
        const formatter = chart.format === "percent"
            ? (value) => `${value}%`
            : chart.format === "count"
                ? (value) => `${Math.round(value)}`
                : formatCurrency;
        renderBarChart("aiChart", chart.points, formatter);
    }
}

function renderTable(tableData) {
    renderTableElement("dataTable", tableData);
    document.getElementById("pageLabel").textContent = `Page ${tableData.page} of ${tableData.totalPages} | ${tableData.totalRows} rows`;
    document.getElementById("downloadTable").href = `${API_BASE_URL}${tableData.downloadUrl}`;
}

function renderLineChartInto(target, points) {
    const width = 920;
    const height = 240;
    const padding = 28;
    const maxValue = Math.max(...points.map((point) => point.value), 1);
    const stepX = points.length === 1 ? 0 : (width - padding * 2) / (points.length - 1);
    const coordinates = points.map((point, index) => {
        const x = padding + index * stepX;
        const y = height - padding - ((point.value / maxValue) * (height - padding * 2));
        return { x, y, label: point.label, value: point.value };
    });

    target.innerHTML = `
        <svg viewBox="0 0 ${width} ${height}" role="img" aria-label="AI generated line chart">
            <defs>
                <linearGradient id="aiTrendStroke" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" stop-color="#1f4f7a"></stop>
                    <stop offset="100%" stop-color="#1f7a72"></stop>
                </linearGradient>
            </defs>
            <line x1="${padding}" y1="${height - padding}" x2="${width - padding}" y2="${height - padding}" stroke="rgba(31,36,48,0.18)" />
            <polyline fill="none" stroke="url(#aiTrendStroke)" stroke-width="4" points="${coordinates.map((point) => `${point.x},${point.y}`).join(" ")}"></polyline>
            ${coordinates.map((point) => `
                <g>
                    <title>${escapeHtml(point.label)}: ${escapeHtml(formatCurrency(point.value))}</title>
                    <circle cx="${point.x}" cy="${point.y}" r="4.5" fill="#1f4f7a"></circle>
                </g>
            `).join("")}
        </svg>
        <div class="axis-labels">
            <span>${escapeHtml(points[0].label)}</span>
            <span>${escapeHtml(points[points.length - 1].label)}</span>
        </div>
    `;
}

function buildTableTabs() {
    const container = document.getElementById("tableTabs");
    container.innerHTML = tableDefinitions.map((table) => `
        <button class="table-tab ${table.name === state.currentTable ? "active" : ""}" data-table="${table.name}">
            ${escapeHtml(table.label)}
        </button>
    `).join("");

    container.querySelectorAll(".table-tab").forEach((button) => {
        button.addEventListener("click", () => {
            state.currentTable = button.dataset.table;
            state.currentPage = 1;
            buildTableTabs();
            loadTable();
        });
    });
}

function renderSuggestions(suggestions) {
    document.getElementById("suggestions").innerHTML = suggestions.map((suggestion) => `
        <button class="suggestion-button" data-suggestion="${escapeHtml(suggestion)}">${escapeHtml(suggestion)}</button>
    `).join("");

    document.querySelectorAll(".suggestion-button").forEach((button) => {
        button.addEventListener("click", () => {
            document.getElementById("aiQuestion").value = button.dataset.suggestion;
            askQuestion();
        });
    });
}

async function loadFilters() {
    const filterOptions = await fetchJson(apiUrl("/api/filter-options"));
    createOptions("dateFrom", filterOptions.dates);
    createOptions("dateTo", filterOptions.dates);
    createOptions("city", filterOptions.cities);
    createOptions("category", filterOptions.categories);
    createOptions("product", filterOptions.products);
    createOptions("paymentMethod", filterOptions.paymentMethods);
    createOptions("status", filterOptions.statuses);
    createOptions("customer", filterOptions.customers);
}

async function loadDashboard() {
    const dashboard = await fetchJson(apiUrl("/api/dashboard", state.filters));
    renderSummary(dashboard.summary);
    renderInsightList("topInsights", dashboard.insights.topInsights);
    renderInsightList("watchouts", dashboard.insights.watchouts);
    renderTrendNarrative(dashboard.trend);
    renderLineChart(dashboard.charts.salesTrend);
    renderBarChart("categoryChart", dashboard.charts.categoryPerformance);
    renderBarChart("cityChart", dashboard.charts.cityPerformance);
    renderBarChart("topProductChart", dashboard.charts.topProducts);
    renderBarChart("bottomProductChart", dashboard.charts.bottomProducts);
    renderBarChart("customerChart", dashboard.charts.topCustomers);
    renderBarChart("paymentChart", dashboard.charts.paymentPerformance);
    renderBarChart("discountChart", dashboard.charts.discountImpact, (value) => `${value}%`);
    renderTableElement("categoryContributionTable", dashboard.tables.categoryContribution);
    renderTableElement("cityContributionTable", dashboard.tables.cityContribution);

    document.getElementById("refreshedAt").textContent = dashboard.meta.refreshedAt;
    document.getElementById("overallRecordCount").textContent = dashboard.meta.overallRecordCount;
    document.getElementById("filteredRecordCount").textContent = dashboard.meta.filteredRecordCount;
}

async function loadTable() {
    const tableData = await fetchJson(apiUrl("/api/table", {
        name: state.currentTable,
        page: state.currentPage,
        pageSize: state.pageSize,
        search: state.search
    }));
    renderTable(tableData);
}

async function askQuestion() {
    const question = document.getElementById("aiQuestion").value.trim();
    document.getElementById("aiAnswer").textContent = "Reviewing the current data selection...";
    document.getElementById("aiInterpretation").textContent = "Preparing a business interpretation...";

    const response = await fetchJson(apiUrl("/api/ask", { q: question, ...state.filters }));
    document.getElementById("aiAnswer").textContent = response.answer || "";
    document.getElementById("aiInterpretation").textContent = response.interpretation || "No additional interpretation returned.";
    renderSuggestions(response.suggestions || []);
    renderAiChart(response.chart);
    renderTableElement("aiTable", response.table);
}

async function refreshAll() {
    state.currentPage = 1;
    await loadDashboard();
    await loadTable();
    await askQuestion();
}

function wireFilters() {
    const logoutForm = document.getElementById("logoutForm");
    logoutForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const button = logoutForm.querySelector("button");
        const message = document.getElementById("logoutMessage");
        button.disabled = true;
        message.textContent = "";
        try {
            const response = await fetch("/dashboard/csrf", {
                credentials: "same-origin",
                cache: "no-store",
                headers: { Accept: "application/json" }
            });
            if (response.redirected || response.status === 401) {
                window.location.assign("/login");
                return;
            }
            if (!response.ok) throw new Error("Unable to prepare sign out");
            const csrf = await response.json();
            if (!csrf.parameterName || !csrf.token) throw new Error("Missing sign out token");
            const logoutToken = document.getElementById("logoutCsrfToken");
            logoutToken.name = csrf.parameterName;
            logoutToken.value = csrf.token;
            logoutForm.submit();
        } catch (error) {
            message.textContent = "Unable to sign out. Please try again.";
            button.disabled = false;
        }
    });

    Object.keys(state.filters).forEach((key) => {
        const element = document.getElementById(key);
        element.addEventListener("change", async (event) => {
            state.filters[key] = event.target.value;
            await refreshAll();
        });
    });

    document.getElementById("tableSearch").addEventListener("input", async (event) => {
        state.search = event.target.value;
        state.currentPage = 1;
        await loadTable();
    });

    document.getElementById("prevPage").addEventListener("click", async () => {
        if (state.currentPage > 1) {
            state.currentPage -= 1;
            await loadTable();
        }
    });

    document.getElementById("nextPage").addEventListener("click", async () => {
        state.currentPage += 1;
        await loadTable();
    });

    document.getElementById("askButton").addEventListener("click", askQuestion);
    document.getElementById("clearTrendFocus").addEventListener("click", clearTrendFocus);
    document.getElementById("aiQuestion").addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
            askQuestion();
        }
    });
}

async function init() {
    buildTableTabs();
    wireFilters();
    await loadFilters();
    await loadDashboard();
    await loadTable();
    await askQuestion();
}

init().catch((error) => {
    console.error(error);
    document.body.innerHTML = `<div style="padding: 24px; font-family: sans-serif;">Unable to load the dashboard. ${escapeHtml(error.message)}</div>`;
});
