package cern.ais.gridwars.web.service;

import cern.ais.gridwars.MatchFile;
import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.domain.Match;
import cern.ais.gridwars.web.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
public class MatchFileService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final GridWarsProperties gridWarsProperties;

    public MatchFileService(GridWarsProperties gridWarsProperties) {
        this.gridWarsProperties = Objects.requireNonNull(gridWarsProperties);
    }

    public String createAbsoluteMatchFilePath(String matchId, MatchFile matchFile) {
        return matchFile.toAbsolutePath(createMatchBaseDirPath(matchId));
    }

    private String createMatchBaseDirPath(String matchId) {
        return Paths.get(gridWarsProperties.getDirectories().getMatchesDir(), matchId).toString();
    }

    public boolean canUserAccessMatchFile(User user, Match match, MatchFile matchFile) {
        if (user.isAdmin()) {
            return true;
        }

        // Everyone can access the turn data
        if (MatchFile.TURN_DATA == matchFile) {
            return true;
        }

        if ((MatchFile.BOT_1_OUTPUT == matchFile) && match.getBot1().getUser().equals(user)) {
            return true;
        }

        if ((MatchFile.BOT_2_OUTPUT == matchFile) && match.getBot2().getUser().equals(user)) {
            return true;
        }

        return false;
    }

    public List<MatchFile> getAvailableMatchFiles(Match match, User user) {
        List<MatchFile> availableFiles = new LinkedList<>();
        String matchDirPath = createMatchBaseDirPath(match.getId());

        addIfUserHasAccessAndFileHasContent(availableFiles, match, MatchFile.STDOUT, user, matchDirPath);
        addIfUserHasAccessAndFileHasContent(availableFiles, match, MatchFile.STDERR, user, matchDirPath);
        addIfUserHasAccessAndFileHasContent(availableFiles, match, MatchFile.BOT_1_OUTPUT, user, matchDirPath);
        addIfUserHasAccessAndFileHasContent(availableFiles, match, MatchFile.BOT_2_OUTPUT, user, matchDirPath);

        return availableFiles;
    }

    private void addIfUserHasAccessAndFileHasContent(List<MatchFile> availableMatchFiles, Match match,
                                                     MatchFile matchFile, User user, String matchDirPath) {
        if (canUserAccessMatchFile(user, match, matchFile) && matchFile.existsAndHasContent(matchDirPath)) {
            availableMatchFiles.add(matchFile);
        }
    }

    public Optional<String> getMatchFileTextContent(String matchId, MatchFile matchFile) {
        try {
            File file = matchFile.toFile(createMatchBaseDirPath(matchId));
            return existsAndHasContent(file)
                ? Optional.of(new String(Files.readAllBytes(file.toPath())))
                : Optional.empty();
        } catch (IOException e) {
            LOG.error("Failed to read file text content", e);
            return Optional.empty();
        }
    }

    private boolean existsAndHasContent(File file) {
        return file.exists() && file.canRead() && (file.length() > 0);
    }
}
