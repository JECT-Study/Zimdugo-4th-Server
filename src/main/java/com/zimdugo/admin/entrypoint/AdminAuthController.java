package com.zimdugo.admin.entrypoint;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminAuthController {

    @GetMapping("/admin/login")
    public String loginForm() {
        return "admin/login";
    }
}
