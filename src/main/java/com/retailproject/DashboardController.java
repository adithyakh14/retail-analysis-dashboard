package com.retailproject;

import com.retailproject.dto.AskRequest;
import com.retailproject.dto.DashboardFilterRequest;
import com.retailproject.dto.TableRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Tag(name = "Retail Dashboard", description = "Backend APIs for the retail analytics dashboard")
public class DashboardController {
    private final DashboardDataService dataService;

    public DashboardController(DashboardDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/api/dashboard")
    @Operation(summary = "Get dashboard data", description = "Returns summary cards, charts, trend data, insights, and contribution tables.")
    public Map<String, Object> getDashboard(@ModelAttribute DashboardFilterRequest request) {
        return dataService.getDashboardData(request.toFilterMap());
    }

    @GetMapping("/api/filter-options")
    @Operation(summary = "Get filter options", description = "Returns all available values for frontend filters.")
    public Map<String, Object> getFilterOptions() {
        return dataService.getFilterOptions();
    }

    @GetMapping("/api/table")
    @Operation(summary = "Get table data", description = "Returns paginated tabular data for a named source table.")
    public Map<String, Object> getTable(@ModelAttribute TableRequest request) {
        return dataService.getTable(request.resolvedName(), request.toQueryMap());
    }

    @GetMapping("/api/ask")
    @Operation(summary = "Ask a business question", description = "Returns an answer, interpretation, and optional chart or table based on the current filtered dataset.")
    public Map<String, Object> askQuestion(@ModelAttribute AskRequest request) {
        return dataService.askQuestion(request.resolvedQuestion(), request.toQueryMap());
    }

    @GetMapping("/download/{name}")
    @Operation(summary = "Download CSV", description = "Downloads a CSV file for the selected raw or transformed table.")
    public ResponseEntity<ByteArrayResource> download(@Parameter(description = "Dataset name to download") @PathVariable String name) {
        DashboardDataService.DownloadFile file = dataService.getDownloadFile(name);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .body(new ByteArrayResource(file.content()));
    }
}
