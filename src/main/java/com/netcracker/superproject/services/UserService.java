package com.netcracker.superproject.services;

import com.netcracker.superproject.entity.Role;
import com.netcracker.superproject.entity.User;
import com.netcracker.superproject.persistence.EntityManager;
import com.netcracker.superproject.springsecurity.EmailExistsException;
import lombok.NonNull;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private MailSender mailSender;
    private EntityManager em = new EntityManager();

    public User registerNewUserAccount(final User accountDto) throws EmailExistsException {
        String email;

        if (emailExist(accountDto.getEmail())) {
            throw new EmailExistsException("There is an account with that email adress: " + accountDto.getEmail());
        }

        accountDto.setPassword(new BCryptPasswordEncoder().encode(accountDto.getPassword()));
        accountDto.setRole("0");
        accountDto.setRegistrationDate(LocalDate.now());

        email = accountDto.getEmail();
        accountDto.setEmail("");
        accountDto.setTmpEmail(email);
        accountDto.setToken(DigestUtils.md5Hex(email + "Hello"));

        em.create(accountDto);

        String message = String.format(
                "Hello, %s!\n" +
                        "Please, visit next link: http://localhost:8181/api/user/confirm?email=%s&token=%s",
                accountDto.getFirstName(),
                email,
                accountDto.getToken()
        );

        try {
            mailSender.send(email, "Activation code", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accountDto;
    }

    public void updateProfile(User accountDto) {
        String email;
        User activeUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        email = activeUser.getEmail();

        em.update(em.getIdByParam("email", email), accountDto);

        User user = findUserByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("Email" + email + " wasn't found "));
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean emailExist(String email) {
        BigInteger id = em.getIdByParam("email", email);
        if (id != null) {
            return true;
        }
        return false;
    }

    private Optional<User> findUserByEmail(@NonNull String email) {
        EntityManager em = new EntityManager();
        return Optional.ofNullable((User) em.read(em.getIdByParam("email", email), User.class));
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        User user = findUserByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("Email" + email + " wasn't found "));

        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setAuthorities(Collections.singletonList(Role.USER));
        user.setCredentialsNonExpired(true);
        user.setEnable(true);

        return user;
    }

    public void updatePassword(String pass, String newPass) {
        User activeUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String newPassword = new BCryptPasswordEncoder().encode(newPass);
        User userWithNewPass = new User();
        userWithNewPass.setPassword(newPassword);

        if (BCrypt.checkpw(pass, activeUser.getPassword())) {
            em.update(activeUser.getId(), userWithNewPass);
            System.out.println("new Password");
        } else {
            System.out.println("Bad password");
        }
    }

    public boolean updateEmail(String email, String password) {
        User activeUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        if(!BCrypt.checkpw(password, activeUser.getPassword())){
           return false;
        }

        User user = new User();
        user.setTmpEmail(email);
        user.setToken(DigestUtils.md5Hex(email + "Hello"));

        String message = String.format(
                "Hello, %s!\n" +
                        "Please, visit next link: http://localhost:8181/api/user/confirm?email=%s&token=%s",
                activeUser.getFirstName(),
                email,
                user.getToken()
        );

        try {
            mailSender.send(email, "Update email", message);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Can't send message");
        }

        em.update(activeUser.getId(), user);

        return true;
    }

    public void confirmEmail(String email, String token) {
        BigInteger id = em.getIdByParam("tmpEmail", email);
        User user = (User) em.read(id, User.class);

        if (user.getToken().equals(token)) {
            user.setToken("");
            user.setTmpEmail("");
            user.setEmail(email);

            em.update(id, user);
        }
    }
}
