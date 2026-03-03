package com.b0cka.ex;

public class RedisException extends RuntimeException {
    public RedisException(String message) {
        super(message);
    }
}
