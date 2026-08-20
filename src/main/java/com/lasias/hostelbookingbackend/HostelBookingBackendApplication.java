package com.lasias.hostelbookingbackend;

import com.lasias.hostelbookingbackend.services.JwtService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class HostelBookingBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HostelBookingBackendApplication.class, args);
	}

	@Bean
    ApplicationRunner jwtTestRunner(JwtService jwtService) {
		return args -> System.out.println("JWT for development: \n"+jwtService.generateToken(2L));
	}

}
