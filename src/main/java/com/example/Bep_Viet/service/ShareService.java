package com.example.Bep_Viet.service;

import com.example.Bep_Viet.enums.TargetType;
import com.example.Bep_Viet.request.ShareRequest;
import com.example.Bep_Viet.response.ShareResponse;
import java.util.List;

public interface ShareService {
    ShareResponse share(ShareRequest request, Long userId);
    void unshare(Long shareId, Long currentUserId);
    List<ShareResponse> getByTarget(Long targetId, TargetType targetType);
    List<ShareResponse> getByUserId(Long userId);
    long count(Long targetId, TargetType targetType);
}