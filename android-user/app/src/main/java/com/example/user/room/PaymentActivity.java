package com.example.user.room;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.user.invoice.InvoiceDetailActivity;
import com.example.user.utils.Ui;






public class PaymentActivity extends AppCompatActivity {

    public static final String EXTRA_INVOICE_ID = InvoiceDetailActivity.EXTRA_INVOICE_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Ui.toast(this, "Thanh toán trực tiếp với chủ nhà. Chủ nhà sẽ xác nhận trên hệ thống.");
        finish();
    }
}
