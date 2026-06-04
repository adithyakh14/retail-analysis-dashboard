package com.retailproject.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard/index.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/dashboard/index.html";
    }

    @GetMapping("/dashboard/")
    public String dashboardTrailingSlash() {
        return "redirect:/dashboard/index.html";
    }
}
