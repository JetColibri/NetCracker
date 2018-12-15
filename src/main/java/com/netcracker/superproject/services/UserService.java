package com.netcracker.superproject.services;

import com.netcracker.superproject.entity.User;

public class UserService {

    public static User createUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(Security.md5Custom(password)); // пароль в md5
        return user;
    }


}
