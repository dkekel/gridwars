package cern.ais.gridwars.web.controller;

import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.Match;
import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.BotFileService;
import cern.ais.gridwars.web.service.BotService;
import cern.ais.gridwars.web.service.BotUploadService;
import cern.ais.gridwars.web.service.MatchService;
import cern.ais.gridwars.web.util.ControllerUtils;
import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/bot")
public class BotController {

    private static final DateTimeFormatter INSTANT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final BotService botService;
    private final BotUploadService botUploadService;
    private final BotFileService botFileService;
    private final MatchService matchService;

    @Autowired
    public BotController(BotService botService, BotUploadService botUploadService, BotFileService botFileService,
                         MatchService matchService) {
        this.botService = Objects.requireNonNull(botService);
        this.botUploadService = Objects.requireNonNull(botUploadService);
        this.botFileService = Objects.requireNonNull(botFileService);
        this.matchService = Objects.requireNonNull(matchService);
    }

    @GetMapping("/show")
    public ModelAndView showActiveBot(@AuthenticationPrincipal User currentUser) {
        ModelAndViewBuilder mavBuilder = ModelAndViewBuilder.forPage("bot/show");

        botService.getActiveBotOfUser(currentUser).ifPresent(bot ->
            mavBuilder.addAttribute("activeBot", bot).addAttribute("botMatches", getBotMatches(bot))
        );

        return mavBuilder.toModelAndView();
    }

    private List<Match> getBotMatches(Bot bot) {
        return matchService.getAllStartedMatchesForBot(bot).stream()
            .sorted(Comparator.comparing(Match::getStarted).reversed())
            .collect(Collectors.toList());
    }

    @GetMapping("/download/{botId}")
    public ResponseEntity<byte[]> download(@PathVariable String botId, @AuthenticationPrincipal User currentUser) {
        return botService.getBotById(botId)
            .filter(bot -> currentUser.isAdmin() || bot.getUser().equals(currentUser))
            .map(this::createBotDownloadResponse)
            .orElseGet(ControllerUtils::createNotFoundByteDataResponse);
    }

    private ResponseEntity<byte[]> createBotDownloadResponse(Bot bot) {
        return botFileService.getBotJarFile(bot)
            .map(botJarFile -> createDownloadFileResponse(bot, botJarFile))
            .orElseGet(ControllerUtils::createNotFoundByteDataResponse);
    }

    private ResponseEntity<byte[]> createDownloadFileResponse(Bot bot, File botJarFile) {
        try {
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .cacheControl(ControllerUtils.FOREVER_CACHE_CONTROL)
                .header(HttpHeaders.CONTENT_DISPOSITION, createBotDownloadContentDispositionHeaderValue(bot))
                .body(Files.readAllBytes(botJarFile.toPath()));
        } catch (IOException e) {
            LOG.error("Failed to send bot jar file: {}", e.getMessage());
            return ControllerUtils.createNotFoundByteDataResponse();
        }
    }

    private String createBotDownloadContentDispositionHeaderValue(Bot bot) {
        return "attachment; filename=\"" + createBotDownloadFileName(bot) + "\"";
    }

    private String createBotDownloadFileName(Bot bot) {
        return bot.getName() + "_" + INSTANT_FORMATTER.format(bot.getUploadedDateTime()) + ".jar";
    }

    @PostMapping("/upload")
    public ModelAndView doUpload(@RequestParam MultipartFile botJarFile,
                                 RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal User currentUser) {
        LOG.info("Received bot jar - name: {}, original name: {}, content type: {}, size: {}, upload user: {}",
                botJarFile.getName(), botJarFile.getOriginalFilename(), botJarFile.getContentType(),
                botJarFile.getSize(), currentUser.getUsername());

        try {
            Bot newBot = botUploadService.uploadNewBot(botJarFile, currentUser, Instant.now());
            redirectAttributes.addFlashAttribute("success", newBot.getName());
        } catch (BotService.BotException bue) {
            redirectAttributes.addFlashAttribute("error", bue.getMessage());
        }

        return ModelAndViewBuilder.forRedirect("/bot/show").toModelAndView();
    }
}
