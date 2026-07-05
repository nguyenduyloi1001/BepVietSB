package com.example.Bep_Viet.service;

import com.example.Bep_Viet.response.UserResponse;

import java.util.List;

public interface UserFollowService {
    void followUser(Long followerId,Long followingId);
    void unfollowUser (Long followId,Long followingId);
    boolean isFollowing(Long followId,Long followingId);
    List<UserResponse> getFollowers(Long userId);
    List<UserResponse> getFollowing(Long userId);
    long countFollowers(Long userId);
    long countFollowing(Long userId);
}
