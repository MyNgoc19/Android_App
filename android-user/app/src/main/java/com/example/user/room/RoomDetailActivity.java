package com.example.user.room;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.user.R;
import com.example.user.appointment.BookAppointmentActivity;
import com.example.user.model.Room;
import com.example.user.network.ApiCallback;
import com.example.user.rental.RentalActivity;
import com.example.user.repository.RoomRepository;
import com.example.user.utils.Format;
import com.example.user.utils.ImageLoader;
import com.example.user.utils.StatusUtil;
import com.example.user.utils.Ui;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;





public class RoomDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_ID = "room_id";

    private ImageView imgRoom;
    private TextView btnFavorite;
    private TextView txtRoomType;
    private TextView txtRoomStatus;
    private TextView txtRoomTitle;
    private TextView txtRoomAddress;
    private TextView txtRoomPrice;
    private TextView txtDescription;
    private TextView txtLandlord;
    private TextView txtUnavailable;
    private LinearLayout layoutRoomInfo;
    private LinearLayout layoutContent;
    private ChipGroup chipGroupAmenities;
    private MaterialButton btnBookAppointment;
    private MaterialButton btnRent;
    private MaterialButton btnCall;
    private LinearProgressIndicator progressBar;

    private final RoomRepository roomRepository = new RoomRepository();
    private int roomId;
    private Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        roomId = getIntent().getIntExtra(EXTRA_ROOM_ID, 0);
        if (roomId == 0) {
            Ui.toast(this, "Không tìm thấy phòng");
            finish();
            return;
        }

        initViews();
        setupEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadRoom();
    }

    private void initViews() {
        imgRoom = findViewById(R.id.imgRoom);
        btnFavorite = findViewById(R.id.btnFavorite);
        txtRoomType = findViewById(R.id.txtRoomType);
        txtRoomStatus = findViewById(R.id.txtRoomStatus);
        txtRoomTitle = findViewById(R.id.txtRoomTitle);
        txtRoomAddress = findViewById(R.id.txtRoomAddress);
        txtRoomPrice = findViewById(R.id.txtRoomPrice);
        txtDescription = findViewById(R.id.txtDescription);
        txtLandlord = findViewById(R.id.txtLandlord);
        txtUnavailable = findViewById(R.id.txtUnavailable);
        layoutRoomInfo = findViewById(R.id.layoutRoomInfo);
        layoutContent = findViewById(R.id.layoutContent);
        chipGroupAmenities = findViewById(R.id.chipGroupAmenities);
        btnBookAppointment = findViewById(R.id.btnBookAppointment);
        btnRent = findViewById(R.id.btnRent);
        btnCall = findViewById(R.id.btnCall);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupEvents() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnFavorite.setOnClickListener(v -> toggleFavorite());

        btnBookAppointment.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookAppointmentActivity.class);
            intent.putExtra(BookAppointmentActivity.EXTRA_ROOM_ID, roomId);
            intent.putExtra(BookAppointmentActivity.EXTRA_ROOM_TITLE, room.getTitle());
            intent.putExtra(BookAppointmentActivity.EXTRA_ROOM_ADDRESS, room.getFullAddress());
            startActivity(intent);
        });

        btnRent.setOnClickListener(v -> {
            Intent intent = new Intent(this, RentalActivity.class);
            intent.putExtra(RentalActivity.EXTRA_ROOM_ID, roomId);
            startActivity(intent);
        });


        btnCall.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:" + Ui.HOTLINE))));
    }

    private void loadRoom() {
        Ui.visible(progressBar, true);
        roomRepository.getRoom(roomId, new ApiCallback<Room>() {
            @Override
            public void onSuccess(Room data) {
                Ui.visible(progressBar, false);
                room = data;
                bindRoom();
            }

            @Override
            public void onError(String message) {
                Ui.visible(progressBar, false);
                Ui.toast(RoomDetailActivity.this, message);
            }
        });
    }

    private void bindRoom() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        layoutContent.setVisibility(View.VISIBLE);
        ImageLoader.load(imgRoom, room.getFirstImage());

        txtRoomType.setText(Format.roomType(room.isApartment()));
        StatusUtil.apply(txtRoomStatus, room.getStatus());
        txtRoomTitle.setText(room.getTitle());
        txtRoomAddress.setText(room.getFullAddress());
        txtRoomPrice.setText(Format.pricePerMonth(room.getMonthlyPrice()));
        txtDescription.setText(TextUtils.isEmpty(room.getDescription())
                ? "Chưa có mô tả" : room.getDescription());
        txtLandlord.setText("N&N Home • " + Ui.HOTLINE);
        updateFavoriteIcon();

        layoutRoomInfo.removeAllViews();
        Ui.addRow(layoutRoomInfo, "Diện tích", Format.area(room.getAreaM2()));
        Ui.addRow(layoutRoomInfo, "Số người tối đa", room.getCapacity() + " người");
        Ui.addRow(layoutRoomInfo, "Giá thuê", Format.money(room.getMonthlyPrice()) + "/tháng");
        Ui.addRow(layoutRoomInfo, "Khu vực", Format.orEmpty(room.getDistrict())
                + (TextUtils.isEmpty(room.getCity()) ? "" : ", " + room.getCity()));
        Ui.addRow(layoutRoomInfo, "Trạng thái", StatusUtil.label(room.getStatus()));

        chipGroupAmenities.removeAllViews();
        if (room.getAmenities() == null || room.getAmenities().isEmpty()) {
            Chip chip = new Chip(this);
            chip.setText("Chưa cập nhật");
            chip.setClickable(false);
            chipGroupAmenities.addView(chip);
        } else {
            for (String amenity : room.getAmenities()) {
                Chip chip = new Chip(this);
                chip.setText(amenity);
                chip.setClickable(false);
                chip.setCheckable(false);
                chipGroupAmenities.addView(chip);
            }
        }


        boolean available = "AVAILABLE".equals(room.getStatus());
        btnBookAppointment.setEnabled(available);
        btnRent.setEnabled(available);
        Ui.visible(txtUnavailable, !available);
    }

    private void updateFavoriteIcon() {
        btnFavorite.setText(room.isFavorite() ? "♥" : "♡");
    }

    private void toggleFavorite() {
        if (room == null) {
            return;
        }
        boolean newValue = !room.isFavorite();
        room.setFavorite(newValue);
        updateFavoriteIcon();

        roomRepository.setFavorite(roomId, newValue, new ApiCallback<String>() {
            @Override
            public void onSuccess(String message) {
                Ui.toast(RoomDetailActivity.this, message);
            }

            @Override
            public void onError(String message) {
                room.setFavorite(!newValue);
                updateFavoriteIcon();
                Ui.toast(RoomDetailActivity.this, message);
            }
        });
    }
}
