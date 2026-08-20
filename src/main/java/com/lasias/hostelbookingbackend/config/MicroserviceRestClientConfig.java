package com.lasias.hostelbookingbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class MicroserviceRestClientConfig {

    @Value("${booking.service.url}")
    private String bookingServiceUrl;

    @Bean
    public RestClient bookingRestClient() {
        return RestClient.builder()
                .baseUrl("bookingServiceUrl")
                .defaultHeader("X-Internal-Call", "true")
                .build();
    }
}
