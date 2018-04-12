package cern.ais.gridwars.web.controller;

import cern.ais.gridwars.runtime.MatchRuntimeConstants;
import cern.ais.gridwars.runtime.MatchTurnStateSerializer;
import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.domain.Match;
import cern.ais.gridwars.web.service.MatchService;
import cern.ais.gridwars.web.util.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;


@Controller
@RequestMapping("/match")
public class MatchController {

    private final MatchTurnStateSerializer serializer = new MatchTurnStateSerializer();
    private final MatchService matchService;
    private final GridWarsProperties gridWarsProperties;

    public MatchController(MatchService matchService, GridWarsProperties gridWarsProperties) {
        this.matchService = Objects.requireNonNull(matchService);
        this.gridWarsProperties = Objects.requireNonNull(gridWarsProperties);
    }

    @GetMapping("/{matchId}")
    public ModelAndView show(@PathVariable String matchId) {
        Match match = matchService.loadMatch(matchId);

        ModelAndView mav = new ModelAndView("pages/match/show");
        mav.addObject("matchDataUrl", "/match/data/" + matchId);
        mav.addObject("turnCount", match.getTurnCount());
        return mav;
    }

    @GetMapping("/data/{matchId}")
    public void data(@PathVariable String matchId, HttpServletResponse response) throws IOException {
        final String matchTurnFile = FileUtils.joinFilePathsToSinglePath(
            gridWarsProperties.getDirectories().getMatchesDir(),
            matchId,
            MatchRuntimeConstants.MATCH_TURNS_PAYLOAD_FILE_NAME
        );

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        IOUtils.copy(serializer.deserializeFromFile(matchTurnFile), response.getOutputStream());
    }
}
