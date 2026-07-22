package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.enums.Role;
import com.example.Bep_Viet.enums.RoleRequestStatus;
import com.example.Bep_Viet.model.RoleRequest;
import com.example.Bep_Viet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRequestRepository extends JpaRepository<RoleRequest, Long> {
    boolean existsByUserAndRoleTypeAndStatus(User user, Role roleType, RoleRequestStatus status);

    List<RoleRequest> findByStatus(RoleRequestStatus status);

    // lịch sử đơn của 1 user
    List<RoleRequest> findByUser(User user);
}
