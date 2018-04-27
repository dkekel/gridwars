package cern.ais.gridwars.web.controller;

import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/docs")
public class DocsController {

    @GetMapping("/getting-started")
    public ModelAndView gettingStarted() {
        return ModelAndViewBuilder.forPage("docs/getting-started").toModelAndView();
    }
}
