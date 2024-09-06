package com.tim.projectmanagement.exception.custom;

public class PasswordException extends RuntimeException{
    public PasswordException(String message) {
        super(message);
    }
}
