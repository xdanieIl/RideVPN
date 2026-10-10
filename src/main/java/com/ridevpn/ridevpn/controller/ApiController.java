package com.ridevpn.ridevpn.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/me")
    public String me(Principal principal) {
        return "Hello " + principal.getName();
    }
}
