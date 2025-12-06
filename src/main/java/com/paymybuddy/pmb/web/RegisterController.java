package com.paymybuddy.pmb.web;

import com.paymybuddy.pmb.domain.User;
import com.paymybuddy.pmb.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class RegisterController {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public RegisterController(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model,
                                   @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("form", new RegisterForm());
        model.addAttribute("error", error);
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("form") RegisterForm form) {

        // email déjà pris ?
        if (users.findByEmail(form.getEmail()).isPresent()) {
            return "redirect:/register?error=email";
        }

        User u = new User();
        u.setFirstName(form.getUsername()); // simple : on met tout dans firstName
        u.setLastName("");
        u.setEmail(form.getEmail());
        u.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        u.setBalance(new BigDecimal("0.00"));

        users.save(u);

        // après inscription -> vers login
        return "redirect:/login?registered";
    }

    public static class RegisterForm {
        private String username;
        private String email;
        private String password;

        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
    }
}