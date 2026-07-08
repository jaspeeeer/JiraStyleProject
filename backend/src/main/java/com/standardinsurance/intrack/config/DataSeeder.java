package com.standardinsurance.intrack.config;

import com.standardinsurance.intrack.project.ProjectEntity;
import com.standardinsurance.intrack.project.ProjectRepository;
import com.standardinsurance.intrack.user.Role;
import com.standardinsurance.intrack.user.UserEntity;
import com.standardinsurance.intrack.user.UserRepository;
import com.standardinsurance.intrack.user.UserStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a little sample data for local development so the app is usable immediately.
 * Active only under the {@code local} profile and only when the DB is empty.
 * Seeded accounts share the dev password {@code password}.
 */
@Component
@Profile("local")
public class DataSeeder implements CommandLineRunner {

    private static final String DEV_PASSWORD = "password";

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      ProjectRepository projectRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (projectRepository.count() > 0) {
            return;
        }

        UserEntity admin = newUser("Ada Admin", "admin@intrack.local", Role.ADMIN);
        UserEntity dev = newUser("Dev Devinson", "dev@intrack.local", Role.DEVELOPER);
        userRepository.save(admin);
        userRepository.save(dev);

        ProjectEntity platform = newProject("PROJ", "Platform");
        platform.getMembers().add(admin);
        platform.getMembers().add(dev);

        ProjectEntity mobile = newProject("MOB", "Mobile App");
        mobile.getMembers().add(dev);

        projectRepository.save(platform);
        projectRepository.save(mobile);
    }

    private UserEntity newUser(String name, String email, Role role) {
        UserEntity user = new UserEntity();
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash(passwordEncoder.encode(DEV_PASSWORD));
        return user;
    }

    private ProjectEntity newProject(String key, String name) {
        ProjectEntity project = new ProjectEntity();
        project.setProjectKey(key);
        project.setName(name);
        return project;
    }
}
