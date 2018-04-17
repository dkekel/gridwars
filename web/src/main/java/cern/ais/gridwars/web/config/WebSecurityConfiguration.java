package cern.ais.gridwars.web.config;

import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.authorizeRequests()
            .antMatchers("/admin/**").hasRole(User.ADMIN)
            .antMatchers("/match/**").fullyAuthenticated()
            .antMatchers("/team/**").fullyAuthenticated()
            .antMatchers("/bot/**").fullyAuthenticated()
            .antMatchers("/user/update").fullyAuthenticated()
            .antMatchers("/user/confirm/**").permitAll()
            .antMatchers("/**").permitAll()
            .and()
                .formLogin().loginPage("/user/signin").permitAll()
            .and()
                .logout().logoutUrl("/user/signout").logoutSuccessUrl("/").permitAll();
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
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
    }
}
