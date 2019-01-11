package com.netcracker.superproject.controller;

import com.google.gson.Gson;
import com.netcracker.superproject.entity.Event;
import com.netcracker.superproject.persistence.EntityManager;
import com.netcracker.superproject.services.EventService;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.math.BigInteger;

@RestController
@RequestMapping("/event")
public class EventController {

    EntityManager em = new EntityManager();
    EventService service = new EventService();

    @GetMapping("{id}")
    public String getEvent(@PathVariable String id){
        Event event = (Event) em.read(BigInteger.valueOf(Integer.parseInt(id)), Event.class);
        Gson gson = new Gson();
        return gson.toJson(event);
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
