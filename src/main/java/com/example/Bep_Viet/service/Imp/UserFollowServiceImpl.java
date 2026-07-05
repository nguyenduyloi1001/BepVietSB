package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.User;
import com.example.Bep_Viet.model.UserFollow;
import com.example.Bep_Viet.model.UserFollowId;
import com.example.Bep_Viet.repository.UserFollowRepository;
import com.example.Bep_Viet.repository.UserRepository;
import com.example.Bep_Viet.response.UserResponse;
import com.example.Bep_Viet.service.UserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFollowServiceImpl implements UserFollowService {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;
    //private final NotificationService notificationService;

    @Override
    @Transactional
    public void followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }

        User follower = findUserById(followerId);
        User following = findUserById(followingId);

        UserFollowId id = new UserFollowId(followerId, followingId);

        if (isFollowExist(id)) {
            throw new IllegalStateException("User " + followerId + " is already following user " + followingId);
        }

        userFollowRepository.save(UserFollow.builder()
                .id(id)
                .follower(follower)
                .following(following)
                .build());

        // ✅ Gửi thông báo cho người được follow
//        notificationService.send(
//                followingId,
//                followerId,
//                NotificationType.NEW_FOLLOWER,
//                followerId,
//                null          // targetType = null (không cần với new_follower)
//        );
    }

    @Override
    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        UserFollowId id = new UserFollowId(followerId, followingId);
        if (!isFollowExist(id)) {
            throw new IllegalStateException("User " + followerId + " is not following user " + followingId);
        }
        userFollowRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followingId) {
        return isFollowExist(new UserFollowId(followerId, followingId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getFollowers(Long userId) {
        findUserById(userId);
        return userFollowRepository.findByFollowingId(userId)
                .stream()
                .map(uf -> mapToUserResponse(uf.getFollower()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getFollowing(Long userId) {
        findUserById(userId);
        return userFollowRepository.findByFollowerId(userId)
                .stream()
                .map(uf -> mapToUserResponse(uf.getFollowing()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countFollowers(Long userId) {
        return userFollowRepository.countByFollowingId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFollowing(Long userId) {
        return userFollowRepository.countByFollowerId(userId);
    }

    // ─── Helpers ─────────────────────────────────────────────────

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private boolean isFollowExist(UserFollowId id) {
        return userFollowRepository.existsById(id);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .location(user.getLocation())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
