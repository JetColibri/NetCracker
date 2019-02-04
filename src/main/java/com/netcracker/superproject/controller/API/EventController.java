package com.netcracker.superproject.controller.API;

import com.google.gson.Gson;
import com.netcracker.superproject.entity.Event;
import com.netcracker.superproject.persistence.EntityManager;
import com.netcracker.superproject.services.EventService;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.Valid;
import java.math.BigInteger;

@RestController
@RequestMapping("/api/event")
public class EventController {

    EntityManager em = new EntityManager();
    EventService service = new EventService();

    // TO DO
    @GetMapping("{id}")
    public String getEvent(@PathVariable String id) {
        Gson gson = new Gson();
        System.out.println(em.read(new BigInteger(id), Event.class).toString());
        return gson.toJson(em.read(new BigInteger(id), Event.class));
    }

    @GetMapping("/create")
    public String create () {
        return "createEvent";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public BigInteger createEvent(@ModelAttribute("event") @Valid Event event,
                                            BindingResult result, WebRequest request, Errors errors) {
        if (!result.hasErrors()) {
            service.createNewEvent(event);
        }

        return event.getId();
    }

}
