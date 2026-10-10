package com.ridevpn.ridevpn.controller;

import com.ridevpn.ridevpn.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {

    private final UserService userService;

    public IndexController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        Model model) {
        if (userService.checkLogin(username, password)) {
            model.addAttribute("message", "Login success");
        } else {
            model.addAttribute("message", "Login failed");
        }
        return "index";
    }
}
