package com.retailproject;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void filterOptionsReturnsAvailableFilters() throws Exception {
        mockMvc.perform(get("/api/filter-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dates").isArray())
                .andExpect(jsonPath("$.cities").isArray())
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.paymentMethods").isArray())
                .andExpect(jsonPath("$.statuses").isArray())
                .andExpect(jsonPath("$.customers").isArray());
    }

    @Test
    void dashboardReturnsSummaryAndCharts() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalSales").isNumber())
                .andExpect(jsonPath("$.summary.totalOrders").isNumber())
                .andExpect(jsonPath("$.meta.overallRecordCount").value(greaterThan(0)))
                .andExpect(jsonPath("$.charts.salesTrend").isArray())
                .andExpect(jsonPath("$.insights.topInsights").isArray())
                .andExpect(jsonPath("$.tables.categoryContribution.columns").isArray());
    }

    @Test
    void dashboardAcceptsFilters() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .param("city", "Mumbai")
                        .param("category", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").exists())
                .andExpect(jsonPath("$.meta.filteredRecordCount").isNumber());
    }

    @Test
    void tableReturnsPagedRows() throws Exception {
        mockMvc.perform(get("/api/table")
                        .param("name", "raw-input")
                        .param("page", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("raw-input"))
                .andExpect(jsonPath("$.columns").isArray())
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows", hasSize(5)))
                .andExpect(jsonPath("$.downloadUrl").value("/download/raw-input"));
    }

    @Test
    void askReturnsAnswerPayload() throws Exception {
        mockMvc.perform(get("/api/ask")
                        .param("q", "Which city contributes the highest sales?"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("Which city contributes the highest sales?"))
                .andExpect(jsonPath("$.answer").isString())
                .andExpect(jsonPath("$.interpretation").isString())
                .andExpect(jsonPath("$.suggestions").isArray());
    }

    @Test
    void downloadReturnsCsvAttachment() throws Exception {
        mockMvc.perform(get("/download/raw-input"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"retail.csv\""))
                .andExpect(content().contentType("text/csv;charset=UTF-8"));
    }
}
