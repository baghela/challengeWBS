package com.dws.challenge.exception;


/* This exception will be thrown when the amount to be transferred is 0 or other than numbers
* */
public class IncorrectAmountException  extends RuntimeException{
    public IncorrectAmountException(String message) {
        super(message);
    }
}
