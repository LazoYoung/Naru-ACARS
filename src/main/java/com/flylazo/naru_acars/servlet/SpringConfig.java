package com.flylazo.naru_acars.servlet;

import com.flylazo.naru_acars.NaruACARS;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpringConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        var path = NaruACARS.getDirectory().resolve("overlay");
        var location = path.toUri().toString();

        registry.addResourceHandler("/overlay/**")
                .addResourceLocations(location)
                .setCachePeriod(0);

    }
}
