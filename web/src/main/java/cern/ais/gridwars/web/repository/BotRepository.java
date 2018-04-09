package cern.ais.gridwars.web.repository;

import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BotRepository extends JpaRepository<Bot, String> {

    List<Bot> findAllByUserAndActiveIsTrue(User user);
}
