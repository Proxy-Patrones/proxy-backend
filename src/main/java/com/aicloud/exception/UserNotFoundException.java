package com.aicloud.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String userId) {
        super("Usuario no encontrado: " + userId);
    }
}
