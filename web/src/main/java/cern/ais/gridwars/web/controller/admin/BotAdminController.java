package cern.ais.gridwars.web.controller.admin;

import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.service.BotService;
import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Controller
public class BotAdminController extends BaseAdminController {

    private final BotService botService;

    public BotAdminController(BotService botService) {
        this.botService = Objects.requireNonNull(botService);
    }

    @GetMapping("bots")
    public ModelAndView listUsers() {
        return ModelAndViewBuilder.forPage("admin/bots")
            .addAttribute("bots", getAllBots())
            .toModelAndView();
    }

    private List<Bot> getAllBots() {
        return botService.getAllBots().stream()
            .sorted(Comparator.comparing(Bot::getUploaded).reversed())
            .collect(Collectors.toList());
    }
}
