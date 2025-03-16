package org.mgruszka.currency.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.mgruszka.currency.model.AccountCreationDTO;
import org.mgruszka.currency.model.AccountDetailsDTO;
import org.mgruszka.currency.model.TargetCurrency;
import org.mgruszka.currency.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @Operation(summary = "Create a new account")
    @ApiResponse(description = "Unique account identifier used for other API methods")
    @PostMapping
    public ResponseEntity<String> createAccount(@RequestBody AccountCreationDTO account) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(account));
    }

    @Operation(summary = "Exchange account balance between USD and PLN")
    @PostMapping("/{accountId}/exchange")
    public ResponseEntity<AccountDetailsDTO> exchangeCurrency(@PathVariable("accountId") String accountId, @RequestParam("amount") double amount, @RequestParam("targetCurrency") TargetCurrency targetCurrency) {
        return ResponseEntity.ok(accountService.exchangeCurrency(accountId, amount, targetCurrency));
    }

    @Operation(summary = "Retrieves account details and balance")
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDetailsDTO> getAccount(@PathVariable("accountId") String accountId) {
        return ResponseEntity.ok(accountService.getAccount(accountId));
    }
}