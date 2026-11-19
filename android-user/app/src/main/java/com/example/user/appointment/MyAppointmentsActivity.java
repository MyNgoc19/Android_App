package com.example.user.appointment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.user.R;
import com.example.user.adapter.AppointmentAdapter;
import com.example.user.model.RentalRequest;
import com.example.user.network.ApiCallback;
import com.example.user.repository.RentalRepository;
import com.example.user.room.RoomDetailActivity;
import com.example.user.room.SearchActivity;
import com.example.user.utils.BottomNavHelper;
import com.example.user.utils.Ui;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

/**
 * Yêu cầu thuê & lịch hẹn của tôi (GET users/me/rental-requests).
 * Trạng thái: PENDING / APPROVED / REJECTED / CANCELLED. Hủy được yêu cầu đang chờ.
 */
public class MyAppointmentsActivity extends AppCompatActivity {

    private AppointmentAdapter adapter;
    private View layoutEmpty;
    private RecyclerView rvAppointments;
    private LinearProgressIndicator progressBar;
    private BottomNavigationView bottomNavigation;
    private final RentalRepository repository = new RentalRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);
        Ui.setupHeader(this, "Yêu cầu thuê & lịch hẹn");

        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        BottomNavHelper.setup(this, bottomNavigation, R.id.navAppointment);

        findViewById(R.id.btnFindRoom).setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class)));

        RecyclerView rv = findViewById(R.id.rvAppointments);
        rvAppointments = rv;
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(new AppointmentAdapter.Listener() {
            @Override
            public void onRoomClick(RentalRequest a) {
                Intent intent = new Intent(MyAppointmentsActivity.this, RoomDetailActivity.class);
                intent.putExtra(RoomDetailActivity.EXTRA_ROOM_ID, a.getRoomId());
                startActivity(intent);
            }

            @Override
            public void onCancelClick(RentalRequest a) {
                confirmCancel(a);
            }
        });
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavHelper.refresh(bottomNavigation, R.id.navAppointment);
        loadAppointments();
    }

    private void loadAppointments() {
        Ui.visible(progressBar, true);
        repository.getRentalRequests(new ApiCallback<List<RentalRequest>>() {
            @Override
            public void onSuccess(List<RentalRequest> data) {
                Ui.visible(progressBar, false);
                adapter.setItems(data);
                Ui.visible(layoutEmpty, data.isEmpty());
                Ui.visible(rvAppointments, !data.isEmpty());
            }

            @Override
            public void onError(String message) {
                Ui.visible(progressBar, false);
                Ui.toast(MyAppointmentsActivity.this, message);
            }
        });
    }

    private void confirmCancel(RentalRequest a) {
        new AlertDialog.Builder(this)
                .setTitle("Hủy yêu cầu")
                .setMessage("Bạn muốn hủy yêu cầu với phòng \"" + a.getRoomTitle() + "\"?")
                .setNegativeButton("Không", null)
                .setPositiveButton("Hủy yêu cầu", (d, w) ->
                        repository.cancelRentalRequest(a.getId(), new ApiCallback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                Ui.toast(MyAppointmentsActivity.this, "Đã hủy yêu cầu");
                                loadAppointments();
                            }

                            @Override
                            public void onError(String message) {
                                Ui.toast(MyAppointmentsActivity.this, message);
                            }
                        }))
                .show();
    }
}
