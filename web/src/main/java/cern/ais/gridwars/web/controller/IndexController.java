package cern.ais.gridwars.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Optional;


@Controller
public class IndexController {

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal Principal currentUser) {
        String username = Optional.ofNullable(currentUser).map(Principal::getName).orElse("<anonymous>");
        model.addAttribute("user", username);
        return "index";
    }
}
