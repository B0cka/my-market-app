package com.b0cka.ex;

public class BalanceError extends RuntimeException {
    public BalanceError(String message) {
        super(message);
    }
}
