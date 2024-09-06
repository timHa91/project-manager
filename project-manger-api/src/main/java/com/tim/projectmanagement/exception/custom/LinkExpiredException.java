package com.tim.projectmanagement.exception.custom;

public class LinkExpiredException extends RuntimeException{
    public LinkExpiredException(String message) {
        super(message);
    }
}
