package cern.ais.gridwars.web.controller.admin;


import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.UserService;
import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.internet.ContentType;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class UserAdminController {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final UserService userService;

    public UserAdminController(UserService userService) {
        this.userService = Objects.requireNonNull(userService);
    }

    @GetMapping("/users")
    public ModelAndView listUsers() {
        return ModelAndViewBuilder.forPage("admin/users")
            .addAttribute("users", getAllUsers())
            .toModelAndView();
    }

    private List<User> getAllUsers() {
        return userService.getAllNonAdminUsers().stream()
            .sorted((user1, user2) -> user1.getUsername().compareToIgnoreCase(user2.getUsername()))
            .collect(Collectors.toList());
    }

    @PostMapping(path = "/users", params = { "action=changePassword" })
    public ModelAndView changePassword(@RequestParam String userId, @RequestParam String newPassword,
                                       RedirectAttributes redirectAttributes) {
        try {
            userService.changeUserPassword(userId, newPassword);
            redirectAttributes.addFlashAttribute("success", "Password successfully changed");
        } catch (Exception e) {
            LOG.error("Error when trying to change password of user {}: {}", userId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return ModelAndViewBuilder.forRedirect("/admin/users").toModelAndView();
    }

    @GetMapping("/users/export")
    public ResponseEntity<String> downloadEgroupsImportFile() {
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"gridwars-egroups-import.csv\"")
            .body(createEgroupsImportFileContent());
    }

    private String createEgroupsImportFileContent() {
        return getAllUsers().stream()
            .filter(this::isNoTestOrCernUser)
            .map(this::createEgroupsImportFileLine)
            .collect(Collectors.joining("\n"));
    }

    private boolean isNoTestOrCernUser(User user) {
        return !user.isAdmin() &&
            !user.getUsername().toLowerCase().startsWith("test") &&
            !user.getEmail().toLowerCase().contains("@cern.ch");
    }

    private String createEgroupsImportFileLine(User user) {
        return "E,," + user.getEmail();
    }
}
