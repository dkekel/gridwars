package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.DomainUtils;
import cern.ais.gridwars.web.domain.Match;
import cern.ais.gridwars.web.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
                        GridWarsProperties gridWarsProperties) {
        this.matchRepository = Objects.requireNonNull(matchRepository);
        this.botService = Objects.requireNonNull(botService);
        this.numberOfMatches = Objects.requireNonNull(gridWarsProperties.getMatches().getNumber());
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
            .setBot1(player1)
            .setBot2(player2);

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

    // IMPORTANT: This method is a critical join point of several worker threads and must ensure to
    // never hand out the same match twice, even if it's called in parallel by several worker threads. This is
    // ensure by making this method synchronized so that it can never be accessed more than once at the same time.
    //
    // Furthermore, this method MUST NOT BE EXECUTED within an ongoing transaction!! The transaction is committed
    // after the method finishes, which means that another thread can already execute the query here before
    // the changes of the last access are already committed. This can lead to the race condition where the
    // same match is returned twice. We ensure this by using Propagation.NEVER on this method (which will cause
    // an exception). This method modifies and updates the match object in the database, but this will be done in
    // an adhoc transaction when the ""matchRepository method is called. This guarantees that the changes are
    // visible in the database when this method here finishes.
    @Transactional(propagation = Propagation.NEVER)
    public synchronized Optional<Match> takeNextPendingMatch() {
        return matchRepository.findFirstByStatusOrderByPendingSinceAsc(Match.Status.PENDING)
            .map(this::markMatchAsStarted);
    }

    private Match markMatchAsStarted(Match match) {
        match.setStatus(Match.Status.RUNNING);
        match.setStarted(Instant.now());
        // the call below will create and commit a transaction on the fly, that's what we want (see comment above)
        matchRepository.saveAndFlush(match);
        return match;
    }

    @Transactional
    public void updateMatch(Match match) {
        matchRepository.saveAndFlush(match);
    }
}
