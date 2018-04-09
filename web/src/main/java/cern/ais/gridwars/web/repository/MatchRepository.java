package cern.ais.gridwars.web.repository;

import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface MatchRepository extends JpaRepository<Match, String> {

    List<Match> findMatchesByPlayer1OrPlayer2(Bot player1, Bot player2);
    Optional<Match> findFirstByStatusOrderByPendingSinceAsc(Match.Status status);
}
