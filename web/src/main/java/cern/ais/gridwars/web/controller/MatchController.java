package cern.ais.gridwars.web.controller;

import cern.ais.gridwars.MatchFile;
import cern.ais.gridwars.MatchDataSerializer;
import cern.ais.gridwars.web.controller.error.AccessDeniedException;
import cern.ais.gridwars.web.controller.error.NotFoundException;
import cern.ais.gridwars.web.domain.Match;
import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.MatchFileService;
import cern.ais.gridwars.web.service.MatchService;
import cern.ais.gridwars.web.service.RankingService;
import cern.ais.gridwars.web.util.ControllerUtils;
import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO separate out into a separate MatchFileController to make this class smaller

@Controller
@RequestMapping("/match")
public class MatchController {

    private static final Map<MatchFile, String> MATCH_FILE_URL_SUFFIX_MAPPING =
        Collections.unmodifiableMap(new HashMap<MatchFile, String>() {
            {
                put(MatchFile.STDOUT, "stdout");
                put(MatchFile.STDERR, "stderr");
                put(MatchFile.BOT_1_OUTPUT, "bot1");
                put(MatchFile.BOT_2_OUTPUT, "bot2");
            }
        });

    private final MatchDataSerializer serializer = new MatchDataSerializer();
    private final MatchService matchService;
    private final RankingService rankingService;
    private final MatchFileService matchFileService;

    @Autowired
    public MatchController(MatchService matchService, RankingService rankingService, MatchFileService matchFileService) {
        this.matchService = Objects.requireNonNull(matchService);
        this.rankingService = Objects.requireNonNull(rankingService);
        this.matchFileService = Objects.requireNonNull(matchFileService);
    }

    @GetMapping("/scores")
    public ModelAndView showScoreboard() {
        return ModelAndViewBuilder.forPage("match/scores")
            .addAttribute("rankings", rankingService.generateRankings())
            .toModelAndView();
    }

    @GetMapping("/list")
    public ModelAndView listStartedMatches() {
        List<Match> matches = matchService.getAllFinishedMatchesForActiveBots().stream()
            .sorted(Comparator.comparing(Match::getStarted).reversed())
            .collect(Collectors.toList());

        return ModelAndViewBuilder.forPage("match/list")
            .addAttribute("matches", matches)
            .toModelAndView();
    }

    @GetMapping("/{matchId}")
    public ModelAndView show(@PathVariable String matchId, @AuthenticationPrincipal User currentUser) {
        return matchService.getMatchById(matchId)
            .map(match -> createShowMatchResponse(match, currentUser))
            .orElseThrow(NotFoundException::new);
    }

    private ModelAndView createShowMatchResponse(Match match, User user) {
        ModelAndViewBuilder mavBuilder = ModelAndViewBuilder.forPage("match/show")
            .addAttribute("match", match)
            .addAttribute("matchDataUrl", "/match/" + match.getId() + "/data")
            .addAttribute("availableFiles", getAvailableMatchFiles(match, user));

        if (match.isFailed()) {
            mavBuilder.addAttribute("error", match.getFailReason());
        }

        return mavBuilder.toModelAndView();
    }

    private List<MatchFileInfo> getAvailableMatchFiles(Match match, User user) {
        return matchFileService.getAvailableMatchFiles(match, user).stream()
            .map(matchFile -> toMatchFileInfo(match.getId(), matchFile))
            .collect(Collectors.toList());
    }

    private MatchFileInfo toMatchFileInfo(String matchId, MatchFile matchFile) {
        return new MatchFileInfo(matchFile.description, createLinkForMatchFile(matchId, matchFile));
    }

    private String createLinkForMatchFile(String matchId, MatchFile matchFile) {
        return "/match/" + matchId + "/" + MATCH_FILE_URL_SUFFIX_MAPPING.get(matchFile);
    }

    @GetMapping("/{matchId}/data")
    public ResponseEntity<byte[]> matchData(@PathVariable String matchId,
                          @RequestHeader(name = HttpHeaders.ACCEPT_ENCODING, required = false) String acceptEncoding) {
        boolean acceptsGzipCompression = acceptsGzipEncoding(acceptEncoding);

        return getDeserializedMatchFileContent(matchId, MatchFile.TURN_DATA, acceptsGzipCompression)
            .map(dataStream -> createMatchFileContentResponse(dataStream, MediaType.APPLICATION_OCTET_STREAM,
                    acceptsGzipCompression))
            .orElseGet(ControllerUtils::createNotFoundByteDataResponse);
    }

    @GetMapping("/{matchId}/{matchFileUrlSuffix}")
    public ResponseEntity<byte[]> matchFile(@PathVariable String matchId, @PathVariable String matchFileUrlSuffix,
            @AuthenticationPrincipal User currentUser,
            @RequestHeader(name = HttpHeaders.ACCEPT_ENCODING, required = false) String acceptEncoding) {
        final MatchFile matchFile = getMatchFileByUrlSuffix(matchFileUrlSuffix.trim());
        final boolean acceptsGzipCompression = acceptsGzipEncoding(acceptEncoding);
        final boolean isCompressed = matchFile.compressed && acceptsGzipCompression;

        return matchService.getMatchById(matchId)
            .map(match -> validateUserAccessToMatchFile(match, currentUser, matchFile))
            .flatMap(match -> getDeserializedMatchFileContent(matchId, matchFile, acceptsGzipCompression))
            .map(dataStream -> createMatchFileContentResponse(dataStream, MediaType.TEXT_PLAIN, isCompressed))
            .orElseThrow(NotFoundException::new);
    }

    private boolean acceptsGzipEncoding(String acceptEncoding) {
        return StringUtils.hasLength(acceptEncoding) && acceptEncoding.toLowerCase().contains(ControllerUtils.GZIP);
    }

    private Match validateUserAccessToMatchFile(Match match, User user, MatchFile matchFile) {
        if (!matchFileService.canUserAccessMatchFile(user, match, matchFile)) {
            throw new AccessDeniedException();
        }
        return match;
    }

    private Optional<InputStream> getDeserializedMatchFileContent(String matchId, MatchFile matchFile,
                                                                  boolean acceptsGzipCompression) {
        String matchFilePath = matchFileService.createAbsoluteMatchFilePath(matchId, matchFile);

        return (!matchFile.compressed || acceptsGzipCompression)
            ? serializer.deserializeCompressedFromFile(matchFilePath)
            : serializer.deserializeUncompressedFromFile(matchFilePath);
    }

    private ResponseEntity<byte[]> createMatchFileContentResponse(InputStream data, MediaType contentType,
                                                                  boolean isCompressed) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
            .contentType(contentType)
            .cacheControl(ControllerUtils.FOREVER_CACHE_CONTROL);

        if (isCompressed) {
            builder.header(HttpHeaders.CONTENT_ENCODING, ControllerUtils.GZIP);
        }

        try (InputStream dataStream = data) { // Ensures that the stream will be closed afterwards!
            return builder.body(StreamUtils.copyToByteArray(dataStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MatchFile getMatchFileByUrlSuffix(String matchFileName) {
        return MATCH_FILE_URL_SUFFIX_MAPPING.entrySet().stream()
            .filter(entry -> entry.getValue().equals(matchFileName))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElseThrow(NotFoundException::new);
    }

    public static class MatchFileInfo {
        public final String name;
        public final String link;

        MatchFileInfo(String name, String link) {
            this.name = name;
            this.link = link;
        }
    }
}
