package com.tim.projectmanagement.exception.custom;

public class ApiException extends RuntimeException{
    public ApiException(String message) {
        super(message);
    }
}
