package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.dashboard.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminHomeController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/admin")
    public String home(Model model) {
        model.addAttribute("dashboard", dashboardService.getDashboard());
        return "admin/home";
    }
}
