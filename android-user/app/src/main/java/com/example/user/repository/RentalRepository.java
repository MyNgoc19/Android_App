package com.example.user.repository;

import com.example.user.model.Contract;
import com.example.user.model.MaintenanceRequest;
import com.example.user.model.MoveOutRequest;
import com.example.user.model.Payment;
import com.example.user.model.RentalRequest;
import com.example.user.network.ApiCallback;
import com.example.user.network.ApiCaller;
import com.example.user.network.ApiService;
import com.example.user.network.RetrofitClient;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Yêu cầu thuê, hợp đồng, thanh toán của người dùng đang đăng nhập.
 * Không gửi user_id: backend tự lấy user từ Firebase token.
 */
public class RentalRepository {

    private final ApiService api = RetrofitClient.getApi();

    // ===== Rental requests =====

    /**
     * @param desiredMoveIn yyyy-MM-dd hoặc null
     */
    public void createRentalRequest(int roomId, String message, String desiredMoveIn,
                                    ApiCallback<Void> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        if (desiredMoveIn != null) {
            body.put("desired_move_in", desiredMoveIn);
        }
        ApiCaller.enqueue(api.createRentalRequest(roomId, body), ignoreBody(callback));
    }

    public void getRentalRequests(ApiCallback<List<RentalRequest>> callback) {
        ApiCaller.enqueue(api.getRentalRequests(), callback);
    }

    public void getRentalRequest(int requestId, ApiCallback<RentalRequest> callback) {
        ApiCaller.enqueue(api.getRentalRequest(requestId), callback);
    }

    public void cancelRentalRequest(int requestId, ApiCallback<Void> callback) {
        ApiCaller.enqueue(api.cancelRentalRequest(requestId), ignoreBody(callback));
    }

    // ===== Contracts =====

    public void getContracts(ApiCallback<List<Contract>> callback) {
        ApiCaller.enqueue(api.getContracts(), callback);
    }

    public void getContract(int contractId, ApiCallback<Contract> callback) {
        ApiCaller.enqueue(api.getContract(contractId), callback);
    }

    /** Hợp đồng ACTIVE (phòng đang thuê), có kèm danh sách payments. null nếu chưa thuê */
    public void getActiveContract(ApiCallback<Contract> callback) {
        getContracts(new ApiCallback<List<Contract>>() {
            @Override
            public void onSuccess(List<Contract> list) {
                Contract active = null;
                if (list != null) {
                    for (Contract c : list) {
                        if ("ACTIVE".equalsIgnoreCase(c.getStatus())) {
                            active = c;
                            break;
                        }
                    }
                }
                if (active == null) {
                    callback.onSuccess(null);
                } else {
                    getContract(active.getId(), callback); // lấy bản chi tiết
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    // ===== Payments =====

    public void getPayments(ApiCallback<List<Payment>> callback) {
        ApiCaller.enqueue(api.getPayments(), callback);
    }

    // ===== Sửa chữa =====

    public void getMaintenanceRequests(ApiCallback<List<MaintenanceRequest>> callback) {
        ApiCaller.enqueue(api.getMaintenanceRequests(), callback);
    }

    /** priority: LOW | MEDIUM | HIGH */
    public void createMaintenanceRequest(String title, String description, String priority,
                                         ApiCallback<MaintenanceRequest> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("priority", priority);
        ApiCaller.enqueue(api.createMaintenanceRequest(body), callback);
    }

    public void cancelMaintenanceRequest(int requestId, ApiCallback<MaintenanceRequest> callback) {
        ApiCaller.enqueue(api.cancelMaintenanceRequest(requestId), callback);
    }

    // ===== Trả phòng =====

    public void getMoveOutRequests(ApiCallback<List<MoveOutRequest>> callback) {
        ApiCaller.enqueue(api.getMoveOutRequests(), callback);
    }

    /** moveOutDate: yyyy-MM-dd */
    public void createMoveOutRequest(String moveOutDate, String reason,
                                     ApiCallback<MoveOutRequest> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("move_out_date", moveOutDate);
        body.put("reason", reason);
        ApiCaller.enqueue(api.createMoveOutRequest(body), callback);
    }

    public void cancelMoveOutRequest(int requestId, ApiCallback<MoveOutRequest> callback) {
        ApiCaller.enqueue(api.cancelMoveOutRequest(requestId), callback);
    }

    private static ApiCallback<JsonElement> ignoreBody(ApiCallback<Void> callback) {
        return new ApiCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement data) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        };
    }
}
