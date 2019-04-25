package cern.ais.gridwars.web.config.oauth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

public class OAuthCookieAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public static final String OAUTH_COOKIE_NAME = "OAUTH_TOKEN";

    public OAuthCookieAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request,
                                                final HttpServletResponse response) throws AuthenticationException {
        Cookie oAuthCookie = Arrays.stream(request.getCookies())
            .filter(cookie -> OAUTH_COOKIE_NAME.equals(cookie.getName())).findFirst().orElse(null);
        return new OAuthAuthentication(oAuthCookie, "");
    }
}
