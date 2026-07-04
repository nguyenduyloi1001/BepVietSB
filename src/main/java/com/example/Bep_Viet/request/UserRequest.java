package com.example.Bep_Viet.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String email;

    @NotBlank(message = "Tài khoản không được để trống")
    @Size(min = 6, max = 255, message = "Tài khoản từ 6 đến 255 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 255, message = "Mật khẩu từ 6 đến 255 ký tự")
    private String password;


    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên từ 2 đến 100 ký tự")
    private String fullName;

    @Size(max = 500, message = "URL ảnh tối đa 500 ký tự")
    private String avatarUrl;

    @Size(max = 500, message = "Bio tối đa 500 ký tự")
    private String bio;

    @Size(max = 100, message = "Địa chỉ tối đa 100 ký tự")
    private String location;
}
