package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.model.PostShare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostShareRepository extends JpaRepository<PostShare, Long> {

    // Danh sách bài đã đăng lại trên trang cá nhân của 1 user
    Page<PostShare> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Kiểm tra user đã đăng lại bài viết này chưa (tránh đăng lại trùng)
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    // Đếm số lượt đăng lại của 1 bài viết
    long countByPostId(Long postId);
}