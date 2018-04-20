package cern.ais.gridwars.web.controller;

import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.controller.error.AccessDeniedException;
import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.BotFileService;
import cern.ais.gridwars.web.service.BotService;
import cern.ais.gridwars.web.service.BotUploadService;
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

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


@Controller
@RequestMapping("/bot")
public class BotController {

    private static final DateTimeFormatter INSTANT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final BotService botService;
    private final BotUploadService botUploadService;
    private final BotFileService botFileService;
    private final GridWarsProperties gridWarsProperties;

    @Autowired
    public BotController(BotService botService, BotUploadService botUploadService, BotFileService botFileService,
                         GridWarsProperties gridWarsProperties) {
        this.botService = Objects.requireNonNull(botService);
        this.botUploadService = Objects.requireNonNull(botUploadService);
        this.botFileService = Objects.requireNonNull(botFileService);
        this.gridWarsProperties = Objects.requireNonNull(gridWarsProperties);
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
    public ModelAndView upload(@RequestParam MultipartFile botJarFile, RedirectAttributes redirectAttributes,
                               @AuthenticationPrincipal User currentUser, HttpServletRequest request) {
        LOG.info("Received bot jar upload - name: {}, original name: {}, content type: {}, size: {}, " +
                "upload user: {}, ip: {}",
                botJarFile.getName(), botJarFile.getOriginalFilename(), botJarFile.getContentType(),
                botJarFile.getSize(), currentUser.getUsername(), request.getRemoteAddr());

        if (botUploadDisabled() && !currentUser.isAdmin()) {
            throw new AccessDeniedException();
        }

        try {
            Bot newBot = botUploadService.uploadNewBot(botJarFile, currentUser, Instant.now(), request.getRemoteAddr());
            redirectAttributes.addFlashAttribute("success", newBot.getName());
        } catch (BotService.BotException bue) {
            redirectAttributes.addFlashAttribute("error", bue.getMessage());
        }

        return ModelAndViewBuilder.forRedirect("/team").toModelAndView();
    }

    private boolean botUploadDisabled() {
        return !gridWarsProperties.getMatches().getBotUploadEnabled();
    }
}
