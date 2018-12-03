package com.netcracker.superproject.dao.entity.attributes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectType {
    String type();
}
