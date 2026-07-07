package com.example.Bep_Viet.request;

import com.example.Bep_Viet.enums.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostRequest{
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không quá 255 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    @Size(max = 500, message = "Thumbnail không quá 500 ký tự")
    private String thumbnail;

    @NotNull(message = "Loại bài viết không được để trống")
    private PostType type;
}
