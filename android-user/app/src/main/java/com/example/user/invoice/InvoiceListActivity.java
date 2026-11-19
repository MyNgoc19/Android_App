package com.example.user.invoice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.user.R;
import com.example.user.adapter.InvoiceAdapter;
import com.example.user.model.Payment;
import com.example.user.network.ApiCallback;
import com.example.user.repository.RentalRepository;
import com.example.user.utils.Ui;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Lịch sử thanh toán (GET users/me/payments): tiền cọc, tiền thuê, khoản khác.
 * Lọc theo trạng thái ngay trên máy.
 */
public class InvoiceListActivity extends AppCompatActivity {

    private ChipGroup chipGroupStatus;
    private TextView txtEmpty;
    private LinearProgressIndicator progressBar;
    private InvoiceAdapter adapter;
    private final RentalRepository repository = new RentalRepository();
    private final List<Payment> allPayments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_list);
        Ui.setupHeader(this, "Thanh toán của tôi");

        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        txtEmpty = findViewById(R.id.txtEmpty);
        progressBar = findViewById(R.id.progressBar);

        // Đổi chữ chip cho đúng trạng thái của backend: PENDING / PAID / FAILED, REFUNDED
        ((Chip) findViewById(R.id.chipUnpaid)).setText("Chờ thanh toán");
        ((Chip) findViewById(R.id.chipPending)).setText("Thất bại / Hoàn tiền");
        ((Chip) findViewById(R.id.chipPaid)).setText("Đã thanh toán");

        RecyclerView rv = findViewById(R.id.rvInvoices);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InvoiceAdapter(payment -> {
            Intent intent = new Intent(this, InvoiceDetailActivity.class);
            intent.putExtra(InvoiceDetailActivity.EXTRA_INVOICE_ID, payment.getId());
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        chipGroupStatus.setOnCheckedStateChangeListener((g, ids) -> applyFilter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPayments();
    }

    private void loadPayments() {
        Ui.visible(progressBar, true);
        repository.getPayments(new ApiCallback<List<Payment>>() {
            @Override
            public void onSuccess(List<Payment> data) {
                Ui.visible(progressBar, false);
                allPayments.clear();
                if (data != null) {
                    allPayments.addAll(data);
                }
                applyFilter();
            }

            @Override
            public void onError(String message) {
                Ui.visible(progressBar, false);
                Ui.toast(InvoiceListActivity.this, message);
            }
        });
    }

    private void applyFilter() {
        int id = chipGroupStatus.getCheckedChipId();
        List<Payment> result = new ArrayList<>();
        for (Payment p : allPayments) {
            String s = p.getStatus() == null ? "" : p.getStatus().toUpperCase();
            boolean keep;
            if (id == R.id.chipUnpaid) {
                keep = s.equals("PENDING");
            } else if (id == R.id.chipPending) {
                keep = s.equals("FAILED") || s.equals("REFUNDED");
            } else if (id == R.id.chipPaid) {
                keep = s.equals("PAID");
            } else {
                keep = true;
            }
            if (keep) {
                result.add(p);
            }
        }
        adapter.setItems(result);
        Ui.visible(txtEmpty, result.isEmpty());
    }
}
