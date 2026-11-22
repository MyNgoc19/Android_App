package com.example.user.model;





public class RentalRequest {

    private int id;
    private int roomId;
    private int userId;
    private String message;
    private String desiredMoveIn;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String roomTitle;
    private String roomAddress;
    private String roomDistrict;
    private String roomCity;
    private Double roomMonthlyPrice;
    private String roomImage;

    public int getId() { return id; }
    public int getRoomId() { return roomId; }
    public int getUserId() { return userId; }
    public String getMessage() { return message; }
    public String getDesiredMoveIn() { return desiredMoveIn; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getRoomTitle() { return roomTitle; }
    public String getRoomAddress() { return roomAddress; }
    public String getRoomDistrict() { return roomDistrict; }
    public String getRoomCity() { return roomCity; }
    public Double getRoomMonthlyPrice() { return roomMonthlyPrice; }
    public String getRoomImage() { return roomImage; }
}
