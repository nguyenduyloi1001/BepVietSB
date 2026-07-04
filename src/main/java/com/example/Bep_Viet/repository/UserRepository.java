package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.enums.Role;
import com.example.Bep_Viet.enums.UserStatus;
import com.example.Bep_Viet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    boolean existsByEmail(String email);

    List<User> findByStatus(UserStatus status);

    List<User> findByRole(Role role);

    List<User> findByFullNameContainingIgnoreCase(String fullName);

    List<User> findByStatusAndRole(UserStatus status, Role role);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

}