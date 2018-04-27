package cern.ais.gridwars.web.controller;

import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class IndexController {

    @GetMapping("/")
    public ModelAndView index() {
        return ModelAndViewBuilder.forPage("index").toModelAndView();
    }
}
