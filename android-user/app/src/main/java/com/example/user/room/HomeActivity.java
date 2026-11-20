
package com.example.user.room;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.user.R;
import com.example.user.adapter.RoomAdapter;
import com.example.user.model.Contract;
import com.example.user.model.Room;
import com.example.user.model.User;
import com.example.user.network.ApiCallback;
import com.example.user.rental.MyRoomActivity;
import com.example.user.repository.AuthRepository;
import com.example.user.repository.RentalRepository;
import com.example.user.repository.RoomRepository;
import com.example.user.utils.BottomNavHelper;
import com.example.user.utils.SessionManager;
import com.example.user.utils.StatusUtil;
import com.example.user.utils.Ui;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class HomeActivity extends AppCompatActivity {


    private TextView txtUserName;
    private TextView txtSearch;
    private TextView txtExploreAll;
    private TextView txtViewAllRooms;
    private TextView txtAvailableSubtitle;
    private TextView txtMyRoomTitle;
    private TextView txtMyRoomName;
    private TextView txtMyRoomStatus;


    private MaterialCardView cardSearch;
    private MaterialCardView cardRoom;
    private MaterialCardView cardApartment;
    private MaterialCardView cardMyRoom;


    private RecyclerView rvAvailableRooms;


    private BottomNavigationView bottomNavigation;


    private RoomAdapter roomAdapter;
    private final RoomRepository roomRepository = new RoomRepository();
    private final RentalRepository rentalRepository = new RentalRepository();
    private final AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupUserInfo();
        setupRecyclerView();
        setupEvents();
        setupBottomNavigation();
    }


    private void initViews() {

        txtUserName = findViewById(R.id.txtUserName);
        txtSearch = findViewById(R.id.txtSearch);
        txtExploreAll = findViewById(R.id.txtExploreAll);
        txtViewAllRooms = findViewById(R.id.txtViewAllRooms);
        txtAvailableSubtitle = findViewById(R.id.txtAvailableSubtitle);

        txtMyRoomTitle = findViewById(R.id.txtMyRoomTitle);
        txtMyRoomName = findViewById(R.id.txtMyRoomName);
        txtMyRoomStatus = findViewById(R.id.txtMyRoomStatus);

        cardSearch = findViewById(R.id.cardSearch);
        cardRoom = findViewById(R.id.cardRoom);
        cardApartment = findViewById(R.id.cardApartment);
        cardMyRoom = findViewById(R.id.cardMyRoom);

        rvAvailableRooms = findViewById(R.id.rvAvailableRooms);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }


    private void setupUserInfo() {

        txtUserName.setText(shortName(SessionManager.getInstance().getFullName()));



        authRepository.syncSession(new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                txtUserName.setText(shortName(user.getFullName()));
            }

            @Override
            public void onError(String message) {
                Ui.toast(HomeActivity.this, message);
                if (!SessionManager.getInstance().isLoggedIn()) {

                    startActivity(new Intent(HomeActivity.this, com.example.user.auth.LoginActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                }
            }
        });


        showMyRoom(false, "", "");
    }


    private String shortName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "bạn";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1];
    }


    private void setupRecyclerView() {

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.VERTICAL,
                        false
                );

        rvAvailableRooms.setLayoutManager(layoutManager);

        rvAvailableRooms.setHasFixedSize(false);
        rvAvailableRooms.setNestedScrollingEnabled(false);

        roomAdapter = new RoomAdapter(new RoomAdapter.Listener() {
            @Override
            public void onRoomClick(Room room) {
                Intent intent = new Intent(HomeActivity.this, RoomDetailActivity.class);
                intent.putExtra(RoomDetailActivity.EXTRA_ROOM_ID, room.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Room room, int position) {
                toggleFavorite(room);
            }
        });
        rvAvailableRooms.setAdapter(roomAdapter);
    }


    private void loadAvailableRooms() {
        Map<String, String> filters = RoomRepository.query(null, 5);

        roomRepository.getRooms(filters, new ApiCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> rooms) {
                roomAdapter.setRooms(rooms);
                txtAvailableSubtitle.setText(rooms.isEmpty()
                        ? "Hiện chưa có phòng trống"
                        : "Những phòng mới nhất đang chờ bạn");
            }

            @Override
            public void onError(String message) {
                txtAvailableSubtitle.setText(message);
            }
        });
    }


    private void toggleFavorite(Room room) {
        roomRepository.setFavorite(room.getId(), room.isFavorite(), new ApiCallback<String>() {
            @Override
            public void onSuccess(String message) {
                Ui.toast(HomeActivity.this, message);
            }

            @Override
            public void onError(String message) {
                Ui.toast(HomeActivity.this, message);
                loadAvailableRooms();
            }
        });
    }


    private void loadMyRoom() {
        rentalRepository.getActiveContract(new ApiCallback<Contract>() {
            @Override
            public void onSuccess(Contract contract) {
                if (contract == null) {
                    showMyRoom(false, "", "");
                    return;
                }
                showMyRoom(true, contract.getRoomTitle(),
                        "● " + StatusUtil.label(contract.getStatus()) + " • Hợp đồng #" + contract.getId());
            }

            @Override
            public void onError(String message) {
                showMyRoom(false, "", "");
            }
        });
    }


    private void setupEvents() {


        cardSearch.setOnClickListener(v -> {
            openActivity(SearchActivity.class);
        });


        txtExploreAll.setOnClickListener(v -> {
            openActivity(SearchActivity.class);
        });


        txtViewAllRooms.setOnClickListener(v -> {
            openActivity(SearchActivity.class);
        });


        cardRoom.setOnClickListener(v -> {
            openSearchByType("ROOM");
        });


        cardApartment.setOnClickListener(v -> {
            openSearchByType("APARTMENT");
        });


        cardMyRoom.setOnClickListener(v -> {
            openActivity(MyRoomActivity.class);
        });
    }


    private void openSearchByType(String roomType) {

        Intent intent = new Intent(
                HomeActivity.this,
                SearchActivity.class
        );

        intent.putExtra("room_type", roomType);

        startActivity(intent);
    }


    private void showMyRoom(
            boolean hasRoom,
            String roomName,
            String roomStatus
    ) {

        if (hasRoom) {

            txtMyRoomTitle.setVisibility(View.VISIBLE);
            cardMyRoom.setVisibility(View.VISIBLE);

            txtMyRoomName.setText(roomName);
            txtMyRoomStatus.setText(roomStatus);

        } else {

            txtMyRoomTitle.setVisibility(View.GONE);
            cardMyRoom.setVisibility(View.GONE);
        }
    }


    private void setupBottomNavigation() {
        BottomNavHelper.setup(this, bottomNavigation, R.id.navHome);
    }


    private void openActivity(Class<?> activityClass) {

        Intent intent = new Intent(
                HomeActivity.this,
                activityClass
        );

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavHelper.refresh(bottomNavigation, R.id.navHome);


        loadAvailableRooms();
        loadMyRoom();
    }
}
