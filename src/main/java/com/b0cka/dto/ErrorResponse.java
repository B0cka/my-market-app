package com.b0cka.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;


@Data
public class ErrorResponse {
    private String timestamp;
    private int status;
    private String message;

    public ErrorResponse(HttpStatus status, String message) {
        this.status = status.value();
        this.message = message;
    }
}
