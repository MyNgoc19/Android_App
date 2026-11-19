package com.example.user.room;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.user.invoice.InvoiceDetailActivity;
import com.example.user.utils.Ui;

/**
 * Backend chung chưa có API để người thuê tự thanh toán / báo đã thanh toán
 * (chỉ có GET users/me/payments). Chủ nhà ghi nhận thanh toán trên Web Admin.
 * Màn này giữ lại để không lỗi Manifest; mở ra sẽ báo và đóng.
 */
public class PaymentActivity extends AppCompatActivity {

    public static final String EXTRA_INVOICE_ID = InvoiceDetailActivity.EXTRA_INVOICE_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Ui.toast(this, "Thanh toán trực tiếp với chủ nhà. Chủ nhà sẽ xác nhận trên hệ thống.");
        finish();
    }
}
