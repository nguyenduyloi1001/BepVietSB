package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.model.Tags;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tags, Long> {

    Optional<Tags> findBySlug(String slug);

    Optional<Tags> findByName(String name);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);
}