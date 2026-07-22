package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.enums.NotificationTargetType;
import com.example.Bep_Viet.enums.NotificationType;
import com.example.Bep_Viet.enums.Role;
import com.example.Bep_Viet.enums.RoleRequestStatus;
import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.RoleRequest;
import com.example.Bep_Viet.model.User;
import com.example.Bep_Viet.repository.RatingRepository;
import com.example.Bep_Viet.repository.RecipeRepository;
import com.example.Bep_Viet.repository.RoleRequestRepository;
import com.example.Bep_Viet.repository.UserRepository;
import com.example.Bep_Viet.request.RoleRequestRequest;
import com.example.Bep_Viet.response.RoleRequestResponse;
import com.example.Bep_Viet.service.LikeService;
import com.example.Bep_Viet.service.NotificationService;
import com.example.Bep_Viet.service.RoleRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RoleRequestServiceImpl implements RoleRequestService {
    private final UserRepository userRepository;
    private final RoleRequestRepository roleRequestRepository;
    private final LikeService likeService;
    private final RatingRepository ratingRepository;
    private final RecipeRepository recipeRepository;
    private final NotificationService notificationService;

    private static final long CHEF_MIN_LIKES = 1;
    private static final double CHEF_MIN_RATING = 4.0;

    @Override
    public RoleRequestResponse submitRequest(Long userId, RoleRequestRequest request) {
        System.out.println(">>> submitRequest called, userId: " + userId + " roleType: " + request.getRoleType());
        User user = userRepository.findById(userId).orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND));
        if (request.getRoleType()!=Role.CHEF && request.getRoleType()!= Role.BLOGGER){
            throw new AppException(ErrorCode.ROLE_REQUEST_INVALID_ROLE);
        }
        if(request.getRoleType()== user.getRole()){
            throw new AppException(ErrorCode.ROLE_REQUEST_ALREADY_APPROVED);
        }
        if(roleRequestRepository.existsByUserAndRoleTypeAndStatus(user,request.getRoleType(), RoleRequestStatus.PENDING)){
            throw new AppException(ErrorCode.ROLE_REQUEST_ALREADY_PENDING);
        }
        if(request.getRoleType()==Role.CHEF){
            validateChefCriteria(userId);
        }
        RoleRequest roleRequest = RoleRequest.builder()
                .user(user)
                .roleType(request.getRoleType())
                .reason(request.getReason())
                .status(RoleRequestStatus.PENDING)
                .build();

        return mapToResponse(roleRequestRepository.save(roleRequest));
    }

    @Override
    public RoleRequestResponse approveRequest(Long requestId, Long adminId) {
        RoleRequest roleRequest = findById(requestId);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (roleRequest.getStatus() != RoleRequestStatus.PENDING) {
            throw new AppException(ErrorCode.ROLE_REQUEST_NOT_PENDING);
        }

        User user = roleRequest.getUser();
        user.setRole(roleRequest.getRoleType());
        userRepository.save(user);

        roleRequest.setStatus(RoleRequestStatus.APPROVED);
        roleRequest.setReviewedBy(admin);
        roleRequest.setReviewedAt(LocalDateTime.now());

        RoleRequestResponse response = mapToResponse(roleRequestRepository.save(roleRequest));

        // 👈 gửi thông báo cho user
        notificationService.send(
                user.getId(),
                null,
                NotificationType.ROLE_REQUEST_APPROVED,
                roleRequest.getId(),
                NotificationTargetType.role_request,
                null
        );

        return response;
    }

    @Override
    public RoleRequestResponse rejectRequest(Long requestId, Long adminId, String note) {
        RoleRequest roleRequest = findById(requestId);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (roleRequest.getStatus() != RoleRequestStatus.PENDING) {
            throw new AppException(ErrorCode.ROLE_REQUEST_NOT_PENDING);
        }

        roleRequest.setStatus(RoleRequestStatus.REJECTED);
        roleRequest.setReviewedBy(admin);
        roleRequest.setReviewedAt(LocalDateTime.now());
        roleRequest.setNote(note);

        RoleRequestResponse response = mapToResponse(roleRequestRepository.save(roleRequest));

        // 👈 gửi thông báo cho user
        notificationService.send(
                roleRequest.getUser().getId(),
                null,
                NotificationType.ROLE_REQUEST_REJECTED,
                roleRequest.getId(),
                NotificationTargetType.role_request,
                note
        );

        return response;
    }

    @Override
    public List<RoleRequestResponse> getPendingRequests() {
        return roleRequestRepository.findByStatus(RoleRequestStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RoleRequestResponse> getRequestsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return roleRequestRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RoleRequestResponse> getRequestsByStatus(RoleRequestStatus status) {
        return roleRequestRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RoleRequestResponse> getAllRequests() {
        return roleRequestRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    //helper
    private RoleRequest findById(Long id){
        return roleRequestRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.ROLE_REQUEST_NOT_FOUND));
    }

    private void validateChefCriteria(Long userId) {
        long totalLikes = likeService.countTotalLikesByUserId(userId);
        System.out.println(">>> totalLikes: " + totalLikes);
        if (totalLikes < CHEF_MIN_LIKES) {
            throw new AppException(ErrorCode.NOT_ENOUGH_LIKES);
        }

        List<Long> recipeIds = recipeRepository.findByUserId(userId)
                .stream()
                .map(r -> r.getId())
                .toList();
        System.out.println(">>> recipeIds: " + recipeIds);

        if (recipeIds.isEmpty()) {
            throw new AppException(ErrorCode.NOT_ENOUGH_RECIPES);
        }

        // Chỉ lấy recipe đã có người rate
        List<Double> ratings = recipeIds.stream()
                .map(ratingRepository::getAverageStars)
                .filter(avg -> avg != null)
                .toList();

        System.out.println(">>> ratings: " + ratings);

        // Có recipe được rate thì mới check
        if (!ratings.isEmpty()) {
            double avgRating = ratings.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            System.out.println(">>> avgRating: " + avgRating);

            if (avgRating < CHEF_MIN_RATING) {
                throw new AppException(ErrorCode.NOT_ENOUGH_RATING);
            }
        }
    }

    private RoleRequestResponse mapToResponse(RoleRequest request){
        return RoleRequestResponse.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .username(request.getUser().getUsername())
                .fullName(request.getUser().getFullName())
                .roleType(request.getRoleType())
                .note(request.getNote())
                .reason(request.getReason())
                .status(request.getStatus())
                .reviewedBy(request.getReviewedBy() != null ? request.getReviewedBy().getId() : null)
                .createdAt(request.getCreatedAt())
                .reviewedAt(request.getReviewedAt())
                .build();
    }
}