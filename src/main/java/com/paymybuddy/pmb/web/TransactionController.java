package com.paymybuddy.pmb.web;

import com.paymybuddy.pmb.domain.Transaction;
import com.paymybuddy.pmb.repository.TransactionRepository;
import com.paymybuddy.pmb.service.TransactionService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class TransactionController {

    private final TransactionService transactions;
    private final TransactionRepository txs;

    public TransactionController(TransactionService transactions,
                                 TransactionRepository txs) {
        this.transactions = transactions;
        this.txs = txs;
    }

    @GetMapping("/transactions")
    public String showTransactions(Model model,
                                   org.springframework.security.core.Authentication authentication,
                                   @RequestParam(value = "success", required = false) String success,
                                   @RequestParam(value = "error", required = false) String error) {

        // Email de l'utilisateur connecté (vient de Spring Security)
        String currentEmail = authentication.getName();

        // Formulaire d’envoi
        model.addAttribute("form", new SendMoneyForm());

        // Transactions de cet utilisateur (sender ou receiver = currentEmail)
        model.addAttribute("transactions", transactions.listForUser(currentEmail));

        // Messages de feedback (optionnels)
        model.addAttribute("success", success);
        model.addAttribute("error", error);

        return "transactions";
    }

    @PostMapping("/transactions")
    public String send(@ModelAttribute("form") SendMoneyForm form) {
        try {
            transactions.transfer(
                    form.getSenderEmail(),
                    form.getReceiverEmail(),
                    form.getAmount(),
                    form.getDescription()
            );

            String success = URLEncoder.encode(
                    "Paiement effectué",
                    StandardCharsets.UTF_8
            );
            return "redirect:/transactions?success=" + success;

        } catch (IllegalArgumentException | IllegalStateException e) {
            String encoded = URLEncoder.encode(
                    e.getMessage(),
                    StandardCharsets.UTF_8
            );
            return "redirect:/transactions?error=" + encoded;
        }
    }

    public static class SendMoneyForm {

        @NotBlank
        @Email
        private String senderEmail;

        @NotBlank
        @Email
        private String receiverEmail;

        @NotNull
        @Positive
        private BigDecimal amount;

        @NotBlank
        private String description;

        public String getSenderEmail() {
            return senderEmail;
        }

        public void setSenderEmail(String senderEmail) {
            this.senderEmail = senderEmail;
        }

        public String getReceiverEmail() {
            return receiverEmail;
        }

        public void setReceiverEmail(String receiverEmail) {
            this.receiverEmail = receiverEmail;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}