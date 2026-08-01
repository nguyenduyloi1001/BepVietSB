package com.example.Bep_Viet.service;

import com.example.Bep_Viet.enums.RoleRequestStatus;
import com.example.Bep_Viet.request.RoleRequestRequest;
import com.example.Bep_Viet.response.RoleRequestResponse;

import java.util.List;

public interface RoleRequestService {
    RoleRequestResponse submitRequest(Long userId, RoleRequestRequest request);

    RoleRequestResponse  approveRequest(Long requestId,Long adminId);

    RoleRequestResponse rejectRequest(Long requestId,Long adminId,String note);

    List<RoleRequestResponse> getPendingRequests();

    List<RoleRequestResponse> getRequestsByUser(Long userId);

    List<RoleRequestResponse> getRequestsByStatus(RoleRequestStatus status);

    List<RoleRequestResponse> getAllRequests();
}
