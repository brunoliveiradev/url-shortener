package com.github.vivyteam.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server localhostServer = new Server()
                .url("http://localhost:9000")
                .description("Local server");

        Server devServer = new Server()
                .url("https://dev-myservicedomain.de/")
                .description("Development server");

        Server stagingServer = new Server()
                .url("https://hml-myservicedomain.de/")
                .description("Staging server");

        Server productionServer = new Server()
                .url("https://myservicedomain.de/")
                .description("Production server");

        return new OpenAPI()
                .addServersItem(localhostServer)
                .addServersItem(devServer)
                .addServersItem(stagingServer)
                .addServersItem(productionServer)
                .info(new Info().title("URL Shortening Service API")
                        .version("1.0")
                        .description("API for shortening and retrieving URLs")
                        .contact(new Contact().name("Bruno H M Oliveira").url("https://github.com/brunoliveiradev")))
                .components(new Components());
    }
}
