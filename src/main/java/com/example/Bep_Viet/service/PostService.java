package com.example.Bep_Viet.service;

import com.example.Bep_Viet.enums.PostStatus;
import com.example.Bep_Viet.enums.PostType;
import com.example.Bep_Viet.request.PostRequest;
import com.example.Bep_Viet.response.PostResponse;

import java.util.List;

public interface PostService {
    PostResponse create(PostRequest request, Long userId);
    PostResponse getPostById(Long id);
    List<PostResponse> getAllPost();
    List<PostResponse> getByUserId(Long userId);
    List<PostResponse> getByStatus (PostStatus status);
    List<PostResponse> getByType(PostType type);
    PostResponse update(Long id,PostRequest request,Long currentUserId);
    void delete(Long id, Long currentUserId, boolean isAdmin);
    PostResponse approve(Long id);
    PostResponse reject(Long id);
}
