package cern.ais.gridwars.web.controller.admin;

import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.MatchWorkerService;
import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;


@Controller
@RequestMapping("/admin")
public class WorkerAdminController {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final MatchWorkerService matchWorkerService;

    public WorkerAdminController(MatchWorkerService matchWorkerService) {
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
    public ModelAndView startWorkers(RedirectAttributes redirectAttributes, @AuthenticationPrincipal User currentUser) {
        logAction("START", currentUser);
        matchWorkerService.startAllMatchWorkers();
        redirectAttributes.addFlashAttribute("started", true);
        return ModelAndViewBuilder.forRedirect("/admin/worker").toModelAndView();
    }

    @PostMapping(path = "/worker", params = { "action=wakeup" })
    public ModelAndView wakeUpWorkers(RedirectAttributes redirectAttributes, @AuthenticationPrincipal User currentUser) {
        logAction("WAKE UP", currentUser);
        matchWorkerService.wakeUpAllMatchWorkers();
        redirectAttributes.addFlashAttribute("wokenup", true);
        return ModelAndViewBuilder.forRedirect("/admin/worker").toModelAndView();
    }

    @PostMapping(path = "/worker", params = { "action=stop" })
    public ModelAndView stopWorkers(RedirectAttributes redirectAttributes, @AuthenticationPrincipal User currentUser) {
        logAction("STOP", currentUser);
        matchWorkerService.stopAllMatchWorkers();
        redirectAttributes.addFlashAttribute("stopped", true);
        return ModelAndViewBuilder.forRedirect("/admin/worker").toModelAndView();
    }

    private void logAction(String action, User user) {
        LOG.info("Received worker {} request from: {}", action, user.getUsername());
    }
}
