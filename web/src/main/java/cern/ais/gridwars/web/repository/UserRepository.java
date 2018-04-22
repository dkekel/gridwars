package cern.ais.gridwars.web.repository;

import cern.ais.gridwars.web.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);
    Optional<User> findByConfirmationId(String confirmationId);
    List<User> findAllByAdminIsFalse();
    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, String id);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, String id);
    boolean existsByTeamName(String teamName);
    boolean existsByTeamNameAndIdNot(String teamName, String id);
}
