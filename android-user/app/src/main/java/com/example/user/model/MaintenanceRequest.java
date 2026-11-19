package com.example.user.model;

/**
 * Yêu cầu sửa chữa (GET/POST users/me/maintenance-requests). status: PENDING | IN_PROGRESS | DONE | REJECTED | CANCELLED.
 */
public class MaintenanceRequest {

    private int id;
    private int contractId;
    private int roomId;
    private String roomTitle;
    private String title;
    private String description;
    private String priority;
    private String imageUrl;
    private String status;
    private String adminNote;
    private String resolvedAt;
    private String createdAt;
    private String updatedAt;

    public int getId() { return id; }
    public int getContractId() { return contractId; }
    public int getRoomId() { return roomId; }
    public String getRoomTitle() { return roomTitle; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status; }
    public String getAdminNote() { return adminNote; }
    public String getResolvedAt() { return resolvedAt; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
