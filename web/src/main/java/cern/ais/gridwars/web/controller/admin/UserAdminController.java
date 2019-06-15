package cern.ais.gridwars.web.controller.admin;


import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.UserService;
import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Controller
public class UserAdminController extends BaseAdminController {

    private final UserService userService;

    public UserAdminController(UserService userService) {
        this.userService = Objects.requireNonNull(userService);
    }

    @GetMapping("users")
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

    @GetMapping("users/export")
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
