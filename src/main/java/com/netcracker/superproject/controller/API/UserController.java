package com.netcracker.superproject.controller.API;

import com.google.gson.Gson;
import com.netcracker.superproject.entity.User;
import com.netcracker.superproject.persistence.EntityManager;
import com.netcracker.superproject.services.UserService;
import com.netcracker.superproject.springsecurity.EmailExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigInteger;

@RestController
@RequestMapping("/api/user")
public class UserController {

    EntityManager em = new EntityManager();

    @Autowired
    UserService service;

    @GetMapping("{id}")
    public String getProfile(@PathVariable String id) {
        Gson gson = new Gson();
        return gson.toJson(em.read(new BigInteger(id), User.class));
    }


    @GetMapping("/profile")
    public String getProfile() {
        User activeUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Gson gson = new Gson();
        return gson.toJson(activeUser);
    }

    @PostMapping("/password")
    public String updatePassword(@RequestParam("password") String password, @RequestParam("newPassword") String newPassword) {
        service.updatePassword(password, newPassword);
        return "true";
    }

    @GetMapping("/confirm")
    public String confirmEmail(@RequestParam("email") String email, @RequestParam("token") String token) {
        service.confirmEmail(email, token);
        return "true";
    }

    @PostMapping("/email")
    public String updateEmail(@RequestParam("email") String email, @RequestParam("password") String password) {
        if (service.updateEmail(email, password)) {
            return "true";
        }
        return "false";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") @Valid User accountDto) {
        service.updateProfile(accountDto);
        return "profile";
    }

    @PostMapping("/registration")
    public String registerUserAccount(@ModelAttribute("user") @Valid User accountDto) {
        try {
            service.registerNewUserAccount(accountDto);
        } catch (EmailExistsException e) {
            e.printStackTrace();
        }
        return "true";
    }
}
