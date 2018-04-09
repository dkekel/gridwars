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
    private final BotRepository botRepository;
    private final Integer numberOfMatches;

    @Autowired
    public MatchService(MatchRepository matchRepository, BotRepository botRepository,
                        @Value("${gridwars.matches.number}") Integer numberOfMatches) {
        this.matchRepository = Objects.requireNonNull(matchRepository);
        this.botRepository = Objects.requireNonNull(botRepository);
        this.numberOfMatches = Objects.requireNonNull(numberOfMatches);
    }

    @Transactional
    public void generateMatches(Bot player) {
        botRepository.findAllByActiveIsTrue().stream()
            .filter(otherPlayer -> !otherPlayer.equals(player))
            .forEach(otherPlayer -> createMatches(player, otherPlayer));
    }

    private void createMatches(Bot player1, Bot player2) {
        for (int n = 0; n < numberOfMatches; n++) {
            createSingleMatch(player1, player2);
        }
        matchRepository.flush();
        LOG.debug("Generated {} matches between bot {} and bot {}", player1.getId(), player2.getId());
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
    public void cancelPendingMatches(Bot player) {
        matchRepository.findMatchesByPlayer1OrPlayer2(player, player).stream()
            .filter(this::isPending)
            .forEach(this::cancelMatch);

        matchRepository.flush();
        LOG.debug("Cancelled pending matches of bot {}", player.getId());
    }

    private boolean isPending(Match match) {
        return Match.Status.PENDING == match.getStatus();
    }

    private void cancelMatch(Match match) {
        match.setStatus(Match.Status.CANCELLED);
        matchRepository.save(match);
    }

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
}
