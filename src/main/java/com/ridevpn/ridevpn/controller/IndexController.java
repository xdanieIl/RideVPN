package com.ridevpn.ridevpn.controller;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping({"/", "/login"})
    public String index(Principal principal) {
        return principal == null ? "index" : "redirect:/dashboard";
    }
}
