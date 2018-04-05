package cern.ais.gridwars.web.repository;

import cern.ais.gridwars.web.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MatchRepository extends JpaRepository<Match, String> {

}
