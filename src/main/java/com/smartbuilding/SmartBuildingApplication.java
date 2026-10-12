package com.smartbuilding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SmartBuildingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartBuildingApplication.class, args);
    }

    // Makes ONE shared PasswordEncoder available everywhere (AuthService, DataInitializer, ...).
    // @Bean = "Spring, please create this object once and hand it to whoever asks for it."
    // BCrypt = one-way password hashing. You can check a password, but never un-hash it.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}