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

import javax.annotation.PostConstruct;


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
            .antMatchers("/admin/**").hasRole("ADMIN")
            .antMatchers("/**").permitAll()
            .and()
                .formLogin().loginPage("/signin").permitAll()
            .and()
                .logout().logoutSuccessUrl("/").permitAll();
        // @formatter:on

        http.headers()
            // Allow using iFrames from the same origin to support H2 console
            .frameOptions().sameOrigin();

        // TODO disable csrf for now, as it breaks i.e. H2 console
        http.csrf().disable();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // Disable security chain for static resources
        web.ignoring().antMatchers("/static/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
    }

    // TODO Remove when database is set up
    @PostConstruct
    public void initTestUsers() {
        userService.create(
            new User()
                .setUsername("user1")
                .setPassword("bla1")
                .setEmail("user1@cern.ch")
                .setTeamname("Team User1"));

        userService.create(
            new User()
                .setUsername("user2")
                .setPassword("bla2")
                .setEmail("user2@cern.ch")
                .setTeamname("Team User2"));

        userService.create(
            new User()
                .setUsername("user3")
                .setPassword("bla3")
                .setEmail("user3@cern.ch")
                .setTeamname("Team User3")
                .setEnabled(false));

//        userService.create(
//            new User()
//                .setUsername("user4")
//                .setPassword("bla4")
//                .setEmail("user4@cern.ch")
//                .setTeamname("Team User4"));

        userService.create(
            new User()
                .setUsername("admin")
                .setPassword("blabla")
                .setEmail("admin@cern.ch")
                .setTeamname("Team Admin")
                .setAdmin(true));
    }
}
