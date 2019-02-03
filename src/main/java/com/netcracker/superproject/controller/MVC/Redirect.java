package com.netcracker.superproject.controller.MVC;

import com.netcracker.superproject.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;

@Controller
public class Redirect {
    @GetMapping("/profile")
    public void getProfile(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse) {
        RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        try {
            User activeUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            BigInteger id = activeUser.getId();

            redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse,
                    "/profile/"+id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
