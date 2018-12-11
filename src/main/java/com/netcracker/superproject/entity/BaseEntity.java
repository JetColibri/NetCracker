package com.netcracker.superproject.entity;

import com.netcracker.superproject.entity.annotations.Attribute;

public class BaseEntity {

    @Attribute(type = "001")
    private Long id;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
