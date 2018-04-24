package cern.ais.gridwars.web.config.data;

import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.controller.user.NewUserDto;
import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


/**
 * Populates some test data used during development. Should only be used on volatile in-memory databases.
 */
@Configuration
@Profile("dev")
public class DevDataPopulatorConfiguration {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private BotService botService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchWorkerService matchWorkerService;

    @Autowired
    private MailService mailService;

    @Autowired
    private GridWarsProperties gridWarsProperties;

    @PostConstruct
    public void initTestData() {
        // Make sure we never work on a non-empty user database
        if (userService.hasExistingUsers()) {
            return;
        }

        LOG.warn("Populating development users and bots ...");

        // =====================================================================
        // Users
        // =====================================================================
        List<User> testUsers = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            testUsers.add(userService.create(
                new NewUserDto()
                    .setUsername("user" + i)
                    .setPassword("bla" + i)
                    .setEmail("user" + i + "@blubb.bla")
                    .setTeamName("Team User" + i),
                false, true, false));
        }

        User admin1 = userService.create(
            new NewUserDto()
                .setUsername("admin")
                .setPassword("blabla")
                .setEmail("admin@cern.ch")
                .setTeamName("Team Admin"),
            true, true, false
        );

        // =====================================================================
        // Bots and Matches
        // =====================================================================
        File sharedBotFile = Paths.get(gridWarsProperties.getDirectories().getBotJarDir(), "gridwars-bots.jar").toFile();

        Bot bot1 = botService.createNewBotRecord(sharedBotFile, "cern.ais.gridwars.bot.ExpandBot", testUsers.get(0), Instant.now(), null);

        Bot bot2 = botService.createNewBotRecord(sharedBotFile, "cern.ais.gridwars.bot.JaegerBot", testUsers.get(1), Instant.now(), null);
        matchService.generateMatches(bot2);

//        Bot bot3 = botService.createNewBotRecord(sharedBotFile, "cern.ais.gridwars.bot.GinTonicBot", testUsers.get(2), Instant.now(), null);
//        matchService.generateMatches(bot3);
//
//        Bot bot4 = botService.createNewBotRecord(sharedBotFile, "cern.ais.gridwars.bot.BrugalColaBot", testUsers.get(3), Instant.now(), null);
//        matchService.generateMatches(bot4);
//
//        Bot bot5 = botService.createNewBotRecord(sharedBotFile, "cern.ais.gridwars.bot.PermissionCheckerBot", testUsers.get(4), Instant.now(), null);
//        matchService.generateMatches(bot5);
//
//        Bot bot6 = botService.createNewBotRecord(sharedBotFile, "cern.ais.gridwars.bot.IdleBot", testUsers.get(5), Instant.now(), null);
//        matchService.generateMatches(bot6);

        matchWorkerService.wakeUpAllMatchWorkers();

//        mailService.sendMail(MailService.MailBuilder.newMail()
//            .setTo("weltenrichter@gmail.com")
//            .setSubject("Hello from GridWars")
//            .setText("Mail sending is working!")
//        );
    }
}
