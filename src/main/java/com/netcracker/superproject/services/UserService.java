package com.netcracker.superproject.services;

import com.netcracker.superproject.entity.User;
import com.netcracker.superproject.persistence.EntityManager;
import com.netcracker.superproject.springsecurity.EmailExistsException;

import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDate;

public class UserService {

    EntityManager em = new EntityManager();

    public User registerNewUserAccount(final User accountDto) throws EmailExistsException {
        if (emailExist(accountDto.getEmail())) {
            throw new EmailExistsException("There is an account with that email adress: " + accountDto.getEmail());
        }
        accountDto.setPassword(Security.md5Custom(accountDto.getPassword()));
        accountDto.setRole("0");
        accountDto.setRegistrationDate(String.valueOf(LocalDate.now()));
        em.create(accountDto);
        return accountDto;
    }

    private boolean emailExist(String email) {
        BigInteger id = em.getIdByParam("email", email);
        if (id != null) {
            return true;
        }
        return false;
    }


}
