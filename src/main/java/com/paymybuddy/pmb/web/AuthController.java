package com.paymybuddy.pmb.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        // renvoie src/main/resources/templates/login.html
        return "login";
    }
}