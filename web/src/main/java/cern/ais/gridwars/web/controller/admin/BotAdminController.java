package cern.ais.gridwars.web.controller.admin;

import cern.ais.gridwars.web.controller.error.NotFoundException;
import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.service.BotService;
import cern.ais.gridwars.web.service.MatchService;
import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Controller
public class BotAdminController extends BaseAdminController {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final BotService botService;
    private final MatchService matchService;

    public BotAdminController(BotService botService, MatchService matchService) {
        this.botService = Objects.requireNonNull(botService);
        this.matchService = Objects.requireNonNull(matchService);
    }

    @GetMapping("bots")
    public ModelAndView listUsers() {
        return ModelAndViewBuilder.forPage("admin/bots")
            .addAttribute("bots", getAllBots())
            .toModelAndView();
    }

    private List<Bot> getAllBots() {
        return botService.getAllBots().stream()
            .sorted()
            .collect(Collectors.toList());
    }

    @PostMapping(path = "bots/{botId}", params = { "action=deactivate" })
    public ModelAndView deactivateBot(@PathVariable String botId, RedirectAttributes redirectAttributes) {
        return botService.getBotById(botId)
            .map(bot -> inactivateBot(bot, redirectAttributes))
            .orElseThrow(NotFoundException::new);
    }

    private ModelAndView inactivateBot(Bot bot, RedirectAttributes redirectAttributes) {
        try {
            matchService.cancelPendingMatches(bot);
            botService.inactivateBot(bot);
            redirectAttributes.addFlashAttribute("success", "Bot \"" + bot.getBotClassName() +
                "\" was deactivated");
        } catch (Exception e) {
            LOG.error("Bot could not be deactivated: {}", bot.getId(), e);
            redirectAttributes.addFlashAttribute("error", "Deactivation of bot \"" +
                bot.getBotClassName() + "\" failed");
        }

        return ModelAndViewBuilder.forRedirect("/admin/bots").toModelAndView();
    }
}
