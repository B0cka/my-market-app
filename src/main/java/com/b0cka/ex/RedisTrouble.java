package com.b0cka.ex;

public class RedisTrouble extends RuntimeException {
    public RedisTrouble(String message) {
        super(message);
    }
}
