package com.ridevpn.ridevpn.controller;

import com.ridevpn.ridevpn.service.WireGuardService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
    @PostMapping(value = "/config", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> config(Principal principal) {
        try {
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"ridevpn.conf\"")
                .header("Cache-Control", "no-store")
                .body(wireGuardService.createConfig(principal.getName()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return configUnavailable();
        } catch (java.io.IOException | IllegalStateException e) {
            return configUnavailable();
        }
    }

    private ResponseEntity<String> configUnavailable() {
        return ResponseEntity.status(503).header("Cache-Control", "no-store")
            .body("Could not generate your VPN config. Please try again later.");
    }
}
