package cern.ais.gridwars.web.repository;

import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface MatchRepository extends JpaRepository<Match, String> {

    List<Match> findAllByBot1OrBot2(Bot bot1, Bot bot2);
    List<Match> findAllByStatus(Match.Status matchStatus);
    Optional<Match> findFirstByStatusOrderByPendingSinceAsc(Match.Status status);

//    @Query("select avg(m.duration) from Match m where m.duration is not null and m.status <> 'FAILED'")
//    int getAverageMatchDurationMillis();
}
