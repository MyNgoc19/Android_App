package com.example.user.model;




public class MoveOutRequest {

    private int id;
    private int contractId;
    private int roomId;
    private String roomTitle;
    private String moveOutDate;
    private String reason;
    private String status;
    private String adminNote;
    private String contractStatus;
    private String createdAt;
    private String updatedAt;

    public int getId() { return id; }
    public int getContractId() { return contractId; }
    public int getRoomId() { return roomId; }
    public String getRoomTitle() { return roomTitle; }
    public String getMoveOutDate() { return moveOutDate; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public String getAdminNote() { return adminNote; }
    public String getContractStatus() { return contractStatus; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
