package com.ridevpn.ridevpn.controller;

import com.ridevpn.ridevpn.repository.UserRepository;
import com.ridevpn.ridevpn.service.UserService;
import java.security.Principal;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final String installerPath;

    public AppController(UserRepository users, UserService userService,
            @Value("${ridevpn.client-installer}") String installerPath) {
        this.users = users;
        this.userService = userService;
        this.installerPath = installerPath;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    // Downloads RideVPN-Setup.exe - the file lives in /opt/ridevpn/client on the server
    @GetMapping("/download/client")
    public ResponseEntity<Resource> downloadClient() {
        Resource installer = new FileSystemResource(installerPath);
        if (!installer.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("RideVPN-Setup.exe").build().toString())
            .body(installer);
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
