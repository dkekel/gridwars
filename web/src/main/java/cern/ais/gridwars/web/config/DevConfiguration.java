package cern.ais.gridwars.web.config;

import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
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

    @PostConstruct
    public void initTestData() {
        // =====================================================================
        // Users
        // =====================================================================
        User user1 = userService.create(
            new User()
                .setUsername("user1")
                .setPassword("bla1")
                .setEmail("user1@cern.ch")
                .setTeamname("Team User1")
                .setEnabled(true)
        );

        User user2 = userService.create(
            new User()
                .setUsername("user2")
                .setPassword("bla2")
                .setEmail("user2@cern.ch")
                .setTeamname("Team User2")
                .setEnabled(true)
        );

        User user3 = userService.create(
            new User()
                .setUsername("user3")
                .setPassword("bla3")
                .setEmail("user3@cern.ch")
                .setTeamname("Team User3")
                .setEnabled(true)
        );

        User user4 = userService.create(
            new User()
                .setUsername("user4")
                .setPassword("bla4")
                .setEmail("user4@cern.ch")
                .setTeamname("Team User4")
                .setEnabled(true)
        );

        User admin1 = userService.create(
            new User()
                .setUsername("admin")
                .setPassword("blabla")
                .setEmail("admin@cern.ch")
                .setTeamname("Team Admin")
                .setAdmin(true)
                .setEnabled(true)
        );

        // =====================================================================
        // Bots and Matches
        // =====================================================================
        Bot bot1 = botService.createNewBotRecord("jaegerbot1.jar", "cern.ais.gridwars.JaegerBot", user1, Instant.now());

        Bot bot2 = botService.createNewBotRecord("jaegerbot2.jar", "cern.ais.gridwars.JaegerBot", user2, Instant.now());
        matchService.generateMatches(bot2);

        //Bot bot3 = botService.createNewBotRecord("gintonicbot.jar", "cern.ais.gridwars.GinTonicBot", user3, Instant.now());
        //matchService.generateMatches(bot3);

        //Bot bot4 = botService.createNewBotRecord("brugalcolabot.jar", "cern.ais.gridwars.BrugalColaBot", user4, Instant.now());
        //matchService.generateMatches(bot4);

        matchWorkerService.wakeUpAllMatchWorkers();

//        mailService.sendMail(MailService.MailBuilder.newMail()
//            .setTo("weltenrichter@gmail.com")
//            .setSubject("Hello from GridWars")
//            .setText("Mail sending is working!")
//        );
    }
}
