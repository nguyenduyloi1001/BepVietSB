package com.example.Bep_Viet.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(1001, "Không tìm thấy người dùng"),
    USERNAME_EXISTED(1002, "Username đã tồn tại"),
    EMAIL_EXISTED(1003, "Email đã tồn tại"),
    UNAUTHORIZED(1004, "Không có quyền truy cập"),
    CATEGORY_NOT_FOUND(1005, "Category không tồn tại"),
    SLUG_EXISTED(1006, "Slug đã tồn tại"),
    INVALID_OTP(1007, "OTP không hợp lệ hoặc đã hết hạn"),
    OTP_EXPIRED(1008, "Phiên đăng ký đã hết hạn, vui lòng thử lại"),
    INGREDIENT_NOT_FOUND(2001,"Không tìm thấy nguyên liệu"),
    INGREDIENT_NAME_EXISTED(2002,"Tên nguyên liệu đã tồn tại"),
    INGREDIENT_SLUG_EXISTED(2003,"Slug nguyên liệu đã tồn tại"),

    RECIPE_NOT_FOUND(3001,"Công thức không tồn tại"),
    RECIPE_NAME_EXISTED(3002,"Tên công thức đã tồn tại"),
    RECIPE_SLUG_EXISTED(3003,"Slug đã tồn tại công thức"),
    RECIPE_SLUG_NOT_FOUND(3004,"Không tìm thấy Slug"),
    RECIPE_FORBIDDEN(3005,"Bạn không có quyền thực hiện hành dộng này"),
    RECIPE_INGREDIENT_NOT_FOUND(3005, "Không tìm thấy nguyên liệu trong công thức"),
    INGREDIENT_ALREADY_IN_RECIPE(3006, "Nguyên liệu đã tồn tại trong công thức này"),
    RECIPE_STEP_NOT_FOUND(3007, "Bước thực hiện không tồn tại"),
    STEP_NUMBER_EXISTED(3008, "Số thứ tự bước đã tồn tại trong công thức này"),
    COOKBOOK_NOT_FOUND(4001,"Không tìm thấy Cookbook"),
    COOKBOOK_NAME_EXISTED(4002,"Tên Cookbook đã tồn tại"),
    COOKBOOK_FORBIDDEN(4003,"Bạn không có quyền thao tác trên Cookbook này"),
    RECIPE_ALREADY_IN_COOKBOOK(4004,"Công thức đã tồn tại trong Cookbook"),
    RECIPE_NOT_IN_COOKBOOK(4005,"Công thức không tồn tại trong Cookbook"),
    POST_NOT_FOUND(4006,"Bài viết không tồn tại"),
    POST_FORBIDDEN(4007,"Bạn không có quyền thao tác bài viết này"),
    COMMENT_NOT_FOUND(4008,"Không tìm thấy comment"),
    QUESTION_NOT_FOUND(5001,"Không tìm thấy Question"),
    ANSWER_NOT_FOUND(5002,"Không tìm thấy Answer"),
    ANSWER_ALREADY_EXISTS(5003,"Câu trả lời đã tồn tại"),
    ANSWER_FORBIDDEN(5004,"Bạn không có quyền trả lời câu hỏi này"),
    MEAL_PLAN_NOT_FOUND(5005,"Meal plan không tìm thấy"),
    MEAL_PLAN_ITEM_EXISTED(5006,"Meal plan item đã tồn tại"),
    FORBIDDEN(5007,"bạn không có quyền thao tác"),
    REPORT_NOT_FOUND(5008,"Không tìm tháya report"),
    RATING_NOT_FOUND(6001, "Không tìm thấy đánh giá"),
    ALREADY_RATED(6002, "Bạn đã đánh giá công thức này rồi"),
    NOTIFICATION_NOT_FOUND(6003, "Notification not found"),
    NOTIFICATION_UNAUTHORIZED(6004, "Không có quyền thao tác thông báo này"),
    SHOPPING_LIST_NOT_FOUND(7001, "Không tìm thấy danh sách đi chợ"),
    SHOPPING_LIST_ITEM_NOT_FOUND(7002, "Không tìm thấy nguyên liệu trong danh sách"),
    SHOPPING_LIST_FORBIDDEN(7003, "Bạn không có quyền thao tác danh sách này"),
    SHOPPING_LIST_ALREADY_EXISTS(7004,"Danh sách Shopping List đã tồn tại"),
    ROLE_REQUEST_NOT_FOUND(8001, "Không tìm thấy đơn đăng ký"),
    ROLE_REQUEST_ALREADY_PENDING(8002, "Bạn đang có đơn chờ duyệt, vui lòng chờ xử lý"),
    ROLE_REQUEST_ALREADY_APPROVED(8003, "Bạn đã là Chef/Blogger rồi"),
    ROLE_REQUEST_NOT_PENDING(8004, "Đơn này đã được xử lý rồi"),
    ROLE_REQUEST_INVALID_ROLE(8005, "Chỉ được đăng ký vai trò Chef hoặc Blogger"),
    NOT_ENOUGH_LIKES(8006, "Công thức của bạn chưa đủ lượt like"),
    NOT_ENOUGH_RECIPES(8007, "Bạn chưa đủ số công thức yêu cầu"),
    NOT_ENOUGH_RATING(8008, "Điểm đánh giá trung bình chưa đủ yêu cầu"),
    AI_SERVICE_ERROR(9001, "Lỗi khi gọi AI, vui lòng thử lại sau"),
    AI_NO_SUGGESTION(9002, "Không tìm thấy món ăn phù hợp với nguyên liệu đã nhập"),
    SESSION_NOT_FOUND(9003, "Phiên chat không tồn tại"),
    AI_SERV_ERROR(9004, "Lỗi dịch vụ AI, vui lòng thử lại sau"),
    AI_NO_SUG(9005, "Không tìm thấy công thức phù hợp"),
    COMMENT_EMPTY(9006,"Comment trống rỗng"),
    GUEST_SESSION_EXPIRED(9007,"Phiên chat đã hết hạn, vui lòng bắt đầu cuộc trò chuyện mới"),
    GUEST_ID_REQUIRED(9008,"Thiếu định danh khách, vui lòng thử lại");



    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
