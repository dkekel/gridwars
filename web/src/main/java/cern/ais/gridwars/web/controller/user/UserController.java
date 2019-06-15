package cern.ais.gridwars.web.controller.user;

import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.config.oauth.OAuthCookieAuthenticationFilter;
import cern.ais.gridwars.web.controller.error.NotFoundException;
import cern.ais.gridwars.web.controller.error.ValidationError;
import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.UserService;
import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final GridWarsProperties gridWarsProperties;

    @Autowired
    public UserController(UserService userService, GridWarsProperties gridWarsProperties) {
        this.userService = Objects.requireNonNull(userService);
        this.gridWarsProperties = Objects.requireNonNull(gridWarsProperties);
    }

    // IMPORTANT: Only map GET method here, not POST, to let the login filter do its work.
    @GetMapping("/login")
    public RedirectView userLogin(final Model model) {
        RedirectView redirectView = new RedirectView(gridWarsProperties.getOAuth().getAuthorizeUrl());
        redirectView.setContextRelative(false);
        model.addAttribute("grant_type", gridWarsProperties.getOAuth().getGrantType());
        model.addAttribute("response_type", gridWarsProperties.getOAuth().getResponseType());
        model.addAttribute("client_id", gridWarsProperties.getOAuth().getClientId());
        //TODO provide better state
        model.addAttribute("state", "NEW_STATE");
        return redirectView;
    }

    @GetMapping("/login/client-app")
    public RedirectView userOAuthToken(final String code, final String state, final HttpServletResponse response) {
        //TODO redirect to the referer
        RedirectView redirectView = new RedirectView("http://localhost:8081/");
        OAuthToken token = userService.getUserOAuthToken(code);
        if (!userService.isExistingUser(token.getUsername())) {
            NewUserDto newUser = new NewUserDto();
            newUser.setUsername(token.getUsername());
            //TODO Send mail to admin
            userService.create(newUser, false, true, false);
        }
        String jwtToken = getJwtToken(token);
        setOAuthCookie(token, jwtToken, response);
        return redirectView;
    }

    private String getJwtToken(final OAuthToken token) {
        Calendar calendar = Calendar.getInstance();
        Date issueDate = calendar.getTime();
        calendar.add(Calendar.SECOND, token.getExpiresIn().intValue());
        Date expirationDate = calendar.getTime();
        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(gridWarsProperties.getJwt().getSecret());
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
        return Jwts.builder()
            .claim(OAuthCookieAuthenticationFilter.OAUTH_COOKIE_NAME, token.getAccessToken())
            .claim("username", token.getUsername())
            .setIssuedAt(issueDate)
            .setExpiration(expirationDate).signWith(signatureAlgorithm, signingKey).compact();
    }

    private void setOAuthCookie(final OAuthToken token, final String jwtToken, final HttpServletResponse response) {
        Cookie oauthCookie = new Cookie(OAuthCookieAuthenticationFilter.OAUTH_COOKIE_NAME, jwtToken);
        oauthCookie.setPath("/");
        oauthCookie.setDomain("localhost");
        oauthCookie.setMaxAge(token.getExpiresIn().intValue());
        oauthCookie.setHttpOnly(true);
        response.addCookie(oauthCookie);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout() {
        userService.destroyAuthenticationToken();
    }

    @GetMapping(value = "/getUsername")
    @ResponseBody
    public String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @GetMapping("/confirm/{confirmationId}")
    public ModelAndView confirmUser(@PathVariable String confirmationId, RedirectAttributes redirectAttributes) {
        return userService.getByConfirmationId(confirmationId)
            .filter(user -> !user.isConfirmed())
            .map(user -> {
                userService.confirmUser(user.getId());
                redirectAttributes.addFlashAttribute("confirmed", user.getUsername());
                return ModelAndViewBuilder.forRedirect("/user/signin").toModelAndView();
            })
            .orElseThrow(NotFoundException::new);
    }

    @GetMapping("/update")
    public ModelAndView showUpdateUser(@AuthenticationPrincipal User currentUser) {
        return userService.getById(currentUser.getId())
            .map(this::toUpdateUserDto)
            .map(updateUserDto ->
                ModelAndViewBuilder.forPage("user/update")
                    .addAttribute("user", updateUserDto)
                    .toModelAndView())
            .orElseThrow(NotFoundException::new);
    }

    private UpdateUserDto toUpdateUserDto(User user) {
        return new UpdateUserDto()
            .setUsername(user.getUsername())
            .setTeamName(user.getTeamName())
            .setEmail(user.getEmail());
    }

    @PostMapping("/update")
    @ResponseStatus(value = HttpStatus.OK)
    public void updateUser(@ModelAttribute("user") @Valid UpdateUserDto updateUserDto, BindingResult result,
                           @AuthenticationPrincipal User currentUser) {
        if (!result.hasErrors()) {
            preprocessUpdatedUser(updateUserDto, currentUser);

            try {
                userService.update(updateUserDto);
            } catch (UserService.UserFieldValueException ufve) {
                result.rejectValue(ufve.getFieldName(), ufve.getErrorMessageCode());
            }
        } else {
            //TODO return actual errors to the client
            //Avoid returning 200 status in case of validation errors
            throw new ValidationError();
        }
    }

    private void preprocessUpdatedUser(UpdateUserDto updateUserDto, User currentUser) {
        updateUserDto.setId(currentUser.getId());
        updateUserDto.setEmail(trim(updateUserDto.getEmail()));
    }

    private String trim(String value) {
        return (value != null) ? value.trim() : null;
    }
}
