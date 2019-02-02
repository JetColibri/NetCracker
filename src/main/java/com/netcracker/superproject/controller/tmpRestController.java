package com.netcracker.superproject.controller;

import com.google.gson.Gson;
import com.netcracker.superproject.entity.User;
import com.netcracker.superproject.persistence.EntityManager;
import com.netcracker.superproject.services.UserService;
import com.netcracker.superproject.springsecurity.EmailExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.jws.soap.SOAPBinding;
import javax.validation.Valid;
import java.math.BigInteger;

@RestController
@RequestMapping("/api/user")
public class tmpRestController {

    EntityManager em = new EntityManager();

    @Autowired
    UserService service;

    @GetMapping("/profile")
    public String getProfile(@RequestParam("id")String id){
        Gson gson = new Gson();
        return gson.toJson(em.read(new BigInteger(id), User.class));
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") @Valid User accountDto,
                                BindingResult result){
        if (!result.hasErrors()) {
            service.updateProfile(accountDto);
        }
        return "true";
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
    public String updateEmail(@RequestParam("email") String email, @RequestParam("password") String password){
        if(service.updateEmail(email, password)){
            return "true";
        }
        return "false";
    }

    @PostMapping(value = "/registration")
    public String registerUserAccount(@ModelAttribute("user") @Valid User accountDto,
                                            BindingResult result, WebRequest request, Errors errors) {
        User registered = new User();
        if (!result.hasErrors()) {
            try {
                registered = service.registerNewUserAccount(accountDto);
            } catch (EmailExistsException e) {
                return null;
            }
        }
        if (registered == null) {
            result.rejectValue("email", "message.regError");
            return "regError";
        }
        return "true";
    }
}
