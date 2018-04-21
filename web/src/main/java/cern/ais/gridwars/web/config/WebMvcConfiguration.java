package cern.ais.gridwars.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;


@Configuration
@EnableWebMvc
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Caching the resources for 1 day might not allow to see the students changes that we
        // do to static resources. Would that be a problem? We don't really modify any static
        // resources and if we use Bootstrap classes and styles inside the Thymeleaf templates
        // then we should be golden. The content of Thymeleaf pages is dynamic that therefore
        // never cached anyway, this here is only about static css, images, js, etc.
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")
            .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic());

        // Disable the cache, as the files (gridwars starter package) might change and then
        // the new files should be immediately available.
        registry.addResourceHandler("/files/**")
            .addResourceLocations("classpath:/files/")
            .setCacheControl(CacheControl.noCache())
            .setCachePeriod(0);
    }
}
