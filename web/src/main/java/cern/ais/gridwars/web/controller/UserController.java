package cern.ais.gridwars.web.controller;

import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.Objects;


@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = Objects.requireNonNull(userService);
    }

    // IMPORTANT: Only map GET method here, not POST, to let the login filter to its work
    @GetMapping("/signin")
    public String signin() {
        return "pages/user/signin";
    }

    @GetMapping("/signup")
    public String showSignup(Model model) {
        User newUser = new User();
        model.addAttribute("newUser", newUser);
        return "pages/user/signup";
    }

    @PostMapping("/signup")
    public String doSignup(@ModelAttribute("newUser") @Valid User newUser, BindingResult result, Errors errors, Model model) {
        if (!errors.hasErrors()) {
            newUser.setUsername(trim(newUser.getUsername()));
            newUser.setTeamname(trim(newUser.getTeamname()));
            newUser.setEmail(trim(newUser.getEmail()));
            newUser.setAdmin(false);
            newUser.setEnabled(true);

            try {
                userService.create(newUser);
            } catch (UserService.FieldValueAlreadyExistsException fae) {
                result.rejectValue(fae.getFieldName(), fae.getErrorMessageCode());
            }
        }

        if (errors.hasErrors()) {
            model.addAttribute("newUser", newUser);
            return "pages/user/signup";
        } else {
            return "redirect:/signin?created=" + newUser.getUsername();
        }
    }

    private String trim(String value) {
        return (value != null) ? value.trim() : null;
    }
}
