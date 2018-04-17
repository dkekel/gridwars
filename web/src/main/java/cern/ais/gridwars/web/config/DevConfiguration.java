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
        final long MEGA_BYTE_FACTOR = 1024 * 1024;
        final Runtime runtime = Runtime.getRuntime();
        final long usedMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / MEGA_BYTE_FACTOR;
        final long maxMemoryMb = runtime.maxMemory() / MEGA_BYTE_FACTOR;
        System.out.println("#### Memory usage [MB}: " + usedMemoryMb + " / " + maxMemoryMb);

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

        Bot bot1 = botService.createNewBotRecord(sharedBotFile, "cern.ais.gridwars.ExpandBot", testUsers.get(0), Instant.now());

        Bot bot2 = botService.createNewBotRecord(sharedBotFile, "cern.ais.gridwars.JaegerBot", testUsers.get(1), Instant.now());
        matchService.generateMatches(bot2);

        Bot bot3 = botService.createNewBotRecord(sharedBotFile, "cern.ais.gridwars.GinTonicBot", testUsers.get(2), Instant.now());
        matchService.generateMatches(bot3);

        Bot bot4 = botService.createNewBotRecord(sharedBotFile, "cern.ais.gridwars.BrugalColaBot", testUsers.get(3), Instant.now());
        matchService.generateMatches(bot4);

        Bot bot5 = botService.createNewBotRecord(sharedBotFile, "cern.ais.gridwars.PermissionCheckerBot", testUsers.get(4), Instant.now());
        matchService.generateMatches(bot5);

        matchWorkerService.wakeUpAllMatchWorkers();

//        mailService.sendMail(MailService.MailBuilder.newMail()
//            .setTo("weltenrichter@gmail.com")
//            .setSubject("Hello from GridWars")
//            .setText("Mail sending is working!")
//        );
    }
}
