package cern.ais.gridwars.web.controller;

import cern.ais.gridwars.runtime.MatchFile;
import cern.ais.gridwars.runtime.MatchTurnDataSerializer;
import cern.ais.gridwars.web.controller.error.AccessDeniedException;
import cern.ais.gridwars.web.controller.error.NotFoundException;
import cern.ais.gridwars.web.domain.Match;
import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.MatchFileService;
import cern.ais.gridwars.web.service.MatchService;
import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/match")
public class MatchController {

    public static class MatchFileInfo {
        public final String name;
        public final String link;

        public MatchFileInfo(String name, String link) {
            this.name = name;
            this.link = link;
        }
    }

    private static final String GZIP = "gzip";

    private final MatchTurnDataSerializer serializer = new MatchTurnDataSerializer();
    private final MatchService matchService;
    private final MatchFileService matchFileService;

    @Autowired
    public MatchController(MatchService matchService, MatchFileService matchFileService) {
        this.matchService = Objects.requireNonNull(matchService);
        this.matchFileService = matchFileService;
    }

    @GetMapping("/scores")
    public ModelAndView showScoreboard() {
        return ModelAndViewBuilder.forPage("match/scores").toModelAndView();
    }

    @GetMapping("/list")
    public ModelAndView listMatches() {
        return ModelAndViewBuilder.forPage("match/list")
            .addAttribute("matches", matchService.findAllPlayedMatchesByActiveBots())
            .toModelAndView();
    }

    @GetMapping("/{matchId}")
    public ModelAndView show(@PathVariable String matchId, @AuthenticationPrincipal User user) {
        return matchService.loadMatch(matchId)
            .map(match ->
                ModelAndViewBuilder.forPage("match/show")
                    .addAttribute("matchDataUrl", "/match/" + matchId + "/data")
                    .addAttribute("availableFiles", getAvailableMatchFiles(match, user))
                    .toModelAndView()
            )
            .orElseThrow(NotFoundException::new);
    }

    private List<MatchFileInfo> getAvailableMatchFiles(Match match, User user) {
        return matchFileService.getAvailableMatchFiles(match, user).stream()
            .map(matchFile -> toMatchFileInfo(match.getId(), matchFile))
            .collect(Collectors.toList());
    }

    private MatchFileInfo toMatchFileInfo(String matchId, MatchFile matchFile) {
        return new MatchFileInfo(getDescriptionOf(matchFile), createLinkForMatchFile(matchId, matchFile));
    }

    private String getDescriptionOf(MatchFile matchFile) {
        switch (matchFile) {
            case STDOUT: return "Standard Output";
            case STDERR: return "Error Output";
            case BOT_1_OUTPUT: return "Bot 1 Output";
            case BOT_2_OUTPUT: return "Bot 2 Output";
            default: return ""; // Should not happen
        }
    }

    private String createLinkForMatchFile(String matchId, MatchFile matchFile) {
        String linkBase = "/match/" + matchId + "/";

        switch (matchFile) {
            case STDOUT:  return linkBase + "stdout";
            case STDERR: return linkBase + "stderr";
            case BOT_1_OUTPUT: return linkBase + "bot_1_output";
            case BOT_2_OUTPUT: return linkBase + "bot_2_output";
            default: return ""; // Should not happen
        }
    }

    // IMPORTANT: This controller mapping is usually excluded from the Spring security
    // filter chain because we need to set our custom response headers (e.g. compression).
    // As a result, there won't be any user security context available in this method!
    // Normally, we don't need any security context in this method, as we only send the
    // turn state bytes, which do not contain any sensitive information whatsoever.
    @GetMapping("/{matchId}/data")
    public void matchData(@PathVariable String matchId,
                          @RequestHeader(name = HttpHeaders.ACCEPT_ENCODING, required = false) String acceptEncoding,
                          HttpServletResponse response) throws IOException {
        boolean acceptsGzipEncoding = acceptsGzipEncoding(acceptEncoding);
        String matchTurnFilePath = matchFileService.createMatchTurnDataFilePath(matchId);

        Optional<InputStream> data = acceptsGzipEncoding
            ? serializer.deserializeCompressedFromFile(matchTurnFilePath)
            : serializer.deserializeUncompressedFromFile(matchTurnFilePath);

        if (data.isPresent()) {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

            if (acceptsGzipEncoding) {
                response.setHeader(HttpHeaders.CONTENT_ENCODING, GZIP);
            }

            // The turn data of a match will never change, so it can be cached "forever" by the browser
            addCacheForeverHeader(response);

            IOUtils.copy(data.get(), response.getOutputStream());
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private boolean acceptsGzipEncoding(String acceptEncoding) {
        return StringUtils.hasLength(acceptEncoding) && acceptEncoding.toLowerCase().contains(GZIP);
    }

    private void addCacheForeverHeader(HttpServletResponse response) {
        response.addHeader(HttpHeaders.CACHE_CONTROL, "max-age=31556926");
    }

    // TODO ensure that full caching for the methods below is enabled

    @GetMapping("/{matchId}/stdout")
    @ResponseBody
    public String matchStdOut(@PathVariable String matchId, @AuthenticationPrincipal User user) {
        return getMatchFileTextContent(matchId, user, MatchFile.STDOUT);
    }

    @GetMapping("/{matchId}/stderr")
    @ResponseBody
    public String matchStdErr(@PathVariable String matchId, @AuthenticationPrincipal User user) {
        return getMatchFileTextContent(matchId, user, MatchFile.STDERR);
    }

    @GetMapping("/{matchId}/bot_1_output")
    @ResponseBody
    public String matchBot1Output(@PathVariable String matchId, @AuthenticationPrincipal User user) {
        return getMatchFileTextContent(matchId, user, MatchFile.BOT_1_OUTPUT);
    }

    @GetMapping("/{matchId}/bot_2_output")
    @ResponseBody
    public String matchBot2Output(@PathVariable String matchId, @AuthenticationPrincipal User user) {
        return getMatchFileTextContent(matchId, user, MatchFile.BOT_2_OUTPUT);
    }

    private String getMatchFileTextContent(String matchId, User user, MatchFile matchFile) {
        return matchService.loadMatch(matchId)
            .map(match -> validateUserAccessToMatchFile(match, user, matchFile))
            .flatMap(match -> matchFileService.getMatchFileTextContent(match.getId(), matchFile))
            .orElseThrow(NotFoundException::new);
    }

    private Match validateUserAccessToMatchFile(Match match, User user, MatchFile matchFile) {
        if (!matchFileService.canUserAccessMatchFile(user, match, matchFile)) {
            throw new AccessDeniedException();
        }
        return match;
    }
}
