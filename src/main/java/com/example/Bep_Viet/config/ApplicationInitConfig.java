package com.example.Bep_Viet.config;

import com.example.Bep_Viet.enums.Role;
import com.example.Bep_Viet.enums.UserStatus;
import com.example.Bep_Viet.model.User;
import com.example.Bep_Viet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {
    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            // Kiểm tra nếu CHƯA có admin thì mới tạo
            if (userRepository.findByUsername("admin").isEmpty()) {  // ✅ Sửa ở đây
                User user = User.builder()
                        .username("admin")
                        .fullName("Administrator")
                        .password(passwordEncoder.encode("123456"))
                        .email("admin@gmail.com")
                        .role(Role.ADMIN)
                        .status(UserStatus.ACTIVE) // ← thêm dòng này
                        .build();
                userRepository.save(user);
                log.warn("Admin user has been created with default username: admin, password: admin");
            } else {
                log.info("Admin user already exists");
            }
        };
    }
}
