package com.paymybuddy.pmb.web;

import com.paymybuddy.pmb.repository.TransactionRepository;
import com.paymybuddy.pmb.security.CustomUserDetails;
import com.paymybuddy.pmb.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class TransactionController {

    private final TransactionService transactions;
    private final TransactionRepository txs;

    public TransactionController(TransactionService transactions, TransactionRepository txs) {
        this.transactions = transactions;
        this.txs = txs;
    }

    @GetMapping("/transactions")
    public String showTransactions(Model model,
                                   @AuthenticationPrincipal CustomUserDetails currentUser,
                                   @ModelAttribute("form") SendMoneyForm form,
                                   @ModelAttribute("success") String success,
                                   @ModelAttribute("error") String error) {

        if (form == null) model.addAttribute("form", new SendMoneyForm());


        model.addAttribute("transactions", txs.findAll());

        model.addAttribute("success", success);
        model.addAttribute("error", error);

        return "transactions";
    }

    @PostMapping("/transactions")
    public String send(@AuthenticationPrincipal CustomUserDetails currentUser,
                       @Valid @ModelAttribute("form") SendMoneyForm form,
                       BindingResult binding) {

        if (currentUser == null) {
            return "redirect:/login";
        }

        if (binding.hasErrors()) {
            String msg = URLEncoder.encode("Champs invalides.", StandardCharsets.UTF_8);
            return "redirect:/transactions?error=" + msg;
        }

        String senderEmail = currentUser.getUsername();

        try {
            transactions.transfer(
                    senderEmail,
                    form.getReceiverEmail(),
                    form.getAmount(),
                    form.getDescription()
            );

            String success = URLEncoder.encode("Paiement effectué", StandardCharsets.UTF_8);
            return "redirect:/transactions?success=" + success;

        } catch (IllegalArgumentException | IllegalStateException e) {
            String encoded = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/transactions?error=" + encoded;
        }
    }

    public static class SendMoneyForm {

        @NotBlank
        @Email
        private String receiverEmail;

        @NotNull
        @Positive
        private BigDecimal amount;

        @NotBlank
        private String description;

        public String getReceiverEmail() { return receiverEmail; }
        public void setReceiverEmail(String receiverEmail) { this.receiverEmail = receiverEmail; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}