package com.paymybuddy.pmb.web;

import com.paymybuddy.pmb.service.ConnectionService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ConnectionController {

    private final ConnectionService connections;

    public ConnectionController(ConnectionService connections) {
        this.connections = connections;
    }

    public static class AddFriendForm {
        @Email @NotBlank
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
}