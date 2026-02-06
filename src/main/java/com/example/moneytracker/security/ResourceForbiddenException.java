package com.example.moneytracker.security;

/**
 * Thrown when a resource exists but does not belong to the current user.
 * Mapped to HTTP 403 Forbidden to prevent cross-user data access.
 */
public class ResourceForbiddenException extends RuntimeException {

    public ResourceForbiddenException(String message) {
        super(message);
    }
}
