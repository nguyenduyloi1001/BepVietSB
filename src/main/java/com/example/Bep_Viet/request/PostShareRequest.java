package com.example.Bep_Viet.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostShareRequest {
    private Long postId;
    private String content; // lời dẫn, có thể để trống
}