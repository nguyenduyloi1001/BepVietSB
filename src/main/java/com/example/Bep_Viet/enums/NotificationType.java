package com.example.Bep_Viet.enums;

public enum NotificationType {
    NEW_FOLLOWER,       // B bắt đầu follow A
    NEW_COMMENT,        // ai đó comment vào recipe/post của A
    NEW_LIKE,           // ai đó like recipe/post/comment của A
    NEW_ANSWER,         // ai đó trả lời comment của A
    RECIPE_APPROVED,    // recipe của A được admin duyệt
    RECIPE_REJECTED,     // recipe của A bị admin từ chối
    ROLE_REQUEST_APPROVED,  // 👈 thêm
    ROLE_REQUEST_REJECTED
}
