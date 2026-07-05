package com.example.Bep_Viet.repository;
    import com.example.Bep_Viet.model.UserFollow;
    import com.example.Bep_Viet.model.UserFollowId;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import java.util.List;
    @Repository
    public interface UserFollowRepository extends JpaRepository<UserFollow, UserFollowId> {
        List<UserFollow> findByFollowerId(Long followerId);
        List<UserFollow> findByFollowingId(Long followingId);
        long countByFollowerId(Long followerId);
        long countByFollowingId(Long followingId);
    }
