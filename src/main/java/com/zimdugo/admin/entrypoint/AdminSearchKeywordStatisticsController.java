package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.application.AdminSearchKeywordStatisticsService;
import com.zimdugo.admin.application.dto.AdminSearchKeywordStatisticsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/statistics/search-keywords")
@RequiredArgsConstructor
public class AdminSearchKeywordStatisticsController {

    private final AdminSearchKeywordStatisticsService adminSearchKeywordStatisticsService;

    @GetMapping
    public String list(Model model) {
        AdminSearchKeywordStatisticsResult result = adminSearchKeywordStatisticsService.getStatistics();
        model.addAttribute("statistics", result);
        model.addAttribute("activeMenu", "search-keywords");
        return "admin/search-keyword-statistics";
    }
}
