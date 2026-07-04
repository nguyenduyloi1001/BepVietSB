package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.enums.Role;
import com.example.Bep_Viet.enums.UserStatus;
import com.example.Bep_Viet.request.UserRequest;
import com.example.Bep_Viet.request.UserUpdateRequest;
import com.example.Bep_Viet.response.ApiResponse;
import com.example.Bep_Viet.response.UserResponse;
import com.example.Bep_Viet.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request){
        UserResponse user = userService.createUser(request);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("Tạo User thành công")
                .result(user)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById (@PathVariable Long id){
        UserResponse userResponse = userService.getUserById(id);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("Lấy thành công User")
                .result(userResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUser (){
        List<UserResponse> userResponses = userService.getAllUser();

        ApiResponse<List<UserResponse>> response = ApiResponse.<List<UserResponse>>builder()
                .code(1000)
                .message("Lấy danh sách User thành công")
                .result(userResponses)
                .build();
        return ResponseEntity.ok(response);
    }

    // GET: /api/users/status?status=ACTIVE
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUserByStatus(@RequestParam UserStatus status) {
        List<UserResponse> userResponses = userService.getAllUserByStatus(status);
        ApiResponse<List<UserResponse>> response = ApiResponse.<List<UserResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách người dùng theo status thành công")
                .result(userResponses)
                .build();
        return ResponseEntity.ok(response);
    }


    // GET: /api/users/role?role=MEMBER
    @GetMapping("/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUserByRole(@RequestParam Role role) {
        List<UserResponse> userResponses = userService.getAllUserByRole(role);
        ApiResponse<List<UserResponse>> response = ApiResponse.<List<UserResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách người dùng theo role thành công")
                .result(userResponses)
                .build();
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse userResponse = userService.updateUser(id, request);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("Cập nhật thông tin người dùng thành công")
                .result(userResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    // PATCH: /api/users/{id}/status?status=BANNED
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status) {
        UserResponse userResponse = userService.updateStatusUser(id, status);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("Cập nhật trạng thái người dùng thành công")
                .result(userResponse)
                .build();
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(1000)
                .message("Xóa người dùng thành công")
                .result(null)
                .build();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal Long userId) {
        UserResponse userResponse = userService.getUserById(userId);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("Lấy thành công User")
                .result(userResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @RequestParam Role role) {
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("Cập nhật vai trò người dùng thành công")
                .result(userService.updateRoleUser(id, role))
                .build());
    }
}
