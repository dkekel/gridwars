package cern.ais.gridwars.web.controller.admin;

import cern.ais.gridwars.web.domain.Match;
import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.MatchService;
import cern.ais.gridwars.web.service.MatchWorkerService;
import cern.ais.gridwars.web.util.ControllerUtils;
import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Controller
public class MatchQueueAdminController extends BaseAdminController {

    private final MatchService matchService;
    private final MatchWorkerService matchWorkerService;

    public MatchQueueAdminController(MatchService matchService, MatchWorkerService matchWorkerService) {
        this.matchService = Objects.requireNonNull(matchService);
        this.matchWorkerService = Objects.requireNonNull(matchWorkerService);
    }

    @GetMapping("queue")
    public ModelAndView showQueue() {
        return ModelAndViewBuilder.forPage("admin/queue")
            .addAttribute("matches", getAllPendingMatches())
            .toModelAndView();
    }

    private List<Match> getAllPendingMatches() {
        return matchService.getAllPendingMatches().stream()
            .sorted(Comparator.comparing(Match::getPendingSince))
            .collect(Collectors.toList());
    }

    @PostMapping("queue/reset")
    public ModelAndView clearAndReplayAllMatches(@AuthenticationPrincipal User currentUser) {
        ControllerUtils.assertIsAdmin(currentUser);
        matchService.clearAndReplayAllMatches();
        matchWorkerService.wakeUpAllMatchWorkers();
        return ModelAndViewBuilder.forRedirect("/admin/queue").toModelAndView();
    }
}
