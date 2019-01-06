package com.netcracker.superproject.controller;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import com.netcracker.superproject.entity.User;
import com.netcracker.superproject.persistence.EntityManager;
import com.netcracker.superproject.services.UserService;
import com.netcracker.superproject.springsecurity.EmailExistsException;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
public class UserController {

    EntityManager em = new EntityManager();
    @Autowired
    UserService service;

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ModelAndView registerUserAccount(@ModelAttribute("user") @Valid User accountDto,
                                            BindingResult result, WebRequest request, Errors errors) {
        User registered = new User();
        if (!result.hasErrors()) {
            registered = createUserAccount(accountDto, result);
        }
        if (registered == null) {
            result.rejectValue("email", "message.regError");
        }
        return new ModelAndView("redirect:/login?registration");
    }

    @RequestMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "registration", required = false) String registration,
                        Model model) {
        model.addAttribute("error", error != null);
        model.addAttribute("logout", logout != null);
        model.addAttribute("registration", registration != null);
        return "login";
    }

    @GetMapping("/registration")
    public String registration () {
        return "registration";
    }

    private User createUserAccount(User accountDto, BindingResult result) {
        User registered = null;
        try {
            registered = service.registerNewUserAccount(accountDto);
        } catch (EmailExistsException e) {
            return null;
        }
        return registered;
    }

    @GetMapping("/profile")
    public String getProfile(){
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") @Valid User accountDto,
                                BindingResult result){
        if (!result.hasErrors()) {
            service.updateProfile(accountDto);
        }
       return "profile";
    }
}