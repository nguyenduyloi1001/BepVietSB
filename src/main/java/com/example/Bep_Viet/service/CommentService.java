package com.example.Bep_Viet.service;

import com.example.Bep_Viet.enums.TargetType;
import com.example.Bep_Viet.request.CommentRequest;
import com.example.Bep_Viet.response.CommentResponse;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

public interface CommentService {
    CommentResponse create(CommentRequest request, MultipartFile image, Long userId);
    List<CommentResponse> getByTarget(Long targetId, TargetType targetType);
    CommentResponse update(Long id, String content, Long currentUserId);
    void delete(Long id,Long currentUserId);

}
