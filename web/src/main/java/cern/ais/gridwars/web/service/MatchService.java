package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service
public class MatchService {

    private final MatchRepository matchRepository;

    @Autowired
    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = Objects.requireNonNull(matchRepository);
    }

    @Transactional
    public void cancelMatches(Bot bot) {
        // TODO implement
    }

    @Transactional
    public void generateMatches(Bot bot) {
        // TODO implement
    }
}
