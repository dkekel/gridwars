package cern.ais.gridwars.web.controller.admin;

import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;


@Controller
public class SettingsAdminController extends BaseAdminController {

    private final GridWarsProperties gridWarsProperties;

    public SettingsAdminController(GridWarsProperties gridWarsProperties) {
        this.gridWarsProperties = Objects.requireNonNull(gridWarsProperties);
    }

    @GetMapping("settings")
    public ModelAndView showSettings() {
        return ModelAndViewBuilder.forPage("admin/settings")
            .addAttribute("settings", createSettingsDto())
            .toModelAndView();
    }

    private SettingsDto createSettingsDto() {
        return new SettingsDto()
            .setUserRegistrationEnabled(gridWarsProperties.getRegistration().getEnabled())
            .setBotUploadEnabled(gridWarsProperties.getMatches().getBotUploadEnabled())
            .setMatchesPerDuel(gridWarsProperties.getMatches().getMatchCountPerOpponent());
    }

    @PostMapping("settings")
    public ModelAndView updateSettings(@ModelAttribute("settings") SettingsDto settingsDto,
                                       RedirectAttributes redirectAttributes) {
        gridWarsProperties.getRegistration().setEnabled(settingsDto.userRegistrationEnabled);
        gridWarsProperties.getMatches().setBotUploadEnabled(settingsDto.botUploadEnabled);
        gridWarsProperties.getMatches().setMatchCountPerOpponent(Math.max(1, Math.min(9, settingsDto.matchesPerDuel)));

        redirectAttributes.addFlashAttribute("success", "Settings updated");
        return ModelAndViewBuilder.forRedirect("/admin/settings").toModelAndView();
    }

    public static final class SettingsDto {
        private boolean userRegistrationEnabled;
        private boolean botUploadEnabled;
        private int matchesPerDuel;

        public boolean isUserRegistrationEnabled() {
            return userRegistrationEnabled;
        }

        public SettingsDto setUserRegistrationEnabled(boolean userRegistrationEnabled) {
            this.userRegistrationEnabled = userRegistrationEnabled;
            return this;
        }

        public boolean isBotUploadEnabled() {
            return botUploadEnabled;
        }

        public SettingsDto setBotUploadEnabled(boolean botUploadEnabled) {
            this.botUploadEnabled = botUploadEnabled;
            return this;
        }

        public int getMatchesPerDuel() {
            return matchesPerDuel;
        }

        public SettingsDto setMatchesPerDuel(int matchesPerDuel) {
            this.matchesPerDuel = matchesPerDuel;
            return this;
        }
    }
}
