package cern.ais.gridwars.web.config.oauth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Arrays;

public class OAuthCookieAuthenticationFilter extends OncePerRequestFilter {

    public static final String OAUTH_COOKIE_NAME = "OAUTH_TOKEN";

    private final String jwtSecret;

    private AuthenticationManager authenticationManager;

    public OAuthCookieAuthenticationFilter(final String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        final boolean debug = this.logger.isDebugEnabled();

        Cookie[] cookies = new Cookie[]{};
        if (request.getCookies() != null) {
            cookies = request.getCookies();
        }
        Cookie oAuthCookie = Arrays.stream(cookies)
            .filter(cookie -> OAUTH_COOKIE_NAME.equals(cookie.getName())).findFirst().orElse(null);

        if (oAuthCookie == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            //This line will throw an exception if it is not a signed JWS (as expected)
            Claims claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(jwtSecret))
                .parseClaimsJws(oAuthCookie.getValue()).getBody();
            String oAuthToken = claims.get(OAUTH_COOKIE_NAME, String.class);
            String username = claims.get("username", String.class);

            //TODO update expiration time

            if (authenticationIsRequired(username)) {
                OAuthAuthentication authRequest = new OAuthAuthentication(oAuthToken, username);
                Authentication authResult = this.authenticationManager.authenticate(authRequest);

                if (debug) {
                    this.logger.debug("Authentication success: " + authResult);
                }
                SecurityContextHolder.getContext().setAuthentication(authResult);
            }
        }
        catch (AuthenticationException | JwtException failed) {
            SecurityContextHolder.clearContext();
            if (debug) {
                this.logger.debug("Authentication request for failed: " + failed);
            }
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean authenticationIsRequired(String username) {
        // Only reauthenticate if username doesn't match SecurityContextHolder and user
        // isn't authenticated
        // (see SEC-53)
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        if (existingAuth == null || !existingAuth.isAuthenticated()) {
            return true;
        }

        // Limit username comparison to providers which use usernames (ie OAuthAuthentication)
        // (see SEC-348)
        if (existingAuth instanceof OAuthAuthentication && !existingAuth.getName().equals(username)) {
            return true;
        }

        // Handle unusual condition where an AnonymousAuthenticationToken is already
        // present
        // This shouldn't happen very often, as BasicProcessingFitler is meant to be
        // earlier in the filter
        // chain than AnonymousAuthenticationFilter. Nevertheless, presence of both an
        // AnonymousAuthenticationToken
        // together with a BASIC authentication request header should indicate
        // reauthentication using the
        // BASIC protocol is desirable. This behaviour is also consistent with that
        // provided by form and digest,
        // both of which force re-authentication if the respective header is detected (and
        // in doing so replace
        // any existing AnonymousAuthenticationToken). See SEC-610.
        return existingAuth instanceof AnonymousAuthenticationToken;
    }

    public void setAuthenticationManager(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
}
