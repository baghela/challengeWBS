package com.dws.challenge.exception;

/* This exception will be thrown when the sender does not have enough money to complete the transfer
 * */
public class InsufficientFundException extends RuntimeException {
    public InsufficientFundException(String message) {
        super(message);
    }
}
