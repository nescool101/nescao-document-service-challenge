package com.clara.ops.challenge.document_management_service_challenge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Document Management Service API")
                .description(
                    "API for managing documents with upload, download, search, and delete"
                        + " operations")
                .version("1.0.0")
                .contact(new Contact().name("Clara Ops").email("support@claraops.com")));
  }
}
