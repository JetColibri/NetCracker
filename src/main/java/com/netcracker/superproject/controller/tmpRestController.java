package com.netcracker.superproject.controller;

import com.google.gson.Gson;
import com.netcracker.superproject.entity.User;
import com.netcracker.superproject.persistence.EntityManager;
import com.netcracker.superproject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class tmpRestController {

    @Autowired
    UserService service;

    @GetMapping("/profile")
    public String getProfile(){
        User activeUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Gson gson = new Gson();
        return gson.toJson(activeUser);
    }

    @PostMapping("/password")
    public String updatePassword(@RequestParam("password")String password, @RequestParam("newPassword")String newPassword){
        service.updatePassword(password, newPassword);
        return "true";
    }

    @GetMapping("/confirm")
    public String confirmEmail(@RequestParam("email")String email, @RequestParam("token")String token){
        service.confirmEmail(email, token);
        return "true";
    }

    @PostMapping("/email")
    public String updateEmail(@RequestParam("email") String email){
        service.updateEmail(email);
        return "true";
    }
}
