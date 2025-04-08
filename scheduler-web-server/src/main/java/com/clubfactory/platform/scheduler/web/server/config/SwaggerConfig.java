package com.clubfactory.platform.scheduler.web.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;

/**
 * @author xiejiajun
 */
@Configuration
@Profile({"dev","test","local"})
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("scheduler-api")
                .apiInfo(new ApiInfoBuilder()
                        .title("scheduler-web-server")
                        .description("scheduler-web-server")
                        .version("1.0")
                        .build())
                .pathMapping("/")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.clubfactory.platform.scheduler.web"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public UiConfiguration uiConfiguration(){
        return UiConfigurationBuilder.builder()
                .defaultModelsExpandDepth(-1)
                .build();
    }

}
