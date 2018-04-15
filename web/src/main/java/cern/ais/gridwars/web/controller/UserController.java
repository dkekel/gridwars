package cern.ais.gridwars.web.controller;

import cern.ais.gridwars.web.controller.error.AccessDeniedException;
import cern.ais.gridwars.web.controller.error.NotFoundException;
import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.UserService;
import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public ModelAndView showSignup(@AuthenticationPrincipal User currentUser) {
        restrictAccessForSignedInNonAdminUser(currentUser);

        return ModelAndViewBuilder.forPage("/user/signup")
            .addAttribute("newUser", new User())
            .toModelAndView();
    }

    private void restrictAccessForSignedInNonAdminUser(User currentUser) {
        if ((currentUser != null) && !currentUser.isAdmin()) {
            throw new AccessDeniedException();
        }
    }

    @PostMapping("/signup")
    public ModelAndView doSignup(@ModelAttribute("newUser") @Valid User newUser, BindingResult result,
                                 RedirectAttributes redirectAttributes, @AuthenticationPrincipal User currentUser) {
        restrictAccessForSignedInNonAdminUser(currentUser);

        if (!result.hasErrors()) {
            preprocessNewUser(newUser);

            try {
                userService.create(newUser);
            } catch (UserService.UserFieldValueException ufve) {
                result.rejectValue(ufve.getFieldName(), ufve.getErrorMessageCode());
            }
        }

        if (result.hasErrors()) {
            return ModelAndViewBuilder.forPage("/user/signup")
                .addAttribute("newUser", newUser)
                .toModelAndView();
        } else {
            redirectAttributes.addFlashAttribute("created", newUser.getUsername());
            return ModelAndViewBuilder.forRedirect("/user/signin").toModelAndView();
        }
    }

    private void preprocessNewUser(User newUser) {
        newUser.setId(null);
        newUser.setUsername(trim(newUser.getUsername()));
        newUser.setTeamName(trim(newUser.getTeamName()));
        newUser.setEmail(trim(newUser.getEmail()));
        newUser.setAdmin(false);
    }

    @GetMapping("/update")
    public ModelAndView showUpdateUser(@AuthenticationPrincipal User currentUser) {
        return userService.getById(currentUser.getId())
            .map(userCopy -> ModelAndViewBuilder.forPage("/user/update")
                .addAttribute("user", userCopy)
                .toModelAndView())
            .orElseThrow(NotFoundException::new);
    }

    @PostMapping("/update")
    public ModelAndView updateUser(@ModelAttribute("user") @Valid User updatedUser, BindingResult result,
                                   RedirectAttributes redirectAttributes, @AuthenticationPrincipal User currentUser) {
        if (!result.hasErrors()) {
            updatedUser.setId(currentUser.getId());
            preprocessUpdatedUser(updatedUser);

            try {
                userService.update(updatedUser);
            } catch (UserService.UserFieldValueException ufve) {
                result.rejectValue(ufve.getFieldName(), ufve.getErrorMessageCode());
            }
        }

        if (result.hasErrors()) {
            return ModelAndViewBuilder.forPage("/user/update")
                .addAttribute("user", updatedUser)
                .toModelAndView();
        } else {
            redirectAttributes.addFlashAttribute("success", true);
            return ModelAndViewBuilder.forRedirect("/user/update").toModelAndView();
        }
    }

    private void preprocessUpdatedUser(User updatedUser) {
        updatedUser.setTeamName(trim(updatedUser.getTeamName()));
        updatedUser.setEmail(trim(updatedUser.getEmail()));
    }

    private String trim(String value) {
        return (value != null) ? value.trim() : null;
    }
}
