package com.example.user.rental;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AppCompatActivity;

import com.example.user.R;
import com.example.user.model.Room;
import com.example.user.network.ApiCallback;
import com.example.user.repository.RentalRepository;
import com.example.user.repository.RoomRepository;
import com.example.user.utils.Format;
import com.example.user.utils.ImageLoader;
import com.example.user.utils.Ui;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

/**
 * "Đồng ý thuê": gửi YÊU CẦU THUÊ tới chủ nhà.
 * Chủ nhà duyệt trên Web Admin rồi tạo hợp đồng => hợp đồng hiện ở "Phòng của tôi".
 */
public class RentalActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_ID = "room_id";

    private ImageView imgRoom;
    private TextView txtRoomTitle;
    private TextView txtRoomAddress;
    private TextView txtRoomPrice;
    private TextInputEditText edtStartDate;
    private TextInputEditText edtNote;
    private ChipGroup chipGroupMonths;
    private MaterialCheckBox cbAgree;
    private MaterialButton btnSubmit;
    private LinearProgressIndicator progressBar;

    private final RoomRepository roomRepository = new RoomRepository();
    private final RentalRepository rentalRepository = new RentalRepository();

    private int roomId;
    private Room room;
    private Calendar startDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rental);
        Ui.setupHeader(this, "Đồng ý thuê phòng");

        roomId = getIntent().getIntExtra(EXTRA_ROOM_ID, 0);

        imgRoom = findViewById(R.id.imgRoom);
        txtRoomTitle = findViewById(R.id.txtRoomTitle);
        txtRoomAddress = findViewById(R.id.txtRoomAddress);
        txtRoomPrice = findViewById(R.id.txtRoomPrice);
        edtStartDate = findViewById(R.id.edtStartDate);
        edtNote = findViewById(R.id.edtNote);
        chipGroupMonths = findViewById(R.id.chipGroupMonths);
        cbAgree = findViewById(R.id.cbAgree);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        // Mặc định bắt đầu sau 3 ngày
        startDate = Calendar.getInstance();
        startDate.add(Calendar.DAY_OF_MONTH, 3);
        edtStartDate.setText(Format.date(Format.toApiDate(startDate)));

        edtStartDate.setOnClickListener(v -> Ui.pickDate(this, startDate, 0, picked -> {
            startDate = picked;
            edtStartDate.setText(Format.date(Format.toApiDate(startDate)));
            updateSummary();
        }));
        chipGroupMonths.setOnCheckedStateChangeListener((g, ids) -> updateSummary());
        btnSubmit.setOnClickListener(v -> submit());

        loadRoom();
    }

    private void loadRoom() {
        Ui.visible(progressBar, true);
        btnSubmit.setEnabled(false);
        roomRepository.getRoom(roomId, new ApiCallback<Room>() {
            @Override
            public void onSuccess(Room data) {
                Ui.visible(progressBar, false);
                room = data;
                txtRoomTitle.setText(room.getTitle());
                txtRoomAddress.setText(room.getFullAddress());
                ImageLoader.load(imgRoom, room.getFirstImage());
                btnSubmit.setEnabled(true);
                updateSummary();
            }

            @Override
            public void onError(String message) {
                Ui.visible(progressBar, false);
                Ui.toast(RentalActivity.this, message);
                finish();
            }
        });
    }

    private int selectedMonths() {
        int id = chipGroupMonths.getCheckedChipId();
        if (id == R.id.chip6Months) return 6;
        if (id == R.id.chip24Months) return 24;
        return 12;
    }

    // Giá thuê + thời gian dự kiến hiển thị ngay dưới tên phòng
    private void updateSummary() {
        if (room == null) {
            return;
        }
        txtRoomPrice.setText(Format.pricePerMonth(room.getMonthlyPrice())
                + " • " + selectedMonths() + " tháng");
    }

    // Tóm tắt toàn bộ chi phí trước khi gửi
    private String buildSummary() {
        int months = selectedMonths();
        String start = Format.toApiDate(startDate);
        return "Phòng: " + room.getTitle()
                + "\nThời gian: " + Format.date(start) + " → " + Format.addMonths(start, months)
                + " (" + months + " tháng)"
                + "\nTiền thuê: " + Format.money(room.getMonthlyPrice()) + "/tháng"
                + "\n\nYêu cầu sẽ được gửi tới chủ nhà. Khi được duyệt, chủ nhà sẽ tạo hợp đồng"
                + " và báo số tiền cọc cho bạn.";
    }

    private void submit() {
        if (!cbAgree.isChecked()) {
            Ui.toast(this, "Vui lòng tích xác nhận đồng ý thuê");
            return;
        }
        if (room == null) {
            return;
        }

        View content = LayoutInflater.from(this)
                .inflate(R.layout.dialog_rental_confirmation, null, false);

        TextView roomName = content.findViewById(R.id.dialogRoomName);
        TextView roomAddress = content.findViewById(R.id.dialogRoomAddress);
        TextView rentalPeriod = content.findViewById(R.id.dialogRentalPeriod);
        TextView startDateText = content.findViewById(R.id.dialogStartDate);
        TextView monthlyPrice = content.findViewById(R.id.dialogMonthlyPrice);
        TextView duration = content.findViewById(R.id.dialogDuration);

        int months = selectedMonths();
        String start = Format.toApiDate(startDate);
        roomName.setText(room.getTitle());
        roomAddress.setText(room.getFullAddress());
        rentalPeriod.setText(Format.date(start) + "  –  " + Format.addMonths(start, months));
        startDateText.setText(Format.date(start));
        monthlyPrice.setText(Format.money(room.getMonthlyPrice()) + " / tháng");
        duration.setText(months + " tháng");

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(content)
                .create();

        MaterialButton editButton = content.findViewById(R.id.dialogEditButton);
        MaterialButton sendButton = content.findViewById(R.id.dialogSendButton);
        editButton.setOnClickListener(v -> dialog.dismiss());
        sendButton.setOnClickListener(v -> {
            dialog.dismiss();
            createRental();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    /**
     * Gửi Rental Request: POST rooms/{room_id}/rental-requests
     * body: message + desired_move_in (ngày bắt đầu thuê)
     */
    private void createRental() {
        btnSubmit.setEnabled(false);
        Ui.visible(progressBar, true);

        String note = Ui.text(edtNote);
        String message = "Muốn thuê " + selectedMonths() + " tháng."
                + (note.isEmpty() ? "" : " Ghi chú: " + note);

        rentalRepository.createRentalRequest(roomId, message, Format.toApiDate(startDate),
                new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        Ui.toast(RentalActivity.this, "Đã gửi yêu cầu thuê, vui lòng chờ chủ nhà duyệt");
                        // Xem danh sách yêu cầu thuê của tôi
                        startActivity(new Intent(RentalActivity.this,
                                com.example.user.appointment.MyAppointmentsActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        Ui.visible(progressBar, false);
                        btnSubmit.setEnabled(true);
                        Ui.toast(RentalActivity.this, message);
                    }
                });
    }
}
