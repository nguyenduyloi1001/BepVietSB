package com.example.Bep_Viet.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LikeResponse {
    private boolean liked;       // true = vừa like, false = vừa unlike
    private long totalLikes;
}