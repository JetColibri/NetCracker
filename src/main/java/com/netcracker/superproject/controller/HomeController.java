package com.netcracker.superproject.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    public HomeController() {
    }

    @RequestMapping({"/"})
    public String home() {
        return "Hello";
    }
}
