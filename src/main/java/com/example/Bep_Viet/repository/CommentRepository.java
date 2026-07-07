package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.enums.TargetType;
import com.example.Bep_Viet.model.Comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    // Lấy tất cả comment gốc (parent_id = null) của 1 target
    List<Comment> findByTargetIdAndTargetTypeAndParentIsNull(Long targetId, TargetType targetType);

    // Lấy tất cả reply của 1 comment cha
    List<Comment> findByParentId(Long parentId);

    // Xoá toàn bộ comment của 1 target (khi xoá bài viết/recipe)
    void deleteByTargetIdAndTargetType(Long targetId, TargetType targetType);

//    @Modifying
//    @Transactional
//    @Query("DELETE FROM Comment c WHERE c.post.id = :postId")
//    void deleteByPostId(Long postId);

}
