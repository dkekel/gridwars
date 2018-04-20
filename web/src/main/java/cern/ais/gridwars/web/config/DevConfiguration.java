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
import java.util.ArrayList;
import java.util.List;


/**
 * Provides some mock data for development when using in in-memory database.
 */
@Configuration
//@Profile("dev") // TODO re-enable to only do this in dev!!!
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
        // TODO Ensure that this here is never executed in production!! Could this class be excluded when building the runnable jar?
        // See: https://stackoverflow.com/questions/19575474/gradle-how-to-exclude-a-particular-package-from-a-jar

        // =====================================================================
        // Users
        // =====================================================================
        List<User> testUsers = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            testUsers.add(userService.create(
                new NewUserDto()
                    .setUsername("user" + i)
                    .setPassword("bla" + i)
                    .setEmail("user" + i + "@cern.ch")
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

        Bot bot1 = botService.createNewBotRecord(sharedBotFile, "cern.ais.gridwars.bot.ExpandBot", testUsers.get(0), Instant.now());

        Bot bot2 = botService.createNewBotRecord(sharedBotFile, "cern.ais.gridwars.bot.JaegerBot", testUsers.get(1), Instant.now());
        matchService.generateMatches(bot2);

        Bot bot3 = botService.createNewBotRecord(sharedBotFile, "cern.ais.gridwars.bot.GinTonicBot", testUsers.get(2), Instant.now());
        matchService.generateMatches(bot3);

        Bot bot4 = botService.createNewBotRecord(sharedBotFile, "cern.ais.gridwars.bot.BrugalColaBot", testUsers.get(3), Instant.now());
        matchService.generateMatches(bot4);

        Bot bot5 = botService.createNewBotRecord(sharedBotFile, "cern.ais.gridwars.bot.PermissionCheckerBot", testUsers.get(4), Instant.now());
        matchService.generateMatches(bot5);

        matchWorkerService.wakeUpAllMatchWorkers();

//        mailService.sendMail(MailService.MailBuilder.newMail()
//            .setTo("weltenrichter@gmail.com")
//            .setSubject("Hello from GridWars")
//            .setText("Mail sending is working!")
//        );
    }
}
