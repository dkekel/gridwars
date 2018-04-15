package cern.ais.gridwars.web.controller;

import cern.ais.gridwars.web.service.MatchWorkerService;
import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private final MatchWorkerService matchWorkerService;

    public AdminController(MatchWorkerService matchWorkerService) {
        this.matchWorkerService = Objects.requireNonNull(matchWorkerService);
    }

    @GetMapping("/worker")
    public ModelAndView workerStatus() {
        return ModelAndViewBuilder.forPage("admin/worker")
            .addAttribute("workerStatuses", matchWorkerService.getMatchWorkerStatuses())
            .addAttribute("activeWorkerThreads", matchWorkerService.getActiveWorkerThreads())
            .addAttribute("maxWorkerThreads", matchWorkerService.getMaxWorkerThreads())
            .toModelAndView();
    }

    @PostMapping(path = "/worker", params = { "action=start" })
    public ModelAndView startWorkers(RedirectAttributes redirectAttributes) {
        matchWorkerService.startAllMatchWorkers();
        redirectAttributes.addFlashAttribute("started", true);
        return ModelAndViewBuilder.forRedirect("/admin/worker").toModelAndView();
    }

    @PostMapping(path = "/worker", params = { "action=wakeup" })
    public ModelAndView wakeUpWorkers(RedirectAttributes redirectAttributes) {
        matchWorkerService.wakeUpAllMatchWorkers();
        redirectAttributes.addFlashAttribute("wokenup", true);
        return ModelAndViewBuilder.forRedirect("/admin/worker").toModelAndView();
    }

    @PostMapping(path = "/worker", params = { "action=stop" })
    public ModelAndView stopWorkers(RedirectAttributes redirectAttributes) {
        matchWorkerService.stopAllMatchWorkers();
        redirectAttributes.addFlashAttribute("stopped", true);
        return ModelAndViewBuilder.forRedirect("/admin/worker").toModelAndView();
    }
}
