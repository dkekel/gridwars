package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.bean.BotInfo;
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
    private final MatchWorkerService matchWorkerService;

    @Autowired
    public BotUploadService(BotService botService, MatchService matchService,
                            MatchWorkerService matchWorkerService) {
        this.botService = Objects.requireNonNull(botService);
        this.matchService = Objects.requireNonNull(matchService);
        this.matchWorkerService = Objects.requireNonNull(matchWorkerService);
    }

    @Transactional
    public Bot uploadNewBot(BotInfo botInfo) {
        List<Bot> oldBots = botService.getAllActiveBotsOfUser(botInfo.getUploadUser());
        Bot newBot = botService.validateAndCreateNewUploadedBot(botInfo);

        inactivateOldBots(oldBots);
        matchService.generateMatches(newBot);

        // TODO Do the following outside of the transaction, maybe in the controller, or in a transaction commit callback?
        // Otherwise the workers may wake up before the transaction is committed and the changes are visible in the db.
        matchWorkerService.wakeUpAllMatchWorkers();

        return newBot;
    }

    @Transactional
    public void activateBot(String botId, User user) {
        botService.getActiveBotOfUser(user).ifPresent(botService::inactivateBot);
        botService.getBotById(botId).ifPresent(botService::activateBot);
    }

    private void inactivateOldBots(List<Bot> oldBots) {
        oldBots.forEach(oldBot -> {
            matchService.cancelPendingMatches(oldBot);
            botService.inactivateBot(oldBot);
        });
    }
}
