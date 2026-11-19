package com.example.user.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.user.R;
import com.example.user.model.Contract;
import com.example.user.model.MoveOutRequest;
import com.example.user.network.ApiCallback;
import com.example.user.repository.RentalRepository;
import com.example.user.utils.Format;
import com.example.user.utils.StatusUtil;
import com.example.user.utils.Ui;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

/**
 * Gửi yêu cầu trả phòng + xem trạng thái yêu cầu.
 * API: GET/POST users/me/move-out-requests (backend routers/move_out.py).
 * Admin: PENDING -> APPROVED -> COMPLETED (hợp đồng ENDED, phòng trống lại).
 * Mỗi hợp đồng chỉ có 1 yêu cầu đang chờ / đã duyệt.
 */
public class MoveOutActivity extends AppCompatActivity {

    private TextView txtNoRoom;
    private TextView txtMoveOutStatus;
    private View layoutContent;
    private View cardExisting;
    private View layoutForm;
    private TextView txtRoomName;
    private TextView txtContractDate;
    private TextView txtRoomPrice;
    private TextView txtExistingDate;
    private TextView txtExistingReason;
    private TextInputLayout layoutMoveOutDate;
    private TextInputEditText edtMoveOutDate;
    private TextInputEditText edtReason;
    private MaterialButton btnSubmit;
    private LinearProgressIndicator progressBar;

    private final RentalRepository repository = new RentalRepository();
    private Contract contract;
    private Calendar moveOutDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_out);
        Ui.setupHeader(this, "Trả phòng");

        txtNoRoom = findViewById(R.id.txtNoRoom);
        txtMoveOutStatus = findViewById(R.id.txtMoveOutStatus);
        layoutContent = findViewById(R.id.layoutContent);
        cardExisting = findViewById(R.id.cardExisting);
        layoutForm = findViewById(R.id.layoutForm);
        txtRoomName = findViewById(R.id.txtRoomName);
        txtContractDate = findViewById(R.id.txtContractDate);
        txtRoomPrice = findViewById(R.id.txtRoomPrice);
        txtExistingDate = findViewById(R.id.txtExistingDate);
        txtExistingReason = findViewById(R.id.txtExistingReason);
        layoutMoveOutDate = findViewById(R.id.layoutMoveOutDate);
        edtMoveOutDate = findViewById(R.id.edtMoveOutDate);
        edtReason = findViewById(R.id.edtReason);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        // Mặc định: 30 ngày sau
        moveOutDate = Calendar.getInstance();
        moveOutDate.add(Calendar.DAY_OF_MONTH, 30);
        edtMoveOutDate.setText(Format.date(Format.toApiDate(moveOutDate)));
        edtMoveOutDate.setOnClickListener(v -> Ui.pickDate(this, moveOutDate, 0, picked -> {
            moveOutDate = picked;
            edtMoveOutDate.setText(Format.date(Format.toApiDate(moveOutDate)));
            layoutMoveOutDate.setError(null);
        }));

        btnSubmit.setOnClickListener(v -> confirmSubmit());

        loadData();
    }

    private void loadData() {
        Ui.visible(progressBar, true);
        repository.getActiveContract(new ApiCallback<Contract>() {
            @Override
            public void onSuccess(Contract data) {
                Ui.visible(progressBar, false);
                contract = data;
                if (contract == null || !"ACTIVE".equals(contract.getStatus())) {
                    Ui.visible(txtNoRoom, true);
                    Ui.visible(layoutContent, false);
                    return;
                }
                Ui.visible(txtNoRoom, false);
                Ui.visible(layoutContent, true);

                txtRoomName.setText(contract.getRoomTitle() + " • Hợp đồng #" + contract.getId());
                txtContractDate.setText("Thuê từ " + Format.date(contract.getStartDate())
                        + (contract.getEndDate() == null ? "" : " đến " + Format.date(contract.getEndDate())));
                txtRoomPrice.setText(Format.money(contract.getMonthlyPrice()) + " / tháng • Cọc "
                        + Format.money(contract.getDepositAmount()));

                loadExistingRequest();
            }

            @Override
            public void onError(String message) {
                Ui.visible(progressBar, false);
                Ui.toast(MoveOutActivity.this, message);
            }
        });
    }

    private void loadExistingRequest() {
        repository.getMoveOutRequests(new ApiCallback<java.util.List<MoveOutRequest>>() {
            @Override
            public void onSuccess(java.util.List<MoveOutRequest> list) {
                // Yêu cầu mới nhất (danh sách đã xếp mới nhất trước) của hợp đồng hiện tại,
                // bỏ qua yêu cầu đã hủy
                MoveOutRequest data = null;
                if (list != null) {
                    for (MoveOutRequest r : list) {
                        if (r.getContractId() == contract.getId() && !"CANCELLED".equals(r.getStatus())) {
                            data = r;
                            break;
                        }
                    }
                }
                boolean hasRequest = data != null;
                Ui.visible(cardExisting, hasRequest);

                boolean canSend = !hasRequest || "REJECTED".equals(data.getStatus());
                Ui.visible(layoutForm, canSend);

                if (hasRequest) {
                    StatusUtil.apply(txtMoveOutStatus, data.getStatus());
                    txtExistingDate.setText("Ngày dự kiến trả phòng: " + Format.date(data.getMoveOutDate())
                            + "\nNgày gửi: " + Format.date(data.getCreatedAt()));
                    String reason = "Lý do: " + (Format.orEmpty(data.getReason()).isEmpty()
                            ? "(không ghi)" : data.getReason());
                    if (data.getAdminNote() != null && !data.getAdminNote().isEmpty()) {
                        reason += "\nChủ nhà: " + data.getAdminNote();
                    }
                    txtExistingReason.setText(reason);
                }
            }

            @Override
            public void onError(String message) {
                Ui.toast(MoveOutActivity.this, message);
            }
        });
    }

    private void confirmSubmit() {
        new AlertDialog.Builder(this)
                .setTitle("Gửi yêu cầu trả phòng")
                .setMessage("Bạn muốn trả phòng vào ngày " + Format.date(Format.toApiDate(moveOutDate)) + "?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Gửi", (d, w) -> submit())
                .show();
    }

    private void submit() {
        btnSubmit.setEnabled(false);
        repository.createMoveOutRequest(Format.toApiDate(moveOutDate), Ui.text(edtReason),
                new ApiCallback<MoveOutRequest>() {
                    @Override
                    public void onSuccess(MoveOutRequest data) {
                        btnSubmit.setEnabled(true);
                        Ui.toast(MoveOutActivity.this, "Đã gửi yêu cầu trả phòng, vui lòng chờ chủ nhà xác nhận");
                        loadExistingRequest();
                    }

                    @Override
                    public void onError(String message) {
                        btnSubmit.setEnabled(true);
                        Ui.toast(MoveOutActivity.this, message);
                    }
                });
    }
}
