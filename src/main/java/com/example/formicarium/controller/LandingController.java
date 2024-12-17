package com.example.formicarium.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingController {

    @GetMapping("/")
    public String showLandingPage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {
            //daca utilizatorul este autentficat se redirectioneaza pe pagina dashboard (prima pagina)
            return "redirect:/dashboard";
        }
        model.addAttribute("headerTitle", "Formicarium");
        return "landing";
    }
}
