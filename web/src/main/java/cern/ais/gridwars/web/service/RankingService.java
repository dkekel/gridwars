package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.Match;
import cern.ais.gridwars.web.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class RankingService {

    private final MatchService matchService;
    private final BotService botService;

    @Autowired
    public RankingService(MatchService matchService, BotService botService) {
        this.matchService = Objects.requireNonNull(matchService);
        this.botService = Objects.requireNonNull(botService);
    }

    @Transactional(readOnly = true)
    public List<RankingInfo> generateRankings() {
        Map<String, RankingInfo> userRankings = new HashMap<>();

        // TODO do we also need to exclude disabled users??
        matchService.getAllFinishedMatchesForActiveBots().forEach(match -> evaluateMatch(match, userRankings));

        return userRankings.values().stream().sorted().collect(Collectors.toList());
    }

    private void evaluateMatch(Match match, Map<String, RankingInfo> userRankings) {
        RankingInfo rankingInfoUser1 = getOrCreateRankingInfoForUser(match.getBot1().getUser(), userRankings);
        RankingInfo rankingInfoUser2 = getOrCreateRankingInfoForUser(match.getBot2().getUser(), userRankings);

        switch (match.getOutcome()) {
            case WIN:
                rankingInfoUser1.wins++;
                rankingInfoUser2.losses++;
                break;
            case LOSS:
                rankingInfoUser1.losses++;
                rankingInfoUser2.wins++;
                break;
            case DRAW:
                rankingInfoUser1.draws++;
                rankingInfoUser2.draws++;
        }
    }

    private RankingInfo getOrCreateRankingInfoForUser(User user, Map<String, RankingInfo> userRankings) {
        RankingInfo rankingInfo = userRankings.get(user.getId());

        if (rankingInfo == null) {
            Bot bot = botService.getActiveBotOfUser(user).orElse(null);
            rankingInfo = new RankingInfo(user, bot);
            userRankings.put(user.getId(), rankingInfo);
        }

        return rankingInfo;
    }


    public static final class RankingInfo implements Comparable<RankingInfo> {

        private final User user;
        private final Bot bot;
        private int wins = 0;
        private int draws = 0;
        private int losses = 0;

        @Override
        public int compareTo(RankingInfo o) {
            // TODO decide on a fair ranking system
            // - elo formula for wins, draws, losses, e.g. like in football?
            // - what if two or more teams have the same score, what are the secondary, tertiary, etc. criteria?
            return o.wins - wins;
        }

        private RankingInfo(User user, Bot bot) {
            this.user = user;
            this.bot = bot;
        }

        public User getUser() {
            return user;
        }

        public Bot getBot() {
            return bot;
        }

        public int getWins() {
            return wins;
        }

        public int getDraws() {
            return draws;
        }

        public int getLosses() {
            return losses;
        }

        public int getTotal() {
            return wins + draws + losses;
        }
    }
}
