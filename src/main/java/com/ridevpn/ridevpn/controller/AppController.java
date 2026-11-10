package com.ridevpn.ridevpn.controller;

import com.ridevpn.ridevpn.repository.UserRepository;
import com.ridevpn.ridevpn.service.UserService;
import java.security.Principal;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AppController {
    private final UserRepository users;
    private final UserService userService;

    public AppController(UserRepository users, UserService userService) {
        this.users = users;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/settings")
    public String settings() { return "settings"; }

    @GetMapping("/account")
    public String account(Principal principal, Model model) {
        var user = users.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("memberSince", DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
            .withZone(ZoneOffset.UTC).format(user.getCreatedAt()));
        return "account";
    }

    @PostMapping("/account/email")
    public String email(Principal principal, @RequestParam String email, RedirectAttributes flash) {
        try {
            userService.updateEmail(principal.getName(), email);
            flash.addFlashAttribute("message", "Email updated.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            flash.addFlashAttribute("error", "That email is already in use.");
        }
        return "redirect:/account";
    }

    @PostMapping("/account/password")
    public String password(Principal principal, @RequestParam String currentPassword,
            @RequestParam String newPassword, @RequestParam String confirmPassword, RedirectAttributes flash) {
        try {
            userService.updatePassword(principal.getName(), currentPassword, newPassword, confirmPassword);
            flash.addFlashAttribute("message", "Password updated.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/account";
    }
}
