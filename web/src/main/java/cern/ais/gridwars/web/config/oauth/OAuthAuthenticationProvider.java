package cern.ais.gridwars.web.config.oauth;

import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.client.RestClientException;

import java.util.Calendar;
import java.util.Date;

@Configuration
public class OAuthAuthenticationProvider implements AuthenticationProvider {

    private static final long MILLISECONDS = 1000L;

    private final transient UserService userService;

    public OAuthAuthenticationProvider(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String oAuthToken = (String) authentication.getCredentials();
        try {
            OAuthorizedToken authorizedToken = userService.validateUserToken(oAuthToken);
            Date expirationDate = new Date(authorizedToken.getExpirationTimestamp() * MILLISECONDS);
            boolean isValid = Calendar.getInstance().getTime().before(expirationDate);
            authentication.setAuthenticated(isValid);
            if (OAuthAuthentication.class.isAssignableFrom(authentication.getClass())) {
                User currentUser = userService.getById(authorizedToken.getUsername()).orElse(null);
                ((OAuthAuthentication) authentication).setUser(currentUser);
            }
        } catch (RestClientException e) {
            throw new BadCredentialsException(e.getLocalizedMessage(), e);
        }
        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuthAuthentication.class.isAssignableFrom(authentication);
    }
}
