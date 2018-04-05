package cern.ais.gridwars.web.repository;

import cern.ais.gridwars.web.domain.Bot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BotRepository extends JpaRepository<Bot, String> {

}
