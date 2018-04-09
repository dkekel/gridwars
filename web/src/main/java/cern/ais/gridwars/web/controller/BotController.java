package cern.ais.gridwars.web.controller;

import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.BotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Objects;


@Controller
@RequestMapping("/bot")
public class BotController {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final BotService botService;

    @Autowired
    public BotController(BotService botService) {
        this.botService = Objects.requireNonNull(botService);
    }

    @GetMapping("/upload")
    public String showUpload() {
        return "pages/bot/upload";
    }

    @PostMapping("/upload")
    public String doUpload(@RequestParam("botJarFile") MultipartFile botJarFile, Model model,
                           @AuthenticationPrincipal User user) {
        LOG.debug("Received file - name: {}, original name: {}, content type: {}, size: {}, upload user: {}",
                botJarFile.getName(), botJarFile.getOriginalFilename(), botJarFile.getContentType(),
                botJarFile.getSize(), user.getUsername());

        try {
            botService.validateAndCreateNewBot(botJarFile, user, Instant.now());
            return "redirect:/bot/upload?success";
        } catch (BotService.BotUploadException bue) {
            model.addAttribute("error", bue.getMessage());
        }

        return "pages/bot/upload";
    }
}
