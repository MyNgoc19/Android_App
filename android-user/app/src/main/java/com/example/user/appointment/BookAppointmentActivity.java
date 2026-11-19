package com.example.user.appointment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.user.R;
import com.example.user.network.ApiCallback;
import com.example.user.repository.RentalRepository;
import com.example.user.utils.Format;
import com.example.user.utils.Ui;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

/**
 * Đặt lịch xem phòng: chọn ngày + khung giờ + ghi chú.
 * Backend chung chưa có bảng lịch hẹn => gửi thành 1 RENTAL REQUEST
 * (POST rooms/{id}/rental-requests) với nội dung "[Hẹn xem phòng] ...".
 * Admin xem trong Web Admin → Rental Requests.
 */
public class BookAppointmentActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_ID = "room_id";
    public static final String EXTRA_ROOM_TITLE = "room_title";
    public static final String EXTRA_ROOM_ADDRESS = "room_address";

    // Khung giờ cho chọn
    private static final String[] TIME_SLOTS = {
            "08:00", "09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00", "19:00"
    };

    private TextInputLayout layoutDate;
    private TextInputEditText edtDate;
    private TextInputEditText edtNote;
    private ChipGroup chipGroupTime;
    private MaterialButton btnSubmit;

    private final RentalRepository repository = new RentalRepository();
    private Calendar selectedDate;
    private int roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);
        Ui.setupHeader(this, "Đặt lịch xem phòng");

        roomId = getIntent().getIntExtra(EXTRA_ROOM_ID, 0);
        ((TextView) findViewById(R.id.txtRoomTitle))
                .setText(getIntent().getStringExtra(EXTRA_ROOM_TITLE));
        ((TextView) findViewById(R.id.txtRoomAddress))
                .setText(getIntent().getStringExtra(EXTRA_ROOM_ADDRESS));

        layoutDate = findViewById(R.id.layoutDate);
        edtDate = findViewById(R.id.edtDate);
        edtNote = findViewById(R.id.edtNote);
        chipGroupTime = findViewById(R.id.chipGroupTime);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Tạo chip giờ
        for (String slot : TIME_SLOTS) {
            Chip chip = new Chip(this);
            chip.setText(slot);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setChipBackgroundColorResource(R.color.chip_bg);
            chip.setTextColor(getColorStateList(R.color.chip_text));
            chipGroupTime.addView(chip);
        }

        // Mặc định: ngày mai
        selectedDate = Calendar.getInstance();
        selectedDate.add(Calendar.DAY_OF_MONTH, 1);
        showDate();

        edtDate.setOnClickListener(v -> Ui.pickDate(this, selectedDate, 0, picked -> {
            selectedDate = picked;
            showDate();
        }));

        btnSubmit.setOnClickListener(v -> submit());
    }

    private void showDate() {
        edtDate.setText(Format.date(Format.toApiDate(selectedDate)));
        layoutDate.setError(null);
    }

    private void submit() {
        int checkedId = chipGroupTime.getCheckedChipId();
        if (checkedId == -1) {
            Ui.toast(this, "Vui lòng chọn giờ xem phòng");
            return;
        }
        String time = ((Chip) findViewById(checkedId)).getText().toString();

        // Không cho đặt giờ đã qua trong hôm nay
        Calendar chosen = (Calendar) selectedDate.clone();
        String[] hm = time.split(":");
        chosen.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hm[0]));
        chosen.set(Calendar.MINUTE, Integer.parseInt(hm[1]));
        if (chosen.before(Calendar.getInstance())) {
            Ui.toast(this, "Giờ này đã qua, vui lòng chọn giờ khác");
            return;
        }

        String note = Ui.text(edtNote);
        String message = "[Hẹn xem phòng] " + time + " ngày " + Format.date(Format.toApiDate(selectedDate))
                + (note.isEmpty() ? "" : ". Ghi chú: " + note);

        btnSubmit.setEnabled(false);
        repository.createRentalRequest(roomId, message, null, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Ui.toast(BookAppointmentActivity.this, "Đã gửi lịch hẹn, vui lòng chờ chủ nhà phản hồi");
                startActivity(new Intent(BookAppointmentActivity.this, MyAppointmentsActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                btnSubmit.setEnabled(true);
                Ui.toast(BookAppointmentActivity.this, message);
            }
        });
    }
}
