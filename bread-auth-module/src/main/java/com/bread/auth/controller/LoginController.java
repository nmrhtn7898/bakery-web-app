package com.bread.auth.controller;

import com.bread.auth.annotation.AuthenticationAccount;
import com.bread.auth.entity.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam("redirect_uri") String redirectUri, @AuthenticationAccount Account account, Model model) {
        if (account != null) {
            return "redirect:" + redirectUri;
        } else {
            model.addAttribute("redirect_uri", redirectUri);
            return "login";
        }
    }

}
