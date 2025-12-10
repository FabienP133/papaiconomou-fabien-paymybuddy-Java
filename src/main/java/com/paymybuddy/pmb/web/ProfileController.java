package com.paymybuddy.pmb.web;

import com.paymybuddy.pmb.domain.User;
import com.paymybuddy.pmb.repository.UserRepository;
import com.paymybuddy.pmb.security.CustomUserDetails;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserRepository users;

    public ProfileController(UserRepository users) {
        this.users = users;
    }

    public static class ProfileForm {

        @NotBlank
        private String firstName;

        @NotBlank
        private String lastName;

        @Email
        @NotBlank
        private String email; // affiché en lecture seule

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal CustomUserDetails currentUser,
                              @RequestParam(value = "success", required = false) String success,
                              @RequestParam(value = "error", required = false) String error,
                              Model model) {

        if (currentUser == null) {
            return "redirect:/login";
        }

        User user = users.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utilisateur courant introuvable."));

        ProfileForm form = new ProfileForm();
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setEmail(user.getEmail());

        model.addAttribute("form", form);
        model.addAttribute("success", success);
        model.addAttribute("error", error);
        model.addAttribute("userBalance", user.getBalance());

        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails currentUser,
                                @ModelAttribute("form") ProfileForm form,
                                RedirectAttributes redirect) {

        if (currentUser == null) {
            return "redirect:/login";
        }

        String currentEmail = currentUser.getUsername();

        try {
            User user = users.findByEmail(currentEmail)
                    .orElseThrow(() -> new IllegalStateException("Utilisateur courant introuvable."));

            // Je ne change pas l’email ici
            user.setFirstName(form.getFirstName());
            user.setLastName(form.getLastName());

            users.save(user);

            redirect.addFlashAttribute("success", "Profil mis à jour.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile";
    }
}