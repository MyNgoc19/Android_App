package com.example.user.rental;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.user.R;
import com.example.user.auth.MoveOutActivity;
import com.example.user.invoice.InvoiceListActivity;
import com.example.user.model.Contract;
import com.example.user.network.ApiCallback;
import com.example.user.repository.RentalRepository;
import com.example.user.room.MaintenanceActivity;
import com.example.user.room.RoomDetailActivity;
import com.example.user.room.SearchActivity;
import com.example.user.utils.Format;
import com.example.user.utils.ImageLoader;
import com.example.user.utils.StatusUtil;
import com.example.user.utils.Ui;
import com.google.android.material.progressindicator.LinearProgressIndicator;

/**
 * Phòng của tôi = hợp đồng ACTIVE (GET users/me/contracts).
 */
public class MyRoomActivity extends AppCompatActivity {

    private View layoutEmpty;
    private View scrollMyRoom;
    private ImageView imgRoom;
    private TextView txtRoomStatus;
    private TextView txtRoomTitle;
    private TextView txtRoomAddress;
    private TextView txtContractCode;
    private TextView txtStartDate;
    private TextView txtEndDate;
    private TextView txtMonthlyRent;
    private TextView txtNextStep;
    private LinearProgressIndicator progressBar;

    private final RentalRepository repository = new RentalRepository();
    private Contract contract;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_room);
        Ui.setupHeader(this, "Phòng của tôi");

        layoutEmpty = findViewById(R.id.layoutEmpty);
        scrollMyRoom = findViewById(R.id.scrollMyRoom);
        imgRoom = findViewById(R.id.imgRoom);
        txtRoomStatus = findViewById(R.id.txtRoomStatus);
        txtRoomTitle = findViewById(R.id.txtRoomTitle);
        txtRoomAddress = findViewById(R.id.txtRoomAddress);
        txtContractCode = findViewById(R.id.txtContractCode);
        txtStartDate = findViewById(R.id.txtStartDate);
        txtEndDate = findViewById(R.id.txtEndDate);
        txtMonthlyRent = findViewById(R.id.txtMonthlyRent);
        txtNextStep = findViewById(R.id.txtNextStep);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnFindRoom).setOnClickListener(v -> open(SearchActivity.class));
        findViewById(R.id.cardRoom).setOnClickListener(v -> {
            Intent intent = new Intent(this, RoomDetailActivity.class);
            intent.putExtra(RoomDetailActivity.EXTRA_ROOM_ID, contract.getRoomId());
            startActivity(intent);
        });
        findViewById(R.id.btnViewContract).setOnClickListener(v -> {
            Intent intent = new Intent(this, ContractActivity.class);
            intent.putExtra(ContractActivity.EXTRA_CONTRACT_ID, contract.getId());
            startActivity(intent);
        });
        findViewById(R.id.btnDeposit).setOnClickListener(v -> {
            Intent intent = new Intent(this, DepositActivity.class);
            intent.putExtra(DepositActivity.EXTRA_CONTRACT_ID, contract.getId());
            startActivity(intent);
        });
        findViewById(R.id.btnInvoices).setOnClickListener(v -> open(InvoiceListActivity.class));
        findViewById(R.id.btnMaintenance).setOnClickListener(v -> open(MaintenanceActivity.class));
        findViewById(R.id.btnMoveOut).setOnClickListener(v -> open(MoveOutActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyRoom();
    }

    private void open(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }

    private void loadMyRoom() {
        Ui.visible(progressBar, true);
        repository.getActiveContract(new ApiCallback<Contract>() {
            @Override
            public void onSuccess(Contract data) {
                Ui.visible(progressBar, false);
                contract = data;
                boolean has = contract != null;
                Ui.visible(layoutEmpty, !has);
                Ui.visible(scrollMyRoom, has);
                if (has) {
                    bind();
                }
            }

            @Override
            public void onError(String message) {
                Ui.visible(progressBar, false);
                Ui.toast(MyRoomActivity.this, message);
            }
        });
    }

    private void bind() {
        if (contract.getRoomImage() != null && !contract.getRoomImage().isEmpty()) {
            ImageLoader.load(imgRoom, contract.getRoomImage());
        } else {
            // API hợp đồng không trả ảnh => lấy ảnh từ GET rooms/{id}
            new com.example.user.repository.RoomRepository().getRoom(contract.getRoomId(),
                    new ApiCallback<com.example.user.model.Room>() {
                        @Override
                        public void onSuccess(com.example.user.model.Room room) {
                            if (room != null && !isFinishing()) {
                                ImageLoader.load(imgRoom, room.getFirstImage());
                            }
                        }

                        @Override
                        public void onError(String message) {
                        }
                    });
        }
        StatusUtil.apply(txtRoomStatus, contract.getStatus());
        txtRoomTitle.setText(contract.getRoomTitle());
        txtRoomAddress.setText(contract.getRoomFullAddress());

        txtContractCode.setText("Mã hợp đồng: #" + contract.getId());
        txtStartDate.setText("Ngày bắt đầu: " + Format.date(contract.getStartDate()));
        txtEndDate.setText("Ngày kết thúc: " + (contract.getEndDate() == null
                ? "Không thời hạn" : Format.date(contract.getEndDate())));
        txtMonthlyRent.setText("Tiền thuê: " + Format.money(contract.getMonthlyPrice()) + " / tháng");

        txtNextStep.setText("Tiền cọc " + Format.money(contract.getDepositAmount())
                + ". Bạn có thể xem hợp đồng, thông tin cọc và lịch sử thanh toán.");
        Ui.visible(txtNextStep, true);
    }
}
