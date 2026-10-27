package com.ridevpn.ridevpn.controller;

import com.ridevpn.ridevpn.service.WireGuardService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final WireGuardService wireGuardService;

    public ApiController(WireGuardService wireGuardService) {
        this.wireGuardService = wireGuardService;
    }

    @GetMapping("/me")
    public String me(Principal principal) {
        return "Hello " + principal.getName();
    }

    // Downloads config file for the client to use
    @GetMapping(value = "/config", produces = MediaType.TEXT_PLAIN_VALUE)
    public String config(Principal principal) throws Exception {
        return wireGuardService.createConfig(principal.getName());
    }
}
