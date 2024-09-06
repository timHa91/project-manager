package com.tim.projectmanagement.exception.custom;

public class InvalidLinkException extends RuntimeException{
    public InvalidLinkException(String message) {
        super(message);
    }
}
