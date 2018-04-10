package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Objects;


@Service
public class BotUploadService {

    private final BotService botService;
    private final MatchService matchService;
    private final MatchExecutionService matchExecutionService;

    @Autowired
    public BotUploadService(BotService botService, MatchService matchService,
                      MatchExecutionService matchExecutionService) {
        this.botService = Objects.requireNonNull(botService);
        this.matchService = Objects.requireNonNull(matchService);
        this.matchExecutionService = Objects.requireNonNull(matchExecutionService);
    }

    @Transactional
    public void uploadNewBot(MultipartFile uploadedBotJarFile, User user, Instant uploadTime) {
        List<Bot> oldBots = botService.getActiveBotsOfUser(user);
        Bot newBot = botService.validateAndCreateNewUploadedBot(uploadedBotJarFile, user, uploadTime);
        inactivateOldBots(oldBots);
        matchService.generateMatches(newBot);

        // TODO Do the following outside of the transaction, maybe in the controller, or in a transaction commit callback?
        matchExecutionService.wakeUpAllMatchWorkers();
    }

    private void inactivateOldBots(List<Bot> oldBots) {
        oldBots.forEach(oldBot -> {
            matchService.cancelPendingMatches(oldBot);
            botService.inactivateBot(oldBot);
        });
    }
}
