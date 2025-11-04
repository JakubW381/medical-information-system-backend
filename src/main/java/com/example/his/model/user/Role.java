package com.example.his.model.user;

public enum Role {
    ROLE_USER("ROLE_USER"),
    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_DOCTOR("ROLE_DOCTOR"),
    ROLE_LAB("ROLE_LAB");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

