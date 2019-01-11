package com.netcracker.superproject.services;

import com.netcracker.superproject.entity.Event;
import com.netcracker.superproject.entity.User;
import com.netcracker.superproject.persistence.EntityManager;
import com.netcracker.superproject.springsecurity.EmailExistsException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;

public class EventService {

    private EntityManager em = new EntityManager();

    public Event createNewEvent (final Event event) {

        // event.setRegistrationDate(String.valueOf(LocalDate.now()));

        em.create(event);
        return event;
    }

}
