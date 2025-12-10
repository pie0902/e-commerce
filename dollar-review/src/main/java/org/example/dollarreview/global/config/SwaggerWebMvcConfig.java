package org.example.dollarreview.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SwaggerWebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Swagger UI 정적 리소스 매핑 (일부 환경에서 @EnableWebMvc로 기본 매핑 비활성화되는 경우 대비)
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations(
                    "classpath:/META-INF/resources/webjars/swagger-ui/",
                    "classpath:/static/swagger-ui/"
                );

        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
    }
}
