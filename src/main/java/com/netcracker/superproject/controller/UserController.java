package com.netcracker.superproject.controller;

import com.netcracker.superproject.entity.User;
import com.netcracker.superproject.persistence.EntityManager;
import com.netcracker.superproject.services.UserService;
import com.netcracker.superproject.springsecurity.EmailExistsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@RestController
public class UserController {

    EntityManager em = new EntityManager();
    UserService service = new UserService();

    @RequestMapping(value = "/user/registration", method = RequestMethod.POST)
    public ModelAndView registerUserAccount (@ModelAttribute("user") @Valid User accountDto,
                                             BindingResult result, WebRequest request, Errors errors) {
    User registered = new User();
    if (!result.hasErrors()) {
        registered = createUserAccount(accountDto, result);
    }
    if (registered == null) {
        result.rejectValue("email", "message.regError");
    }
    if (result.hasErrors()) {
        return new ModelAndView("registration", "user", accountDto);
    }
    else {
        return new ModelAndView("successRegister", "user", accountDto);
    }
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

}
