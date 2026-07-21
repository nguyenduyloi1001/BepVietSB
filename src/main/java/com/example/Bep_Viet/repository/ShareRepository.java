package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.enums.TargetType;
import com.example.Bep_Viet.model.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ShareRepository extends JpaRepository<Share, Long> {
    boolean existsByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, TargetType targetType);
    Optional<Share> findByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, TargetType targetType);
    List<Share> findByTargetIdAndTargetType(Long targetId, TargetType targetType);
    List<Share> findByUserId(Long userId);
    long countByTargetIdAndTargetType(Long targetId, TargetType targetType);
    void deleteByTargetIdAndTargetType(Long targetId, TargetType targetType);
}