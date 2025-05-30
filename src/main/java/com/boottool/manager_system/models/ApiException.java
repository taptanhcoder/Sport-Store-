package com.boottool.manager_system.models;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}