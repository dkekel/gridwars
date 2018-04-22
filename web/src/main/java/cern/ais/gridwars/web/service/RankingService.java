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
        matchService.getAllFinishedMatchesForActiveBots().forEach(match -> evaluateMatchForBothBots(match, userRankings));

        return userRankings.values().stream().sorted().collect(Collectors.toList());
    }

    private void evaluateMatchForBothBots(Match match, Map<String, RankingInfo> userRankings) {
        RankingInfo rankingInfoUser1 = getOrCreateRankingInfoForUser(match.getBot1().getUser(), userRankings);
        RankingInfo rankingInfoUser2 = getOrCreateRankingInfoForUser(match.getBot2().getUser(), userRankings);

        switch (match.getOutcome()) {
            case WIN:
                rankingInfoUser1.incrementWins();
                rankingInfoUser2.incrementLosses();
                break;
            case LOSS:
                rankingInfoUser1.incrementLosses();
                rankingInfoUser2.incrementWins();
                break;
            case DRAW:
                rankingInfoUser1.incrementDraws();
                rankingInfoUser2.incrementDraws();
        }
        // Failed matches are currently not taken into consideration for the ranking calculation.
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

    @Transactional(readOnly = true)
    public RankingInfo generateRankingInfoOfMatchesForBot(List<Match> matches, Bot bot) {
        RankingInfo rankingInfo = new RankingInfo(bot.getUser(), bot);
        matches.forEach(match -> evaluateMatchForBot(match, bot, rankingInfo));
        return rankingInfo;
    }

    private void evaluateMatchForBot(Match match, Bot bot, RankingInfo rankingInfo) {
        if (match.isBotWinner(bot)) {
            rankingInfo.incrementWins();
        } else if (match.isBotLoser(bot)) {
            rankingInfo.incrementLosses();
        } else if (match.isDraw()) {
            rankingInfo.incrementDraws();
        }
        // Failed matches are currently not taken into consideration for the ranking calculation.
    }

    public static final class RankingInfo implements Comparable<RankingInfo> {

        private final User user;
        private final Bot bot;
        private int wins = 0;
        private int draws = 0;
        private int losses = 0;
        private int scoreCache = -1;

        @Override
        public int compareTo(RankingInfo o) {
            // 1. higher score
            int comparisonResult = o.getScore() - getScore();

            // 2. more wins
            if (comparisonResult == 0) {
                comparisonResult = o.wins - wins;
            }

            // 3. more draws
            if (comparisonResult == 0) {
                comparisonResult = o.draws - draws;
            }

            // 3. less losses
            if (comparisonResult == 0) {
                comparisonResult = losses - o.losses;
            }

            // 4. first uploaded
            if (comparisonResult == 0) {
                comparisonResult = bot.getUploaded().compareTo(o.getBot().getUploaded());
            }

            return comparisonResult;
        }

        private RankingInfo(User user, Bot bot) {
            this.user = user;
            this.bot = bot;
        }

        private void incrementWins() {
            wins++;
            resetScoreCache();
        }

        private void incrementDraws() {
            draws++;
            resetScoreCache();
        }

        private void incrementLosses() {
            losses++;
            resetScoreCache();
        }

        private void resetScoreCache() {
            scoreCache = -1;
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

        public int getScore() {
            if (scoreCache == -1) {
                scoreCache = (wins * 3) + draws;
            }

            return scoreCache;
        }
    }
}
