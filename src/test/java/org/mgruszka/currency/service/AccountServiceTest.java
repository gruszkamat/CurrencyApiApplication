package org.mgruszka.currency.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mgruszka.currency.exceptions.AccountNotFoundException;
import org.mgruszka.currency.exceptions.InsufficientFundsException;
import org.mgruszka.currency.model.Account;
import org.mgruszka.currency.model.AccountCreationDTO;
import org.mgruszka.currency.model.AccountDetailsDTO;
import org.mgruszka.currency.model.TargetCurrency;
import org.mgruszka.currency.repository.AccountRepository;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private final AccountRepository accountRepository = Mockito.mock(AccountRepository.class);

    private final RecentExchangeRateService recentExchangeRateService = Mockito.mock(RecentExchangeRateService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AccountService accountService = new AccountService(accountRepository, objectMapper, recentExchangeRateService);

    @Test
    void shouldCreateNewAccountWithUniqueIdAndInitialBalanceInPLN() {
        // Arrange
        AccountCreationDTO request = new AccountCreationDTO();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setInitialBalance(1000.0);

        Account account = new Account();
        account.setId(UUID.randomUUID().toString());
        account.setFirstName(request.getFirstName());
        account.setLastName(request.getLastName());
        account.setBalancePLN(request.getInitialBalance());
        account.setBalanceUSD(0);

        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        String accountId = accountService.createAccount(request);

        // Assert
        assertNotNull(accountId);
        assertEquals(account.getId(), accountId);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenExchangingCurrencyForNonExistentAccount() {
        // Arrange
        String nonExistentAccountId = "non-existent-id";
        double amount = 100.0;
        TargetCurrency targetCurrency = TargetCurrency.USD;

        when(accountRepository.findById(nonExistentAccountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> {
            accountService.exchangeCurrency(nonExistentAccountId, amount, targetCurrency);
        });

        verify(accountRepository).findById(nonExistentAccountId);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void shouldThrowInsufficientFundsExceptionWhenExchangingMorePLNThanAvailable() {
        // Arrange
        String accountId = "existing-account-id";
        double amountToExchange = 1500.0; // More than available balance
        TargetCurrency targetCurrency = TargetCurrency.USD;

        Account account = new Account();
        account.setId(accountId);
        account.setBalancePLN(1000.0); // Available balance in PLN
        account.setBalanceUSD(0);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> accountService.exchangeCurrency(accountId, amountToExchange, targetCurrency));

        verify(accountRepository).findById(accountId);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void shouldThrowInsufficientFundsExceptionWhenExchangingMoreUSDThanAvailable() {
        // Arrange
        String accountId = "existing-account-id";
        double amountToExchange = 500.0; // More than available balance in USD
        TargetCurrency targetCurrency = TargetCurrency.PLN;

        Account account = new Account();
        account.setFirstName("John");
        account.setLastName("Doe");
        account.setId(accountId);
        account.setBalancePLN(0);
        account.setBalanceUSD(100.0); // Available balance in USD

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> {
            accountService.exchangeCurrency(accountId, amountToExchange, targetCurrency);
        });

        verify(accountRepository).findById(accountId);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void shouldCorrectlyExchangePLNToUSDUsingCurrentExchangeRateAndUpdateBalances() {
        // Arrange
        String accountId = "existing-account-id";
        double amountToExchange = 500.0;
        TargetCurrency targetCurrency = TargetCurrency.USD;
        double exchangeRate = 4.0; // Example exchange rate

        Account account = new Account();
        account.setFirstName("John");
        account.setLastName("Doe");
        account.setId(accountId);
        account.setBalancePLN(1000.0); // Initial balance in PLN
        account.setBalanceUSD(0); // Initial balance in USD

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(recentExchangeRateService.getUSDExchangeRate()).thenReturn(exchangeRate);
        when(accountRepository.save(any())).thenReturn(account);

        // Act
        AccountDetailsDTO updatedAccount = accountService.exchangeCurrency(accountId, amountToExchange, targetCurrency);

        // Assert
        assertEquals(500.0, updatedAccount.getBalancePLN());
        assertEquals(125.0, updatedAccount.getBalanceUSD()); // 500 PLN / 4.0 = 125 USD
        verify(accountRepository).findById(accountId);
        verify(recentExchangeRateService).getUSDExchangeRate();
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldCorrectlyExchangeUSDToPLNUsingCurrentExchangeRateAndUpdateBalances() {
        // Arrange
        String accountId = "existing-account-id";
        double amountToExchange = 50.0;
        TargetCurrency targetCurrency = TargetCurrency.PLN;
        double exchangeRate = 4.0; // Example exchange rate

        Account account = new Account();
        account.setFirstName("John");
        account.setLastName("Doe");
        account.setId(accountId);
        account.setBalancePLN(0); // Initial balance in PLN
        account.setBalanceUSD(100.0); // Initial balance in USD

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(recentExchangeRateService.getUSDExchangeRate()).thenReturn(exchangeRate);
        when(accountRepository.save(any())).thenReturn(account);

        // Act
        AccountDetailsDTO updatedAccount = accountService.exchangeCurrency(accountId, amountToExchange, targetCurrency);

        // Assert
        assertEquals(200.0, updatedAccount.getBalancePLN()); // 50 USD * 4.0 = 200 PLN
        assertEquals(50.0, updatedAccount.getBalanceUSD());
        verify(accountRepository).findById(accountId);
        verify(recentExchangeRateService).getUSDExchangeRate();
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldReturnAccountDetailsAsAccountDetailsDTOWhenRetrievingExistingAccount() {
        // Arrange
        String accountId = "existing-account-id";
        Account account = new Account();
        account.setId(accountId);
        account.setFirstName("John");
        account.setLastName("Doe");
        account.setBalancePLN(1000.0);
        account.setBalanceUSD(200.0);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        AccountDetailsDTO accountDetails = accountService.getAccount(accountId);

        // Assert
        assertNotNull(accountDetails);
        assertEquals(accountId, accountDetails.getId());
        assertEquals("John", accountDetails.getFirstName());
        assertEquals("Doe", accountDetails.getLastName());
        assertEquals(1000.0, accountDetails.getBalancePLN());
        assertEquals(200.0, accountDetails.getBalanceUSD());
        verify(accountRepository).findById(accountId);
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenRetrievingNonExistentAccount() {
        // Arrange
        String nonExistentAccountId = "non-existent-id";

        when(accountRepository.findById(nonExistentAccountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> {
            accountService.getAccount(nonExistentAccountId);
        });

        verify(accountRepository).findById(nonExistentAccountId);
        verifyNoMoreInteractions(accountRepository);
    }
}


