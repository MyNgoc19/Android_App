package com.example.user.model;

/**
 * Khoản thanh toán (GET users/me/payments). payment_type: DEPOSIT | RENT | OTHER; status: PENDING | PAID | FAILED | REFUNDED.
 * JSON snake_case (monthly_price) được Gson tự map sang camelCase (monthlyPrice).
 */
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
