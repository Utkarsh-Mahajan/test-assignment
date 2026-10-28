package com.example.ledger.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LedgerApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void maintainsBalanceAndRejectsInsufficientDebit() throws Exception {
        long accountId = createAccount("Primary");
        postTransaction(accountId, "credit", "100.00", "c-1");
        postTransaction(accountId, "debit", "30.00", "d-1");

        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                .content(json(accountId, "debit", "71.00", "d-2")))
                .andExpect(status().isUnprocessableEntity());
        assertBalance(accountId, "70.00");
    }

    @Test
    void rejectsDuplicateReferenceAndDoesNotChangeBalance() throws Exception {
        long accountId = createAccount("Duplicate");
        postTransaction(accountId, "credit", "50.00", "same-ref");

        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                .content(json(accountId, "credit", "25.00", "same-ref")))
                .andExpect(status().isConflict());
        assertBalance(accountId, "50.00");
    }

    @Test
    void transferIsAtomicAndTransactionsAreNewestFirst() throws Exception {
        long sourceId = createAccount("Source");
        long destinationId = createAccount("Destination");
        postTransaction(sourceId, "credit", "100.00", "fund-source");

        mvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"sourceAccountId":%d,"destinationAccountId":%d,"amount":30.00,
                     "referenceId":"transfer-1"}
                    """.formatted(sourceId, destinationId)))
                .andExpect(status().isCreated());
        assertBalance(sourceId, "70.00");
        assertBalance(destinationId, "30.00");

        mvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"sourceAccountId":%d,"destinationAccountId":%d,"amount":71.00,
                     "referenceId":"transfer-2"}
                    """.formatted(sourceId, destinationId)))
                .andExpect(status().isUnprocessableEntity());
        assertBalance(sourceId, "70.00");
        assertBalance(destinationId, "30.00");

        mvc.perform(get("/api/transactions?page=0&limit=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(3))
                .andExpect(jsonPath("$.content[0].referenceId").value("transfer-1"))
                .andExpect(jsonPath("$.content[0].type").value("CREDIT"));
    }

    @Test
    void validatesPaginationAndRequestBody() throws Exception {
        mvc.perform(get("/api/transactions?page=-1&limit=0"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.accountId").exists());
        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":999,\"type\":\"credit\",\"amount\":1,\"referenceId\":\"missing\"}"))
                .andExpect(status().isNotFound());
    }

    private long createAccount(String name) throws Exception {
        String response = mvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").longValue();
    }

    private void postTransaction(long accountId, String type, String amount, String referenceId) throws Exception {
        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                .content(json(accountId, type, amount, referenceId)))
                .andExpect(status().isCreated());
    }

    private void assertBalance(long accountId, String expected) throws Exception {
        String response = mvc.perform(get("/api/accounts/" + accountId))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(new BigDecimal(mapper.readTree(response).get("balance").asText())).isEqualByComparingTo(expected);
    }

    private String json(long accountId, String type, String amount, String referenceId) {
        try {
            return mapper.writeValueAsString(new CreateTransactionRequest(accountId,
                    com.example.ledger.domain.TransactionType.from(type), new BigDecimal(amount),
                    referenceId, null));
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
