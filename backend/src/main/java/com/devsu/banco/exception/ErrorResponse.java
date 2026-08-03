package com.devsu.banco.exception;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String message;
    private List<String> details;

    public ErrorResponse(LocalDateTime timestamp, int status, String message, List<String> details) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getDetails() {
        return details;
    }
}
