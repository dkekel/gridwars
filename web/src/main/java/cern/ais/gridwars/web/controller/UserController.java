package cern.ais.gridwars.web.controller;

import cern.ais.gridwars.web.controller.error.AccessDeniedException;
import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.UserService;
import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.Objects;


@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = Objects.requireNonNull(userService);
    }

    // IMPORTANT: Only map GET method here, not POST, to let the login filter do its work.
    @GetMapping("/signin")
    public ModelAndView showSignin() {
        return ModelAndViewBuilder.forPage("/user/signin").toModelAndView();
    }

    @GetMapping("/signup")
    public ModelAndView showSignup(@AuthenticationPrincipal User user) {
        // Don't allow signed in non-admin users to use the sign up form again.
        if ((user != null) && !user.isAdmin()) {
            throw new AccessDeniedException();
        }

        return ModelAndViewBuilder.forPage("/user/signup")
            .addAttribute("newUser", new User())
            .toModelAndView();
    }

    @PostMapping("/signup")
    public ModelAndView doSignup(@ModelAttribute("newUser") @Valid User newUser, BindingResult result, Errors errors) {
        if (!errors.hasErrors()) {
            preprocessNewUser(newUser);

            try {
                userService.create(newUser);
            } catch (UserService.UserFieldValueException ufve) {
                result.rejectValue(ufve.getFieldName(), ufve.getErrorMessageCode());
            }
        }

        if (errors.hasErrors()) {
            return ModelAndViewBuilder.forPage("/user/signup")
                .addAttribute("newUser", newUser)
                .toModelAndView();
        } else {
            // TODO use RedirectAttributes here instead of a URL parameter?
            return ModelAndViewBuilder.forRedirect("/user/signin?created=" + newUser.getUsername()).toModelAndView();
        }
    }

    private void preprocessNewUser(User newUser) {
        newUser.setId(null);
        newUser.setUsername(trim(newUser.getUsername()));
        newUser.setTeamName(trim(newUser.getTeamName()));
        newUser.setEmail(trim(newUser.getEmail()));
        newUser.setAdmin(false);
    }

    private String trim(String value) {
        return (value != null) ? value.trim() : null;
    }
}
