package cern.ais.gridwars.web.config;

import cern.ais.gridwars.web.controller.NewUserDto;
import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Paths;
import java.time.Instant;


/**
 * Provides some mock data for development when using in in-memory database.
 */
@Configuration
@Profile("dev")
public class DevConfiguration {

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
        // =====================================================================
        // Users
        // =====================================================================
        User user1 = userService.create(
            new NewUserDto()
                .setUsername("user1")
                .setPassword("bla1")
                .setEmail("user1@cern.ch")
                .setTeamName("Team User1"),
            true, false,false
        );

        User user2 = userService.create(
            new NewUserDto()
                .setUsername("user2")
                .setPassword("bla2")
                .setEmail("user2@cern.ch")
                .setTeamName("Team User2"),
            true, false,false
        );

        User user3 = userService.create(
            new NewUserDto()
                .setUsername("user3")
                .setPassword("bla3")
                .setEmail("user3@cern.ch")
                .setTeamName("Team User3"),
            true, false,false
        );

        User user4 = userService.create(
            new NewUserDto()
                .setUsername("user4")
                .setPassword("bla4")
                .setEmail("user4@cern.ch")
                .setTeamName("Team User4"),
            true, false,false
        );

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
        Bot bot1 = botService.createNewBotRecord(getBotFile("jaegerbot1.jar"), "cern.ais.gridwars.JaegerBot", user1, Instant.now());

        Bot bot2 = botService.createNewBotRecord(getBotFile("jaegerbot2.jar"), "cern.ais.gridwars.JaegerBot", user2, Instant.now());
        matchService.generateMatches(bot2);

        Bot bot3 = botService.createNewBotRecord(getBotFile("gintonicbot.jar"), "cern.ais.gridwars.GinTonicBot", user3, Instant.now());
        matchService.generateMatches(bot3);

        Bot bot4 = botService.createNewBotRecord(getBotFile("brugalcolabot.jar"), "cern.ais.gridwars.BrugalColaBot", user4, Instant.now());
        matchService.generateMatches(bot4);

        matchWorkerService.wakeUpAllMatchWorkers();

//        mailService.sendMail(MailService.MailBuilder.newMail()
//            .setTo("weltenrichter@gmail.com")
//            .setSubject("Hello from GridWars")
//            .setText("Mail sending is working!")
//        );
    }

    private File getBotFile(String botJarFileName) {
        return Paths.get(gridWarsProperties.getDirectories().getBotJarDir(), botJarFileName).toFile();
    }
}
