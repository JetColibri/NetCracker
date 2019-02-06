package com.netcracker.superproject.services;

import com.google.gson.Gson;
import com.netcracker.superproject.entity.Event;
import com.netcracker.superproject.entity.User;
import com.netcracker.superproject.persistence.EntityManager;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class EventService {

    private EntityManager em = new EntityManager();

    public BigInteger createNewEvent (final Event event) {
        User activeUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        event.setOrganizer(activeUser.getId());

        return em.create(event);
    }

    public String addReference(BigInteger id){
        User activeUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return em.addReference("reference", activeUser.getId(), id);
    }

    public String search(String location, String date, String sponsor){
        Map<String, String> map = new HashMap<>();
        Gson gson = new Gson();

        if(location != null){
            map.put("2003", location);
        }
        if(date != null){

            map.put("2007", date);
        }

        if(sponsor != null){
            map.put("2008", sponsor);
        }

        return gson.toJson(em.getSomeEntitiesByParam(Event.class, 1, 5, map));
    }
}
