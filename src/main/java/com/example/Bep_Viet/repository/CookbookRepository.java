package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.model.Cookbook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CookbookRepository extends JpaRepository<Cookbook,Long> {
    List<Cookbook> findByUserId(Long userId);
    boolean existsByNameAndUserId(String name,Long userId); //Kiểm tra user đã có cookbook tên này chưa
    boolean existsByNameAndUserIdAndIdNot(String name,Long userId,Long id); //Kiểm tra trùng tên nhưng loại trừ cookbook hiện tại
}