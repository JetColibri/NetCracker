package com.netcracker.superproject.controller;

import com.netcracker.superproject.entity.User;
import com.netcracker.superproject.persistence.EntityManager;
import com.netcracker.superproject.services.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("user")
public class UserController {

    EntityManager em = new EntityManager();

    @PostMapping()
    public void create(@RequestBody Map<String, String> info) {
        User user = UserService.createUser(info.get("email"), info.get("password"));
        em.create(user);
    }

}
