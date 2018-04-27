package cern.ais.gridwars.web.controller;

import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.controller.error.NotFoundException;
import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.Match;
import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.BotService;
import cern.ais.gridwars.web.service.MatchService;
import cern.ais.gridwars.web.service.RankingService;
import cern.ais.gridwars.web.service.UserService;
import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/team")
public class TeamController {

    private final UserService userService;
    private final BotService botService;
    private final MatchService matchService;
    private final RankingService rankingService;
    private final GridWarsProperties gridWarsProperties;

    @Autowired
    public TeamController(UserService userService, BotService botService, MatchService matchService,
                          RankingService rankingService, GridWarsProperties gridWarsProperties) {
        this.userService = Objects.requireNonNull(userService);
        this.botService = Objects.requireNonNull(botService);
        this.matchService = Objects.requireNonNull(matchService);
        this.rankingService = Objects.requireNonNull(rankingService);
        this.gridWarsProperties = Objects.requireNonNull(gridWarsProperties);
    }

    @GetMapping // empty path parameter here maps is it to "" as well as "/"
    public ModelAndView showCurrentTeam(@AuthenticationPrincipal User currentUser) {
        return showTeam(currentUser.getId(), currentUser);
    }

    @GetMapping("/{userId}")
    public ModelAndView showTeam(@PathVariable String userId, @AuthenticationPrincipal User currentUser) {
        ModelAndViewBuilder mavBuilder = ModelAndViewBuilder.forPage("team/show");

        return userService.getById(userId)
            .map(userToShow -> {
                boolean isCurrentUserTheUserToShow = userToShow.equals(currentUser);
                mavBuilder.addAttribute("userToShow", userToShow);
                mavBuilder.addAttribute("isCurrentUserTheUserToShow", isCurrentUserTheUserToShow);
                mavBuilder.addAttribute("botUploadEnabled", isBotUploadEnabled());
                addActiveBotInfos(mavBuilder, userToShow, isCurrentUserTheUserToShow);
                return mavBuilder.toModelAndView();
            })
            .orElseThrow(NotFoundException::new);
    }

    private boolean isBotUploadEnabled() {
        return gridWarsProperties.getMatches().getBotUploadEnabled();
    }

    private void addActiveBotInfos(ModelAndViewBuilder mavBuilder, User userToShow, boolean isCurrentUserTheUserToShow) {
        botService.getActiveBotOfUser(userToShow).ifPresent(bot -> {
            List<Match> botMatches = getBotMatches(bot, isCurrentUserTheUserToShow);

            mavBuilder
                .addAttribute("activeBot", bot)
                .addAttribute("botMatches", botMatches)
                .addAttribute("rankingInfo", rankingService.generateRankingInfoOfMatchesForBot(botMatches, bot));
        });
    }

    private List<Match> getBotMatches(Bot bot, boolean isCurrentUserBotOwner) {
        List<Match> botMatches = isCurrentUserBotOwner
            ? matchService.getAllStartedMatchesForBotAgainstActiveBots(bot)
            : matchService.getAllPublicMatchesForBotAgainstActiveBots(bot);

        return botMatches.stream()
            .sorted(Comparator.comparing(Match::getStarted).reversed())
            .collect(Collectors.toList());
    }
}
