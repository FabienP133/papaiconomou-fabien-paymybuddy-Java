package com.paymybuddy.pmb.web;

import com.paymybuddy.pmb.repository.TransactionRepository;
import com.paymybuddy.pmb.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    TransactionService transactions;

    @MockBean
    TransactionRepository txs;

    @Test
    @WithMockUser(username = "alice@example.com")
    void shouldDisplayTransactionsPage() throws Exception {
        // On stub la méthode filtrée par utilisateur connecté
        when(txs.findBySenderEmailOrReceiverEmailOrderByCreatedAtDesc(
                "alice@example.com", "alice@example.com"))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void shouldSendMoneyAndRedirectOnSuccess() throws Exception {
        mvc.perform(post("/transactions")
                        .with(csrf())
                        .param("receiverEmail", "bob@example.com")
                        .param("amount", "10.00")
                        .param("description", "Déj"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/transactions?success=*"));

        // senderEmail = username du user connecté
        verify(transactions).transfer(
                "alice@example.com",
                "bob@example.com",
                new BigDecimal("10.00"),
                "Déj"
        );
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void shouldRedirectWithErrorWhenServiceThrows() throws Exception {
        doThrow(new IllegalArgumentException("Solde insuffisant"))
                .when(transactions)
                .transfer(
                        "alice@example.com",
                        "bob@example.com",
                        new BigDecimal("10.00"),
                        "Déj"
                );

        mvc.perform(post("/transactions")
                        .with(csrf())
                        .param("receiverEmail", "bob@example.com")
                        .param("amount", "10.00")
                        .param("description", "Déj"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/transactions?error=*"));
    }
}