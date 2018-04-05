package cern.ais.gridwars.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Optional;


@Controller
public class AdminController {

    @GetMapping("/admin")
    public String admin(Model model, Principal currentUser) {
        model.addAttribute("user", extractUsername(currentUser));
        return "pages/admin/index";
    }

    private String extractUsername(Principal principal) {
        return Optional.ofNullable(principal).map(Principal::getName).orElse("<anonymous>");
    }
}
