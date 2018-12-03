package com.netcracker.superproject.dao.service;

import com.netcracker.superproject.dao.userDao;
import com.netcracker.superproject.dao.entity.User;

import java.math.BigInteger;
import java.util.Map;

public class UserServiceImpl extends EntityServiceImpl {

    public void create(Object obj) {
        new userDao().create(objectType(obj), getAllFields(obj));
    }

    public User getUserById(BigInteger id) {
        return (User) new EntityServiceImpl().setAllFields("user", new userDao().getById(id));
    }

    public void update(BigInteger id, Object obj) {
        Map<String, Object> fields = getAllFields(obj);
        delNull(fields);
        new userDao().update(id, fields);
    }

    public void delete(BigInteger id) {
        new userDao().delete(id);
    }
}
