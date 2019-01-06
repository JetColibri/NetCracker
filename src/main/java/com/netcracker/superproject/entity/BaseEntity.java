package com.netcracker.superproject.entity;

import com.netcracker.superproject.entity.annotations.Attribute;

import java.math.BigInteger;

public class BaseEntity {

    @Attribute(type = "0001")
    private BigInteger id;

    public BigInteger getId() {
        return this.id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }
}
