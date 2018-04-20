package cern.ais.gridwars.web.config;

import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


/**
 * Configures an additional HTTP connector to enforces all requests to require HTTPS and to redirect all
 * HTTP requests automatically to HTTPS.
 */
@Configuration
@Profile("prod")
public class TomcatConnectorConfiguration {

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(createHttpToHttpsRedirectConnector());
        tomcat.addContextCustomizers(createRequiresSecureChannelContentCustomizer());
        return tomcat;
    }

    private Connector createHttpToHttpsRedirectConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        // We set the default HTTPS redirect port here instead that of the actual HTTPS connector port 8443.
        // When deployed to prod, the server will run behind 80<->8080 and 443<->8443 port forwarding rules,
        // so if we would use 8443 as port here, the server would respond with a redirect location header
        // containing port 8443, but it should be 443 for outside. This won't work locally when not sitting
        // behind nat port forwarding rules, but the prod environment should only be used when running as
        // standalone jar on the gridwars machine anyway.
        connector.setRedirectPort(443);
        return connector;
    }

    private TomcatContextCustomizer createRequiresSecureChannelContentCustomizer() {
        return context -> {
            SecurityConstraint securityConstraint = new SecurityConstraint();
            securityConstraint.setUserConstraint("CONFIDENTIAL");
            SecurityCollection collection = new SecurityCollection();
            collection.addPattern("/*");
            securityConstraint.addCollection(collection);
            context.addConstraint(securityConstraint);
        };
    }
}
