package com.paymybuddy.pmb.web;

import com.paymybuddy.pmb.domain.User;
import com.paymybuddy.pmb.repository.UserRepository;
import com.paymybuddy.pmb.security.CustomUserDetails;
import com.paymybuddy.pmb.service.ConnectionService;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ConnectionController {

    private final ConnectionService connections;
    private final UserRepository users;   //  ajouté

    public ConnectionController(ConnectionService connections,
                                UserRepository users) {  //  injecté
        this.connections = connections;
        this.users = users;
    }

    public static class AddFriendForm {
        @Email
        @NotBlank
        private String friendEmail;

        public String getFriendEmail() { return friendEmail; }
        public void setFriendEmail(String friendEmail) { this.friendEmail = friendEmail; }
    }

    @GetMapping("/connections")
    public String list(Model model,
                       @ModelAttribute("form") AddFriendForm form,
                       @RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "success", required = false) String success) {

        model.addAttribute("connections", connections.listConnections());
        model.addAttribute("error", error);
        model.addAttribute("success", success);
        return "connections";
    }

    @PostMapping("/connections")
    public String add(@ModelAttribute("form") AddFriendForm form, Model model) {
        try {
            connections.addConnection(form.getFriendEmail());
            return "redirect:/connections?success=Relation+ajoutée";
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "redirect:/connections?error=" + e.getMessage().replace(" ", "+");
        }
    }

    @PostMapping("/connections/remove")
    @Transactional
    public String removeConnection(@AuthenticationPrincipal CustomUserDetails currentUser,
                                   @RequestParam("email") String friendEmail,
                                   RedirectAttributes redirect) {

        if (currentUser == null) {
            return "redirect:/login";
        }

        String ownerEmail = currentUser.getUsername();
        friendEmail = friendEmail.trim();

        try {
            User owner = users.findByEmail(ownerEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur courant introuvable."));
            User friend = users.findByEmail(friendEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Aucun utilisateur avec cet email."));

            if (!owner.getConnections().contains(friend)) {
                throw new IllegalArgumentException("Cette relation n'existe pas.");
            }

            owner.getConnections().remove(friend);
            friend.getConnections().remove(owner);

            users.save(owner);
            users.save(friend);

            redirect.addFlashAttribute("success", "Relation supprimée.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/connections";
    }

}