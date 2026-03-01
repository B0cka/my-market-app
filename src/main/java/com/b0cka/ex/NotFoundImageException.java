package com.b0cka.ex;

import java.io.IOException;

public class NotFoundImageException extends IOException {
    public NotFoundImageException(String message) {
        super(message);
    }
}
