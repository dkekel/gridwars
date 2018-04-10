package cern.ais.gridwars.web.controller;

import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.BotService;
import cern.ais.gridwars.web.service.BotUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.util.Objects;


@Controller
@RequestMapping("/bot")
public class BotController {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final BotUploadService botUploadService;

    @Autowired
    public BotController(BotUploadService botUploadService) {
        this.botUploadService = Objects.requireNonNull(botUploadService);
    }

    @GetMapping("/upload")
    public String showUpload() {
        return "pages/bot/upload";
    }

    @PostMapping("/upload")
    public String doUpload(@RequestParam("botJarFile") MultipartFile botJarFile, RedirectAttributes redirectAttributes,
                           @AuthenticationPrincipal User user) {
        LOG.debug("Received file - name: {}, original name: {}, content type: {}, size: {}, upload user: {}",
                botJarFile.getName(), botJarFile.getOriginalFilename(), botJarFile.getContentType(),
                botJarFile.getSize(), user.getUsername());

        try {
            botUploadService.uploadNewBot(botJarFile, user, Instant.now());
            redirectAttributes.addFlashAttribute("success", true);
        } catch (BotService.BotException bue) {
            redirectAttributes.addFlashAttribute("error", bue.getMessage());
        }

        return "redirect:/bot/upload";
    }
}
