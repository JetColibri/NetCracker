package com.netcracker.superproject.controller;

import com.netcracker.superproject.entity.User;
import com.netcracker.superproject.persistence.EntityManager;
import com.netcracker.superproject.services.UserService;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Map;

@RestController
@RequestMapping("user")
public class UserController {

    EntityManager em = new EntityManager();

    @PostMapping
    public void create(@RequestBody Map<String, String> info) {
        User user = UserService.createUser(info.get("email"), info.get("password"));
        em.create(user);
    }

    @PostMapping
    public boolean login(@RequestBody Map<String, String> info) {
        BigInteger id = em.getIdByParam("email", info.get("email"));
        User user = em.read(id);
        if (user.getPassword().equals(info.get("password"))) {
            return true;
        } else {
            return false;
        }
    }

}
