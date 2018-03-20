package cern.ais.gridwars.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class IndexController {

    @GetMapping(path = { "", "/" })
    public String index(Model model) {
        model.addAttribute("name", "Ben");
        return "index";
    }
}
