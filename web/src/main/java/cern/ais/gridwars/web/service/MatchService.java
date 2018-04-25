package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.util.DomainUtils;
import cern.ais.gridwars.web.domain.Match;
import cern.ais.gridwars.web.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class MatchService {

    private static final List<Match.Status> STARTED_MATCH_STATUSES = Arrays.asList(
        Match.Status.FINISHED,
        Match.Status.FAILED
    );

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final MatchRepository matchRepository;
    private final BotService botService;
    private final GridWarsProperties gridWarsProperties;

    @Autowired
    public MatchService(MatchRepository matchRepository, BotService botService,
                        GridWarsProperties gridWarsProperties) {
        this.matchRepository = Objects.requireNonNull(matchRepository);
        this.botService = Objects.requireNonNull(botService);
        this.gridWarsProperties = Objects.requireNonNull(gridWarsProperties);
    }

    @Transactional
    public void generateMatches(Bot bot) {
        botService.getAllActiveBots().stream()
            .filter(otherBot -> !otherBot.equals(bot))
            .forEach(otherBot -> createMatches(bot, otherBot));
    }

    // TODO add matchmaking method that generates new matches for a given list of bots, or for all currently
    // active bots...

    private void createMatches(Bot bot1, Bot bot2) {
        int numberOfMatches = gridWarsProperties.getMatches().getMatchCountPerOpponent();

        for (int n = 0; n < numberOfMatches; n++) {
            // When generating the matches, shuffle around bot 1 and 2 to introduce a bit more
            // randomness and thus fairness. Otherwise, one bot will always get the first turn,
            // which can be an advantage for the first bot.
            if ((n % 2) == 0) {
                createSingleMatch(bot1, bot2);
            } else {
                createSingleMatch(bot2, bot1);
            }
        }

        matchRepository.flush();
        LOG.debug("Generated {} matches between bot {} and bot {}", numberOfMatches, bot1.getId(), bot2.getId());
    }

    private void createSingleMatch(Bot bot1, Bot bot2) {
        Match newMatch = new Match()
            .setId(DomainUtils.generateId())
            .setStatus(Match.Status.PENDING)
            .setPendingSince(Instant.now())
            .setBot1(bot1)
            .setBot2(bot2);

        matchRepository.save(newMatch);
    }

    @Transactional
    public void cancelPendingMatches(Bot bot) {
        matchRepository.findAllByBot1OrBot2(bot, bot).stream()
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
        match.setCancelled(Instant.now());
        matchRepository.save(match);
    }

    // TODO Implement method for cancelling all pending matches

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
        // The call below will create and commit a transaction on the fly, that's what we want (see comment above!).
        matchRepository.saveAndFlush(match);
        return match;
    }

    @Transactional
    public void returnMatchToPendingQueue(Match match) {
        match.setStatus(Match.Status.PENDING);
        match.setOutcome(null);
        match.setStarted(null);
        match.setEnded(null);
        matchRepository.saveAndFlush(match);
    }

    @Transactional(readOnly = true)
    public Optional<Match> getMatchById(String matchId) {
        return matchRepository.findById(matchId);
    }

    @Transactional(readOnly = true)
    public List<Match> getAllFinishedMatchesForActiveBots() {
        return matchRepository.findAllByStatusIn(Collections.singletonList(Match.Status.FINISHED)).stream()
            .filter(this::areBothBotsActive)
            .collect(Collectors.toList());
    }

    private boolean areBothBotsActive(Match match) {
        return match.getBot1().isActive() && match.getBot2().isActive();
    }

    @Transactional(readOnly = true)
    public List<Match> getAllStartedMatchesForBotAgainstActiveBots(Bot bot) {
        return matchRepository.findAllByBot1OrBot2(bot, bot).stream()
            .filter(this::wasMatchStarted)
            .filter(this::areBothBotsActive)
            .collect(Collectors.toList());
    }

    private boolean wasMatchStarted(Match match) {
        return STARTED_MATCH_STATUSES.contains(match.getStatus());
    }

    @Transactional(readOnly = true)
    public List<Match> getAllPublicMatchesForBotAgainstActiveBots(Bot bot) {
        return matchRepository.findAllByBot1OrBot2(bot, bot).stream()
            .filter(this::isPublic)
            .filter(this::areBothBotsActive)
            .collect(Collectors.toList());
    }

    private boolean isPublic(Match match) {
        return match.isFinished() && (Match.Outcome.DNF != match.getOutcome());
    }

    @Transactional(readOnly = true)
    public List<Match> getAllPendingMatches() {
        return matchRepository.findAllByStatus(Match.Status.PENDING);
    }

    @Transactional
    public void updateMatch(Match match) {
        matchRepository.saveAndFlush(match);
    }

//    public int getAverageMatchDurationMillis() {
//        return matchRepository.getAverageMatchDurationMillis();
//    }
}
