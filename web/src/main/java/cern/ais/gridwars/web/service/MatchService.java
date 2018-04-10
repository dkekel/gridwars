package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.DomainUtils;
import cern.ais.gridwars.web.domain.Match;
import cern.ais.gridwars.web.repository.BotRepository;
import cern.ais.gridwars.web.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;


@Service
public class MatchService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final MatchRepository matchRepository;
    private final BotService botService;
    private final Integer numberOfMatches;

    @Autowired
    public MatchService(MatchRepository matchRepository, BotService botService,
                        @Value("${gridwars.matches.number}") Integer numberOfMatches) {
        this.matchRepository = Objects.requireNonNull(matchRepository);
        this.botService = Objects.requireNonNull(botService);
        this.numberOfMatches = Objects.requireNonNull(numberOfMatches);
    }

    @Transactional
    public void generateMatches(Bot bot) {
        botService.getAllActiveBots().stream()
            .filter(otherPlayer -> !otherPlayer.equals(bot))
            .forEach(otherPlayer -> createMatches(bot, otherPlayer));
    }

    private void createMatches(Bot bot1, Bot bot2) {
        for (int n = 0; n < numberOfMatches; n++) {
            createSingleMatch(bot1, bot2);
        }
        matchRepository.flush();
        LOG.debug("Generated {} matches between bot {} and bot {}", numberOfMatches, bot1.getId(), bot2.getId());
    }

    private void createSingleMatch(Bot player1, Bot player2) {
        Match newMatch = new Match()
            .setId(DomainUtils.generateId())
            .setStatus(Match.Status.PENDING)
            .setPendingSince(Instant.now())
            .setPlayer1(player1)
            .setPlayer2(player2);

        matchRepository.save(newMatch);
    }

    @Transactional
    public void cancelPendingMatches(Bot bot) {
        matchRepository.findMatchesByPlayer1OrPlayer2(bot, bot).stream()
            .filter(this::isPending)
            .forEach(this::cancelMatch);

        matchRepository.flush();
        LOG.debug("Cancelled pending matches of bot {}", bot.getId());
    }

    private boolean isPending(Match match) {
        return Match.Status.PENDING == match.getStatus();
    }

    private void cancelMatch(Match match) {
        match.setStatus(Match.Status.CANCELLED);
        matchRepository.save(match);
    }

    // TODO instead of using a synchronized method here, maybe try to obtain a read lock on the DB table?
    // A locking exception would mean the row (match) was already taken by another worker.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized Optional<Match> takeNextPendingMatch() {
        return matchRepository.findFirstByStatusOrderByPendingSinceAsc(Match.Status.PENDING)
            .map(this::markMatchAsStarted);
    }

    private Match markMatchAsStarted(Match match) {
        match.setStatus(Match.Status.RUNNING);
        match.setStarted(Instant.now());
        matchRepository.saveAndFlush(match);
        return match;
    }

    @Transactional
    public void updateMatch(Match match) {
        matchRepository.saveAndFlush(match);
    }
}
