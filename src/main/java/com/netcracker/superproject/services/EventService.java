package com.netcracker.superproject.services;

import com.netcracker.superproject.entity.Event;
import com.netcracker.superproject.entity.User;
import com.netcracker.superproject.persistence.EntityManager;
import com.netcracker.superproject.springsecurity.EmailExistsException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;

public class EventService {

    private EntityManager em = new EntityManager();

    public Event createNewEvent (final Event event) {
        User activeUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        event.setOrganizer(activeUser.getId());
        em.create(event);
        return event;
    }

}
