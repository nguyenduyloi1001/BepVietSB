package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.enums.PostStatus;
import com.example.Bep_Viet.enums.PostType;
import com.example.Bep_Viet.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post,Long> {

    List<Post> findByUserId(Long userId);

    List<Post> findByPostStatus(PostStatus postStatus);

    List<Post> findByPostType(PostType postType);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
