package com.example.user.model;

import java.util.List;

/**
 * Hợp đồng (GET users/me/contracts, users/me/contracts/{id}).
 * JSON snake_case (monthly_price) được Gson tự map sang camelCase (monthlyPrice).
 */
public class Contract {

    private int id;
    private int requestId;
    private int roomId;
    private int tenantId;
    private String startDate;
    private String endDate;
    private double monthlyPrice;
    private double depositAmount;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String tenantName;
    private String tenantEmail;
    private String tenantPhone;
    private String roomTitle;
    private String roomDescription;
    private String roomAddress;
    private String roomWard;
    private String roomDistrict;
    private String roomCity;
    private Double currentRoomPrice;
    private String roomStatus;
    private String roomImage;
    private List<Payment> payments;

    public int getId() { return id; }
    public int getRequestId() { return requestId; }
    public int getRoomId() { return roomId; }
    public int getTenantId() { return tenantId; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public double getMonthlyPrice() { return monthlyPrice; }
    public double getDepositAmount() { return depositAmount; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getTenantName() { return tenantName; }
    public String getTenantEmail() { return tenantEmail; }
    public String getTenantPhone() { return tenantPhone; }
    public String getRoomTitle() { return roomTitle; }
    public String getRoomDescription() { return roomDescription; }
    public String getRoomAddress() { return roomAddress; }
    public String getRoomWard() { return roomWard; }
    public String getRoomDistrict() { return roomDistrict; }
    public String getRoomCity() { return roomCity; }
    public Double getCurrentRoomPrice() { return currentRoomPrice; }
    public String getRoomStatus() { return roomStatus; }
    public String getRoomImage() { return roomImage; }
    public List<Payment> getPayments() { return payments; }

    /** Địa chỉ phòng đầy đủ */
    public String getRoomFullAddress() {
        StringBuilder sb = new StringBuilder(roomAddress == null ? "" : roomAddress);
        for (String part : new String[]{roomWard, roomDistrict, roomCity}) {
            if (part != null && !part.trim().isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(part.trim());
            }
        }
        return sb.toString();
    }
}
