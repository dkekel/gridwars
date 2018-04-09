package cern.ais.gridwars.web.config;

import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;


/**
 * Provides some mock data for development when using in in-memory database.
 */
@Configuration
@Profile("dev")
public class DevConfiguration {

    @Autowired
    private UserService userService;

    @PostConstruct
    public void initTestData() {
        initTestUsers();
    }

    private void initTestUsers() {
        userService.create(
            new User()
                .setUsername("user1")
                .setPassword("bla1")
                .setEmail("user1@cern.ch")
                .setTeamname("Team User1"));

        userService.create(
            new User()
                .setUsername("user2")
                .setPassword("bla2")
                .setEmail("user2@cern.ch")
                .setTeamname("Team User2"));

        userService.create(
            new User()
                .setUsername("user3")
                .setPassword("bla3")
                .setEmail("user3@cern.ch")
                .setTeamname("Team User3")
                .setEnabled(false));

        userService.create(
            new User()
                .setUsername("admin")
                .setPassword("blabla")
                .setEmail("admin@cern.ch")
                .setTeamname("Team Admin")
                .setAdmin(true));
    }

    private void initTestBots() {
        // TODO implement...
    }
}
