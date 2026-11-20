package com.example.user.model;





public class Payment {

    private int id;
    private int contractId;
    private double amount;
    private String paymentType;
    private String paymentPeriod;
    private String status;
    private String paidAt;
    private String note;
    private String createdAt;
    private String roomTitle;

    public int getId() { return id; }
    public int getContractId() { return contractId; }
    public double getAmount() { return amount; }
    public String getPaymentType() { return paymentType; }
    public String getPaymentPeriod() { return paymentPeriod; }
    public String getStatus() { return status; }
    public String getPaidAt() { return paidAt; }
    public String getNote() { return note; }
    public String getCreatedAt() { return createdAt; }
    public String getRoomTitle() { return roomTitle; }
}
