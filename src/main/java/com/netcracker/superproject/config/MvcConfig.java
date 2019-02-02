package com.netcracker.superproject.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/registration").setViewName("registration");
        //registry.addViewController("/event/create").setViewName("createEvent");
        registry.addViewController("/event").setViewName("createEvent");
        registry.addViewController("/profile").setViewName("profile");
        registry.addViewController("/profile/edit").setViewName("updProfile");
    }

}