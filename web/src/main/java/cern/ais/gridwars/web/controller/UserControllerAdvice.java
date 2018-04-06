package cern.ais.gridwars.web.controller;

import cern.ais.gridwars.web.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;


/**
 * Provides the current user as context variable to all controller methods,
 * which usually serve Thymeleaf templates.
 */
@ControllerAdvice("cern.ais.gridwars.web.controller")
public class UserControllerAdvice {

    @ModelAttribute("user")
    public User getCurrentUser(Authentication authentication) {
        return Optional.ofNullable(authentication)
                .map(Authentication::getPrincipal)
                .filter(User.class::isInstance)
                .map(User.class::cast)
                .orElse(null);
    }
}
