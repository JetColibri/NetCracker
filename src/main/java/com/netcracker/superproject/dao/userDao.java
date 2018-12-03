package com.netcracker.superproject.dao;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class userDao {

    public void create(String objType, Map<String, Object> objFields) {
    }

    public Map<String, Object> getById(BigInteger id) {
        Map<String, Object> fields = new HashMap<>();
        //SQL
        return fields;
    }

    public void delete(BigInteger id){
        //SQL
    }

    public void update(BigInteger id, Map<String, Object> fields){
        //
    }
    private BigInteger createObject(String objType) {
        BigInteger id = new BigInteger("1");
        return id;
    }
}
