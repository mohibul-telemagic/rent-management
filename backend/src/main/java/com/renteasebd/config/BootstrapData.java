package com.renteasebd.config;

import com.renteasebd.domain.user.User;
import com.renteasebd.domain.user.UserRole;
import com.renteasebd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ConditionalOnProperty(prefix = "app.bootstrap", name = "enabled", havingValue = "true")
public class BootstrapData {

    @Bean
    CommandLineRunner adminBootstrap(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        @Value("${app.bootstrap.admin-email:owner@rentease.bd}") String adminEmail,
        @Value("${app.bootstrap.admin-password:Owner@123}") String adminPassword
    ) {
        return args -> {
            if (userRepository.findByEmailIgnoreCase(adminEmail).isPresent()) {
                return;
            }
            User user = new User();
            user.setFullName("Owner");
            user.setEmail(adminEmail);
            user.setPasswordHash(passwordEncoder.encode(adminPassword));
            user.setRole(UserRole.OWNER);
            user.setPreferredLanguage("en");
            user.setActive(true);
            userRepository.save(user);
        };
    }
}
