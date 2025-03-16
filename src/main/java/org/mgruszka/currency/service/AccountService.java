package org.mgruszka.currency.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mgruszka.currency.exceptions.AccountNotFoundException;
import org.mgruszka.currency.exceptions.InsufficientFundsException;
import org.mgruszka.currency.exceptions.InvalidCurrencyException;
import org.mgruszka.currency.model.Account;
import org.mgruszka.currency.model.AccountCreationDTO;
import org.mgruszka.currency.model.AccountDetailsDTO;
import org.mgruszka.currency.model.TargetCurrency;
import org.mgruszka.currency.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;
    private final RecentExchangeRateService recentExchangeRateService;

    @Autowired
    public AccountService(AccountRepository accountRepository, ObjectMapper objectMapper, RecentExchangeRateService recentExchangeRateService) {
        this.accountRepository = accountRepository;
        this.objectMapper = objectMapper;
        this.recentExchangeRateService = recentExchangeRateService;
    }

    public String createAccount(AccountCreationDTO request) {
        Account account = new Account();
        account.setId(UUID.randomUUID().toString());
        account.setFirstName(request.getFirstName());
        account.setLastName(request.getLastName());
        account.setBalancePLN(request.getInitialBalance());
        account.setBalanceUSD(0);
        return accountRepository.save(account).getId();
    }

    public AccountDetailsDTO exchangeCurrency(String accountId, double amount, TargetCurrency targetCurrency) {
        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);

        double exchangeRate = recentExchangeRateService.getUSDExchangeRate();
        switch (targetCurrency) {
            case USD -> {
                if (account.getBalancePLN() >= amount) {
                    account.setBalancePLN(account.getBalancePLN() - amount);
                    account.setBalanceUSD(roundDown(account.getBalanceUSD() + (amount / exchangeRate)));
                } else {
                    throw new InsufficientFundsException();
                }
            }
            case PLN -> {
                if (account.getBalanceUSD() >= amount) {
                    account.setBalanceUSD(account.getBalanceUSD() - amount);
                    account.setBalancePLN(roundDown(account.getBalancePLN() + (amount * exchangeRate)));
                } else {
                    throw new InsufficientFundsException();
                }
            }
            default -> throw new InvalidCurrencyException("Unsupported target currency: " + targetCurrency);
        }
        return objectMapper.convertValue(accountRepository.save(account), AccountDetailsDTO.class);
    }

    private double roundDown(double value) {
        return  BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
    }

    public AccountDetailsDTO getAccount(String accountId) {
        return objectMapper.convertValue(accountRepository
                .findById(accountId)
                .orElseThrow(AccountNotFoundException::new), AccountDetailsDTO.class);
    }
}
