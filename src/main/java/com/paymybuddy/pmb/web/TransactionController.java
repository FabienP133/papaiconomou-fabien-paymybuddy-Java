package com.paymybuddy.pmb.web;

import com.paymybuddy.pmb.repository.TransactionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TransactionController {

    private final TransactionRepository transactions;

    public TransactionController(TransactionRepository transactions) {
        this.transactions = transactions;
    }

    @GetMapping("/transactions")
    public String list(Model model) {
        model.addAttribute("transactions", transactions.findAll());
        return "transactions";
    }
}
