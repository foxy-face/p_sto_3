package com.javamentor.qa.platform.webapp.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**/*")
                .addResourceLocations("/", "/resources/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("/html/login.html");
        registry.addViewController("/question/add").setViewName("/html/question/add.html");
        registry.addViewController("/questions").setViewName("/html/question/questions.html");
        registry.addViewController("/user/profile").setViewName("/html/user/profile.html");
        registry.addViewController("/main").setViewName("/html/main.html");
        registry.addViewController("/users").setViewName("/html/users.html");
        registry.addViewController("/tags").setViewName("/html/tag/tags.html");
        registry.addViewController("/users/example").setViewName("/html/example/usersExample.html");
        registry.addViewController("/question/example").setViewName("/html/example/questionExample.html");
        WebMvcConfigurer.super.addViewControllers(registry);
    }
}

