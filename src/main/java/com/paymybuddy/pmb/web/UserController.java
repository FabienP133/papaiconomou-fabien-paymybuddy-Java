package com.paymybuddy.pmb.web;

import com.paymybuddy.pmb.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/users")
    public String list(Model model) {
        model.addAttribute("users", users.findAll());
        return "users";
    }
}