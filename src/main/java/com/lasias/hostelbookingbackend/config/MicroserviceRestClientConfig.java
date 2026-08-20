package com.lasias.hostelbookingbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class MicroserviceRestClientConfig {

    @Bean
    public RestClient bookingRestClient() {
        return RestClient.builder()
                .baseUrl("http://booking:8083")
                .defaultHeader("X-Internal-Call", "true")
                .build();
    }
}
