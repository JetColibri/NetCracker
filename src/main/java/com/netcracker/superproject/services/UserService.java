package com.netcracker.superproject.services;

import com.netcracker.superproject.entity.User;
import com.netcracker.superproject.persistence.EntityManager;
import com.netcracker.superproject.springsecurity.EmailExistsException;

import java.math.BigInteger;

public class UserService {

    EntityManager em = new EntityManager();

    public static User createUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(Security.md5Custom(password)); // пароль в md5
        return user;
    }


    public User registerNewUserAccount(final User accountDto) throws EmailExistsException {
        if (emailExist(accountDto.getEmail())) {
            throw new EmailExistsException("There is an account with that email adress: " + accountDto.getEmail());
        }

        final User user = new User();
        user.setFirstName(accountDto.getFirstName());
        user.setLastName(accountDto.getLastName());
        user.setPassword(Security.md5Custom(accountDto.getPassword()));
        user.setEmail(accountDto.getEmail());
        em.create(user);
        return user;
    }
    private boolean emailExist(String email) {
        BigInteger id = em.getIdByParam("email", email);
        User user = em.read(id);
        if (user != null) {
            return true;
        }
        return false;
    }


}
