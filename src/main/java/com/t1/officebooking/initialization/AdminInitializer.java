package com.t1.officebooking.initialization;

import com.t1.officebooking.model.User;
import com.t1.officebooking.model.UserRole;
import com.t1.officebooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        createDefaultAdminFromEnv();
    }

    private void createDefaultAdminFromEnv() {
        String adminEmail = System.getenv("ADMIN_EMAIL");
        String adminPassword = System.getenv("ADMIN_PASSWORD");
        String adminFullName = System.getenv("ADMIN_FULLNAME");
        String adminPosition = System.getenv("ADMIN_POSITION");

        if (adminEmail == null || adminPassword == null) {
            log.warn("ADMIN_EMAIL or ADMIN_PASSWORD not set in environment variables");
            return;
        }

        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("Admin user already exists: {}", adminEmail);
            return;
        }

        try {
            User admin = new User(
                    null,
                    adminFullName,
                    adminEmail,
                    Set.of(UserRole.ROLE_ADMIN_WORKSPACE, UserRole.ROLE_ADMIN_PROJECT, UserRole.ROLE_USER),
                    adminPosition,
                    passwordEncoder.encode(adminPassword)
            );

            userRepository.save(admin);

            log.info("Admin user created successfully: {}",
                    admin.getEmail());

        } catch (Exception e) {
            log.error("Failed to create admin user: {}", e.getMessage());
        }
    }

}
