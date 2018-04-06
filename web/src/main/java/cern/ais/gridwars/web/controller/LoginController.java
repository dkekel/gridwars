package cern.ais.gridwars.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class LoginController {

    // IMPORTANT: Only map GET method here, not POST, to let the login filter to its work
    @GetMapping("/login")
    public String index() {
        return "pages/login";
    }
}
