package com.example.Bep_Viet.service;

import com.example.Bep_Viet.request.PostShareRequest;
import com.example.Bep_Viet.response.PostShareResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostShareService {
    PostShareResponse sharePost(Long userId, PostShareRequest request);
    void unsharePost(Long userId, Long shareId);
    Page<PostShareResponse> getSharesByUser(Long userId, Pageable pageable);
}