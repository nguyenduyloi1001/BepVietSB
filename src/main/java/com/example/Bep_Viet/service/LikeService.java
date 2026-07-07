package com.example.Bep_Viet.service;

import com.example.Bep_Viet.request.LikeRequest;
import com.example.Bep_Viet.response.LikeResponse;

public interface LikeService {
    LikeResponse toggle(Long userId, LikeRequest request);
    long count(Long targetId, String targetType);
    boolean isLiked(Long userId, Long targetId, String targetType);
    long countTotalLikesByUserId(Long userId);
}