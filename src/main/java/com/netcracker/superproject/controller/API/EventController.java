package com.netcracker.superproject.controller.API;

import com.google.gson.Gson;
import com.netcracker.superproject.entity.Event;
import com.netcracker.superproject.persistence.EntityManager;
import com.netcracker.superproject.services.EventService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigInteger;

@RestController
@RequestMapping("/api/event")
public class EventController {

    EntityManager em = new EntityManager();
    EventService service = new EventService();

    @GetMapping("/get/{id}")
    public String getEvent(@PathVariable String id) {
        Gson gson = new Gson();
        return gson.toJson(em.read(new BigInteger(id), Event.class));
    }

    @GetMapping("/create")
    public String create () {
        return "createEvent";
    }

    @PostMapping("/add/{id}")
    public String add (@PathVariable BigInteger id){
       return service.addReference(id);
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "location", required = false) String location,
                         @RequestParam(value = "date", required = false) String date,
                         @RequestParam(value = "sponsor", required = false) String sponsor){

        return service.search(location, date, sponsor);
    }
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String createEvent(@ModelAttribute("event") @Valid Event event,
                       BindingResult result, HttpServletResponse response) {
        BigInteger id = null;
        if (!result.hasErrors()) {
           id = service.createNewEvent(event);
        }

        try {
            response.sendRedirect("/event/" + id);
        } catch (IOException e) {
            e.printStackTrace();
            return "false";
        }
        return "false";
    }

}
