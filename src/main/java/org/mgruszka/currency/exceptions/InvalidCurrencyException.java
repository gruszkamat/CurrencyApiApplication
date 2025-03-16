package org.mgruszka.currency.exceptions;

public class InvalidCurrencyException extends RuntimeException {
    public InvalidCurrencyException(String msg) {
        super(msg);
    }
}
