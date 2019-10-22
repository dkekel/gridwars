package cern.ais.gridwars.web.config;

import cern.ais.gridwars.web.config.oauth.OAuthCookieAuthenticationFilter;
import cern.ais.gridwars.web.domain.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.filter.OncePerRequestFilter;


@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String CSRF_COOKIE_NAME = "GW-XSRF-TOKEN";
    private static final String CSRF_HEADER_NAME = "X-GW-XSRF-TOKEN";

    private final transient AuthenticationProvider authenticationProvider;
    private final transient GridWarsProperties gridWarsProperties;

    public WebSecurityConfiguration(
        @Qualifier("OAuthAuthenticationProvider") final AuthenticationProvider authenticationProvider,
        final GridWarsProperties gridWarsProperties) {
        this.authenticationProvider = authenticationProvider;
        this.gridWarsProperties = gridWarsProperties;
    }

    @Bean
    public OncePerRequestFilter getAuthenticationFilter() throws Exception {
        String jwtSecret = gridWarsProperties.getJwt().getSecret();
        OAuthCookieAuthenticationFilter authenticationFilter =
            new OAuthCookieAuthenticationFilter(jwtSecret);
        authenticationFilter.setAuthenticationManager(authenticationManagerBean());
        return authenticationFilter;
    }

    @Bean
    public CsrfTokenRepository getCsrfTokenRepository() {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookieName(CSRF_COOKIE_NAME);
        csrfTokenRepository.setHeaderName(CSRF_HEADER_NAME);
        return csrfTokenRepository;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.cors()
            .and()
            .csrf()
            .csrfTokenRepository(getCsrfTokenRepository())
            .and()
            .authorizeRequests()
            .antMatchers("/user/login", "/user/login/client-app").permitAll()
            .antMatchers("/admin/**").hasRole(User.ADMIN)
            .antMatchers("/docs/**").fullyAuthenticated()
            .antMatchers("/files/**").fullyAuthenticated()
            .antMatchers("/match/**").fullyAuthenticated()
            .antMatchers("/team/**").fullyAuthenticated()
            .antMatchers("/bot/**").fullyAuthenticated()
            .antMatchers("/user/**").fullyAuthenticated()
            .antMatchers("/user/confirm/**").permitAll()
            .antMatchers("/**").permitAll()
            .and()
                .formLogin().loginPage("/user/login").defaultSuccessUrl("/team").permitAll()
            .and()
                .logout().logoutUrl("/user/signout").logoutSuccessUrl("/").permitAll()
            .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and().addFilterBefore(getAuthenticationFilter(), BasicAuthenticationFilter.class);
        // @formatter:on

        // Allow using iFrames from the same origin for the H2 console to work
        http.headers().frameOptions().sameOrigin();

        // CSRF protection needs to be disabled for the H2 console to work
        http.csrf().ignoringAntMatchers("/admin/h2/**");
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // Disable security chain for static resources
        web.ignoring().antMatchers("/static/**", "**/favicon.ico");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }
}
