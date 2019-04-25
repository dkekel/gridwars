package cern.ais.gridwars.web.config.oauth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;

public class OAuthCookieAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public static final String OAUTH_COOKIE_NAME = "OAUTH_TOKEN";

    private final String jwtSecret;

    public OAuthCookieAuthenticationFilter(final RequestMatcher requiresAuthenticationRequestMatcher,
                                           final String jwtSecret) {
        super(requiresAuthenticationRequestMatcher);
        this.jwtSecret = jwtSecret;
    }

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request,
                                                final HttpServletResponse response) throws AuthenticationException {
        Cookie oAuthCookie = Arrays.stream(request.getCookies())
            .filter(cookie -> OAUTH_COOKIE_NAME.equals(cookie.getName())).findFirst().orElse(null);
        //This line will throw an exception if it is not a signed JWS (as expected)
        Claims claims = Jwts.parser()
            .setSigningKey(DatatypeConverter.parseBase64Binary(jwtSecret))
            .parseClaimsJws(oAuthCookie.getValue()).getBody();
        String oAuthToken = claims.get(OAUTH_COOKIE_NAME, String.class);
        String username = claims.get("username", String.class);
        return new OAuthAuthentication(oAuthToken, username);
    }
}
