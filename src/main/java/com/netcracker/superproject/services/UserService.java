package com.netcracker.superproject.services;

import com.netcracker.superproject.entity.Role;
import com.netcracker.superproject.entity.User;
import com.netcracker.superproject.persistence.EntityManager;
import com.netcracker.superproject.springsecurity.EmailExistsException;
import lombok.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    EntityManager em = new EntityManager();

    public User registerNewUserAccount(final User accountDto) throws EmailExistsException {
        if (emailExist(accountDto.getEmail())) {
            throw new EmailExistsException("There is an account with that email adress: " + accountDto.getEmail());
        }

        accountDto.setPassword(new BCryptPasswordEncoder().encode(accountDto.getPassword()));
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

    public Optional<User> findUserByEmail(@NonNull String email) {
        EntityManager em = new EntityManager();
        return Optional.ofNullable((User) em.read(em.getIdByParam("email", email), User.class));
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        User user = findUserByEmail(email).orElseThrow(()->
                new UsernameNotFoundException("Email" + email + " wasn't found "));

            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setAuthorities(Collections.singletonList(Role.USER));
            user.setCredentialsNonExpired(true);
            user.setEnable(true);

        return user;
    }
}
