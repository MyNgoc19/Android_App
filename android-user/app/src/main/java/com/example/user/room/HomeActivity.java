
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

    // TextView
    private TextView txtUserName;
    private TextView txtSearch;
    private TextView txtExploreAll;
    private TextView txtViewAllRooms;
    private TextView txtAvailableSubtitle;
    private TextView txtMyRoomTitle;
    private TextView txtMyRoomName;
    private TextView txtMyRoomStatus;

    // CardView
    private MaterialCardView cardSearch;
    private MaterialCardView cardRoom;
    private MaterialCardView cardApartment;
    private MaterialCardView cardMyRoom;

    // RecyclerView
    private RecyclerView rvAvailableRooms;

    // Bottom Navigation
    private BottomNavigationView bottomNavigation;

    // Dữ liệu
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

    // Ánh xạ các thành phần giao diện
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

    // Hiển thị thông tin người dùng (lấy nhanh từ phiên, sau đó cập nhật từ API)
    private void setupUserInfo() {

        txtUserName.setText(shortName(SessionManager.getInstance().getFullName()));

        // Đồng bộ lại tài khoản với backend (POST auth/session + GET auth/me),
        // đồng thời phát hiện tài khoản bị khóa
        authRepository.syncSession(new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                txtUserName.setText(shortName(user.getFullName()));
            }

            @Override
            public void onError(String message) {
                Ui.toast(HomeActivity.this, message);
                if (!SessionManager.getInstance().isLoggedIn()) {
                    // bị khóa / không đồng bộ được => về màn đăng nhập
                    startActivity(new Intent(HomeActivity.this, com.example.user.auth.LoginActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                }
            }
        });

        // Mặc định ẩn, loadMyRoom() sẽ hiện nếu đang thuê
        showMyRoom(false, "", "");
    }

    // "Phan Thụy Mỹ Ngọc" -> "Ngọc"
    private String shortName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "bạn";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1];
    }

    // Thiết lập danh sách phòng còn trống
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

    // Lấy 5 phòng còn trống mới nhất
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

    // Thêm / bỏ yêu thích
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

    // Phòng đang thuê / hợp đồng đang xử lý
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

    // Xử lý các sự kiện trên trang chủ
    private void setupEvents() {

        // Nhấn vào ô tìm kiếm
        cardSearch.setOnClickListener(v -> {
            openActivity(SearchActivity.class);
        });

        // Nhấn xem tất cả danh mục
        txtExploreAll.setOnClickListener(v -> {
            openActivity(SearchActivity.class);
        });

        // Nhấn xem tất cả phòng còn trống
        txtViewAllRooms.setOnClickListener(v -> {
            openActivity(SearchActivity.class);
        });

        // Nhấn danh mục phòng trọ
        cardRoom.setOnClickListener(v -> {
            openSearchByType("ROOM");
        });

        // Nhấn danh mục căn hộ
        cardApartment.setOnClickListener(v -> {
            openSearchByType("APARTMENT");
        });

        // Nhấn phòng đang thuê
        cardMyRoom.setOnClickListener(v -> {
            openActivity(MyRoomActivity.class);
        });
    }

    // Chuyển đến tìm kiếm theo loại phòng
    private void openSearchByType(String roomType) {

        Intent intent = new Intent(
                HomeActivity.this,
                SearchActivity.class
        );

        intent.putExtra("room_type", roomType);

        startActivity(intent);
    }

    // Hiển thị hoặc ẩn phòng đang thuê
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

    // Thiết lập thanh điều hướng phía dưới
    private void setupBottomNavigation() {
        BottomNavHelper.setup(this, bottomNavigation, R.id.navHome);
    }

    // Hàm chuyển màn hình dùng chung
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

        // Tải lại mỗi lần quay về (vd vừa thuê phòng / bấm yêu thích ở màn khác)
        loadAvailableRooms();
        loadMyRoom();
    }
}
