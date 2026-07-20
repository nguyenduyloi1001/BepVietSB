package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.model.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    List<UserPreference> findByUserId(Long userId);
    Optional<UserPreference> findByUserIdAndPreferenceKey(Long userId, String preferenceKey);
}