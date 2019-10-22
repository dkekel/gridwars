package cern.ais.gridwars.web.repository;

import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface BotRepository extends JpaRepository<Bot, String> {

    Optional<Bot> findFirstByUserAndActiveIsTrueOrderByUploadedDesc(User user);
    List<Bot> findAllByUserAndActiveIsTrueOrderByUploadedDesc(User user);
    List<Bot> findAllByUserOrderByUploadedDesc(User user);
    List<Bot> findAllByActiveIsTrue();
}
