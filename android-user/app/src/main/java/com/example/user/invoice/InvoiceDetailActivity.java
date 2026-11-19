package com.example.user.invoice;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.user.R;
import com.example.user.model.Payment;
import com.example.user.network.ApiCallback;
import com.example.user.repository.RentalRepository;
import com.example.user.utils.Format;
import com.example.user.utils.StatusUtil;
import com.example.user.utils.Ui;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

/**
 * Chi tiết 1 khoản thanh toán (lấy từ GET users/me/payments).
 * Backend chung CHƯA có chỉ số điện/nước => ẩn 2 mục điện, nước của layout.
 */
public class InvoiceDetailActivity extends AppCompatActivity {

    public static final String EXTRA_INVOICE_ID = "invoice_id";

    private TextView txtInvoiceMonth;
    private TextView txtInvoiceRoom;
    private TextView txtInvoiceTotal;
    private TextView txtInvoiceStatus;
    private TextView txtInvoiceDue;
    private LinearLayout layoutFees;
    private LinearLayout layoutPayments;
    private View layoutContent;
    private LinearProgressIndicator progressBar;

    private final RentalRepository repository = new RentalRepository();
    private int paymentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail);
        Ui.setupHeader(this, "Chi tiết thanh toán");

        paymentId = getIntent().getIntExtra(EXTRA_INVOICE_ID, 0);

        txtInvoiceMonth = findViewById(R.id.txtInvoiceMonth);
        txtInvoiceRoom = findViewById(R.id.txtInvoiceRoom);
        txtInvoiceTotal = findViewById(R.id.txtInvoiceTotal);
        txtInvoiceStatus = findViewById(R.id.txtInvoiceStatus);
        txtInvoiceDue = findViewById(R.id.txtInvoiceDue);
        layoutFees = findViewById(R.id.layoutFees);
        layoutPayments = findViewById(R.id.layoutPayments);
        layoutContent = findViewById(R.id.layoutContent);
        progressBar = findViewById(R.id.progressBar);

        // Chưa có dữ liệu điện/nước, chưa có API thanh toán online
        hideSection(findViewById(R.id.layoutElectricity));
        hideSection(findViewById(R.id.layoutWater));
        MaterialButton btnPay = findViewById(R.id.btnPay);
        Ui.visible(btnPay, false);
        layoutContent.setVisibility(View.INVISIBLE);

        loadPayment();
    }

    /** Ẩn thẻ chứa container + tiêu đề ngay phía trên thẻ */
    private void hideSection(View container) {
        if (!(container.getParent() instanceof View)) {
            return;
        }
        View card = (View) container.getParent();
        card.setVisibility(View.GONE);
        if (card.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) card.getParent();
            int index = parent.indexOfChild(card);
            if (index > 0 && parent.getChildAt(index - 1) instanceof TextView) {
                parent.getChildAt(index - 1).setVisibility(View.GONE);
            }
        }
    }

    private void loadPayment() {
        Ui.visible(progressBar, true);
        repository.getPayments(new ApiCallback<List<Payment>>() {
            @Override
            public void onSuccess(List<Payment> list) {
                Ui.visible(progressBar, false);
                Payment found = null;
                if (list != null) {
                    for (Payment p : list) {
                        if (p.getId() == paymentId) {
                            found = p;
                            break;
                        }
                    }
                }
                if (found == null) {
                    Ui.toast(InvoiceDetailActivity.this, "Không tìm thấy khoản thanh toán");
                    finish();
                    return;
                }
                bind(found);
            }

            @Override
            public void onError(String message) {
                Ui.visible(progressBar, false);
                Ui.toast(InvoiceDetailActivity.this, message);
            }
        });
    }

    private void bind(Payment p) {
        layoutContent.setVisibility(View.VISIBLE);

        String title = StatusUtil.paymentType(p.getPaymentType());
        if (!TextUtils.isEmpty(p.getPaymentPeriod())) {
            title += " kỳ " + Format.period(p.getPaymentPeriod());
        }
        txtInvoiceMonth.setText(title);
        txtInvoiceRoom.setText((TextUtils.isEmpty(p.getRoomTitle()) ? "" : p.getRoomTitle() + " • ")
                + "Hợp đồng #" + p.getContractId());
        txtInvoiceTotal.setText(Format.money(p.getAmount()));
        StatusUtil.apply(txtInvoiceStatus, p.getStatus());
        txtInvoiceDue.setText("Tạo ngày: " + Format.date(p.getCreatedAt()));

        layoutFees.removeAllViews();
        Ui.addRow(layoutFees, "Mã thanh toán", "#" + p.getId());
        Ui.addRow(layoutFees, "Loại", StatusUtil.paymentType(p.getPaymentType()));
        if (!TextUtils.isEmpty(p.getPaymentPeriod())) {
            Ui.addRow(layoutFees, "Kỳ thanh toán", Format.period(p.getPaymentPeriod()));
        }
        Ui.addRow(layoutFees, "Trạng thái", StatusUtil.label(p.getStatus()));
        if (!TextUtils.isEmpty(p.getNote())) {
            Ui.addRow(layoutFees, "Ghi chú", p.getNote());
        }
        Ui.addRow(layoutFees, "Số tiền", Format.money(p.getAmount()), true);

        layoutPayments.removeAllViews();
        if (p.getPaidAt() != null) {
            Ui.addRow(layoutPayments, "Đã thanh toán lúc", Format.date(p.getPaidAt()));
        } else if ("PENDING".equalsIgnoreCase(p.getStatus())) {
            Ui.addRow(layoutPayments, "Chưa thanh toán",
                    "Thanh toán cho chủ nhà, chủ nhà sẽ xác nhận trên hệ thống");
        } else {
            Ui.addRow(layoutPayments, "Trạng thái", StatusUtil.label(p.getStatus()));
        }
    }
}
