package com.example.user.room;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.user.R;
import com.example.user.adapter.RoomAdapter;
import com.example.user.model.Room;
import com.example.user.network.ApiCallback;
import com.example.user.repository.RoomRepository;
import com.example.user.utils.Ui;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

/**
 * Danh sách phòng yêu thích (bảng favorites). Bỏ tim => xóa khỏi danh sách.
 */
public class FavoriteActivity extends AppCompatActivity {

    private RoomAdapter adapter;
    private View layoutEmpty;
    private RecyclerView rvFavorites;
    private LinearProgressIndicator progressBar;
    private final RoomRepository roomRepository = new RoomRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        Ui.setupHeader(this, "Phòng yêu thích");

        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        Ui.visible(layoutEmpty, false);

        // Chưa có phòng nào => đi khám phá
        findViewById(R.id.btnExploreRooms).setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class)));

        RecyclerView rv = findViewById(R.id.rvFavorites);
        rvFavorites = rv;
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoomAdapter(new RoomAdapter.Listener() {
            @Override
            public void onRoomClick(Room room) {
                Intent intent = new Intent(FavoriteActivity.this, RoomDetailActivity.class);
                intent.putExtra(RoomDetailActivity.EXTRA_ROOM_ID, room.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Room room, int position) {
                // Ở màn này bấm tim luôn là bỏ yêu thích
                adapter.removeAt(position);
                showEmpty(adapter.getItemCount() == 0);
                roomRepository.setFavorite(room.getId(), false, new ApiCallback<String>() {
                    @Override
                    public void onSuccess(String message) {
                        Ui.toast(FavoriteActivity.this, message);
                    }

                    @Override
                    public void onError(String message) {
                        Ui.toast(FavoriteActivity.this, message);
                        loadFavorites();
                    }
                });
            }
        });
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void showEmpty(boolean empty) {
        Ui.visible(layoutEmpty, empty);
        Ui.visible(rvFavorites, !empty);
    }

    private void loadFavorites() {
        Ui.visible(progressBar, true);
        roomRepository.getFavoriteRooms(new ApiCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> rooms) {
                Ui.visible(progressBar, false);
                adapter.setRooms(rooms);
                showEmpty(rooms.isEmpty());
                TextView txtSubtitle = findViewById(R.id.txtSubtitle);
                txtSubtitle.setText(rooms.isEmpty()
                        ? "Những căn phòng bạn đã lưu"
                        : "Bạn đã lưu " + rooms.size() + " phòng");
            }

            @Override
            public void onError(String message) {
                Ui.visible(progressBar, false);
                Ui.toast(FavoriteActivity.this, message);
            }
        });
    }
}
