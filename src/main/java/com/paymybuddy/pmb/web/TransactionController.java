package com.paymybuddy.pmb.web;

import com.paymybuddy.pmb.repository.TransactionRepository;
import com.paymybuddy.pmb.service.TransactionService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class TransactionController {

    private final TransactionService transactions;
    private final TransactionRepository txs;

    public TransactionController(TransactionService transactions,
                                 TransactionRepository txs) {
        this.transactions = transactions;
        this.txs = txs;
    }

    public static class SendMoneyForm {

        @Email
        @NotBlank
        private String senderEmail;

        @Email
        @NotBlank
        private String receiverEmail;

        private String description;

        private BigDecimal amount;

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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    @GetMapping("/transactions")
    public String list(Model model,
                       @ModelAttribute("form") SendMoneyForm form,
                       @RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "success", required = false) String success) {

        // Liste de toutes les transactions
        model.addAttribute("transactions", txs.findAll());
        model.addAttribute("error", error);
        model.addAttribute("success", success);
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
            return "redirect:/transactions?success=" +
                    URLEncoder.encode("Paiement effectué", StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | IllegalStateException e) {
            String msg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/transactions?error=" + msg;
        }
    }
}