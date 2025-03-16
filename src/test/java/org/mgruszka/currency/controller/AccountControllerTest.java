package org.mgruszka.currency.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mgruszka.currency.model.AccountCreationDTO;
import org.mgruszka.currency.model.TargetCurrency;
import org.mgruszka.currency.service.AccountService;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void whenCreateAccountThenIdShouldBeReturned() throws Exception {
        AccountCreationDTO creationDTO = new AccountCreationDTO("John", "Doe", 1000.0);

        String result = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        assertThat(result).isNotBlank();
    }

    @Test
    void whenGetAccountThenShouldReturnAccountDetails() throws Exception {
        AccountCreationDTO creationDTO = new AccountCreationDTO("John", "Doe", 1000.0);
        //create account
        String newAccountId = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        //get details
        mockMvc.perform(get("/api/accounts/{accountId}", newAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newAccountId))
                .andExpect(jsonPath("$.balancePLN").value(1000.0))
                .andExpect(jsonPath("$.balanceUSD").value(0.0))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andDo(print());
    }

    @Test
    void whenGetAccountThatDoesNotExistsThenShouldReturnNotFoundError() throws Exception {
        //get details
        mockMvc.perform(get("/api/accounts/{accountId}", "nonExistingId"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void whenExchangeThenWeShouldSeeDifferentAccountBalance() throws Exception {
        AccountCreationDTO creationDTO = new AccountCreationDTO("John", "Doe", 1000.0);
        //create account
        String newAccountId = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        //exchange pln to usd
        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/{accountId}/exchange", newAccountId)
                        .param("amount", String.valueOf(100))
                        .param("targetCurrency", TargetCurrency.USD.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newAccountId))
                .andExpect(jsonPath("$.balancePLN").value(900.0))
                //since exchange rate might be different everytime we just make simple check if it is now greater than zero which is initial amount
                .andExpect(jsonPath("$.balanceUSD", Matchers.greaterThan(0.0)))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andDo(print());
    }

    @Test
    void whenExchangeAndHaveInsufficientFundsThenWeWillGet400Error() throws Exception {
        AccountCreationDTO creationDTO = new AccountCreationDTO("John", "Doe", 1000.0);
        //create account
        String newAccountId = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        //exchange pln to usd
        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/{accountId}/exchange", newAccountId)
                        .param("amount", String.valueOf(1100))
                        .param("targetCurrency", TargetCurrency.USD.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}
