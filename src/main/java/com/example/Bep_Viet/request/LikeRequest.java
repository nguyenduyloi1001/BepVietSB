package com.example.Bep_Viet.request;

import com.example.Bep_Viet.model.Like;
import lombok.Data;

@Data
public class LikeRequest {
    private Long targetId;
    private Like.TargetType targetType;
}