package com.bread.auth.controller;

import com.bread.auth.config.custom.AuthenticationAccount;
import com.bread.auth.entity.Account;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {

    @GetMapping("/login")
    public ModelAndView login(@RequestParam("redirect_uri") String redirectUri, @AuthenticationAccount Account account) {
        ModelAndView modelAndView = new ModelAndView();
        if (account != null) {
            modelAndView.setViewName("redirect:" + redirectUri);
        } else {
            modelAndView.setViewName("login");
            modelAndView.addObject("redirect_uri", redirectUri);
        }
        return modelAndView;
    }

}
