package com.retailproject;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.retailproject.security.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void resetAttempts() {
        loginAttemptService.reset("admin");
        loginAttemptService.reset("analyst");
    }

    @Test
    void protectedApiRejectsAnonymousUsers() throws Exception {
        mockMvc.perform(get("/api/filter-options"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginPageShowsInvalidCredentialsMessage() throws Exception {
        mockMvc.perform(get("/login").param("error", "invalid"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid username or password.")));
    }

    @Test
    void invalidLoginRedirectsBackToLoginWithError() throws Exception {
        mockMvc.perform(formLogin().user("wrong-user").password("wrong-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=invalid"));
    }

    @Test
    void repeatedInvalidLoginsTriggerTemporaryLockout() throws Exception {
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(formLogin().user("analyst").password("bad-password"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?error=invalid"));
        }

        mockMvc.perform(formLogin().user("analyst").password("bad-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=locked"));
    }

    @Test
    void loginPageShowsLockoutMessage() throws Exception {
        mockMvc.perform(get("/login").param("error", "locked"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("temporarily locked for 5 minutes")));
    }

    @Test
    @WithMockUser(roles = "USER")
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
    void apiLoginReturnsBearerToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"change-me-now"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void filterOptionsSupportsBearerTokenFromApiLogin() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"change-me-now"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String token = com.jayway.jsonpath.JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(get("/api/filter-options")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cities").isArray());
    }

    @Test
    @WithMockUser(roles = "USER")
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
    @WithMockUser(roles = "USER")
    void dashboardAcceptsFilters() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .param("city", "Mumbai")
                        .param("category", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").exists())
                .andExpect(jsonPath("$.meta.filteredRecordCount").isNumber());
    }

    @Test
    @WithMockUser(roles = "USER")
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
    @WithMockUser(roles = "USER")
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
    @WithMockUser(roles = "ADMIN")
    void downloadReturnsCsvAttachment() throws Exception {
        mockMvc.perform(get("/download/raw-input"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"retail.csv\""))
                .andExpect(content().contentType("text/csv;charset=UTF-8"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void downloadIsForbiddenForRegularUsers() throws Exception {
        mockMvc.perform(get("/download/raw-input"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void swaggerDocsAreForbiddenForRegularUsers() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isForbidden());
    }

    @Test
    void dashboardHomeRedirectsIntoProtectedDashboard() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/dashboard/index.html"));
    }
}
