package cern.ais.gridwars.web.repository;

import cern.ais.gridwars.web.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByConfirmationId(String confirmationId);
    List<User> findAllByAdminIsFalse();
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, String id);
    boolean existsByTeamNameIgnoreCase(String teamName);
}
