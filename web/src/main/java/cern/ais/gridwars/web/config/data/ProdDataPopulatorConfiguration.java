package cern.ais.gridwars.web.config.data;

import cern.ais.gridwars.web.controller.user.NewUserDto;
import cern.ais.gridwars.web.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;


/**
 * Used in production mode to populate an empty database with the initial data. First checks if there is
 * already data and if not, it will populate the initial data.
 */
@Configuration
@Profile("prod")
public class ProdDataPopulatorConfiguration {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final transient UserService userService;

    @Autowired
    public ProdDataPopulatorConfiguration(final UserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    protected void populateInitialData() {
        // Make sure to never work on a non-empty user database
        if (userService.hasExistingUsers()) {
            return;
        }

        LOG.warn("User database is empty, populating initial set of users ...");

        // Create main admin account
        userService.create(
            new NewUserDto()
                .setUsername("admin")
                .setEmail("grid.wars@cern.ch")
                .setTeamName("Admin Team"),
            true, true, false
        );

        // Create additional admin accounts for the different level bots
        final List<String> adminPws = Arrays.asList(
            "zivuco62",
            "rutofu79",
            "cayilu74",
            "zodefo24",
            "luwulo19",
            "kiceri59",
            "kufasa62"
        );

        for (int i = 0; i < adminPws.size(); i++) {
            final int index = i + 1;

            userService.create(
                new NewUserDto()
                    .setUsername("admin" + index)
                    .setEmail("admin" + index + "@dummy.bla")
                    .setTeamName("Admin Level " + index),
                true, true, false
            );
        }

        // Create a single test user
        userService.create(
            new NewUserDto()
                .setUsername("test")
                .setEmail("test@dummy.bla")
                .setTeamName("Test Team"),
                false, true, false
        );
    }
}
