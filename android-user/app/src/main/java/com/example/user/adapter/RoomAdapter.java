package com.example.user.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.user.R;
import com.example.user.model.Room;
import com.example.user.utils.Format;
import com.example.user.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Danh sách phòng: dùng ở Home, Tìm kiếm, Yêu thích.
 */
public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    public interface Listener {
        void onRoomClick(Room room);

        /** Bấm trái tim. Không bắt buộc override. */
        default void onFavoriteClick(Room room, int position) {
        }
    }

    private final List<Room> rooms = new ArrayList<>();
    private final Listener listener;

    public RoomAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setRooms(List<Room> newRooms) {
        rooms.clear();
        if (newRooms != null) {
            rooms.addAll(newRooms);
        }
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        if (position >= 0 && position < rooms.size()) {
            rooms.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = rooms.get(position);

        holder.txtRoomTitle.setText(room.getTitle());
        holder.txtRoomAddress.setText(room.getFullAddress());
        holder.txtRoomPrice.setText(Format.pricePerMonth(room.getMonthlyPrice()));
        holder.txtRoomArea.setText(Format.area(room.getAreaM2()));
        holder.txtRoomType.setText(Format.roomType(room.isApartment()));
        holder.btnFavorite.setImageResource(
                room.isFavorite() ? R.drawable.ic_heart : R.drawable.ic_heart_outline);
        ImageLoader.load(holder.imgRoom, room.getFirstImage());

        holder.itemView.setOnClickListener(v -> listener.onRoomClick(room));
        holder.btnFavorite.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) {
                return;
            }
            // Đổi icon ngay cho mượt, Activity sẽ gọi API
            room.setFavorite(!room.isFavorite());
            notifyItemChanged(pos);
            listener.onFavoriteClick(room, pos);
        });
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgRoom;
        final ImageView btnFavorite;
        final TextView txtRoomType;
        final TextView txtRoomTitle;
        final TextView txtRoomAddress;
        final TextView txtRoomPrice;
        final TextView txtRoomArea;

        RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRoom = itemView.findViewById(R.id.imgRoom);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            txtRoomType = itemView.findViewById(R.id.txtRoomType);
            txtRoomTitle = itemView.findViewById(R.id.txtRoomTitle);
            txtRoomAddress = itemView.findViewById(R.id.txtRoomAddress);
            txtRoomPrice = itemView.findViewById(R.id.txtRoomPrice);
            txtRoomArea = itemView.findViewById(R.id.txtRoomArea);
        }
    }
}
