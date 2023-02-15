package com.naver.idealproduction.song.servlet;

import com.naver.idealproduction.song.SimOverlayNG;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpringConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        var path = SimOverlayNG.getDirectory().resolve("overlay");
        var location = path.toUri().toString();

        registry.addResourceHandler("/overlay/**")
                .addResourceLocations(location)
                .setCachePeriod(0);

    }
}
