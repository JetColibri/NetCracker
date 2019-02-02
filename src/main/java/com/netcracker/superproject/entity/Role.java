package com.netcracker.superproject.entity;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority{
    USER,
    SPONSOR;


    @Override
    public String getAuthority() {
        return name();
    }
}
