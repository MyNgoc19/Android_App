package com.example.user.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.user.R;
import com.example.user.model.RentalRequest;
import com.example.user.utils.Format;
import com.example.user.utils.ImageLoader;
import com.example.user.utils.StatusUtil;

import java.util.ArrayList;
import java.util.List;





public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    public interface Listener {
        void onRoomClick(RentalRequest request);

        void onCancelClick(RentalRequest request);
    }

    private final List<RentalRequest> items = new ArrayList<>();
    private final Listener listener;

    public AppointmentAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<RentalRequest> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        RentalRequest r = items.get(position);

        h.txtRoom.setText(TextUtils.isEmpty(r.getRoomTitle()) ? "Phòng #" + r.getRoomId() : r.getRoomTitle());

        String time = "Gửi ngày " + Format.date(r.getCreatedAt());
        if (!TextUtils.isEmpty(r.getDesiredMoveIn())) {
            time = "Dọn vào " + Format.date(r.getDesiredMoveIn()) + " • " + time;
        }
        h.txtTime.setText(time);

        h.txtNote.setText(TextUtils.isEmpty(r.getMessage())
                ? Format.orEmpty(r.getRoomAddress())
                : r.getMessage());

        StatusUtil.apply(h.txtStatus, r.getStatus());
        ImageLoader.load(h.img, r.getRoomImage());


        boolean canCancel = "PENDING".equalsIgnoreCase(r.getStatus());
        h.btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);
        h.btnCancel.setText("Hủy yêu cầu");
        h.btnCancel.setOnClickListener(v -> listener.onCancelClick(r));
        h.itemView.setOnClickListener(v -> listener.onRoomClick(r));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView img;
        final TextView txtRoom;
        final TextView txtTime;
        final TextView txtNote;
        final TextView txtStatus;
        final TextView btnCancel;

        ViewHolder(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgAppointmentRoom);
            txtRoom = v.findViewById(R.id.txtAppointmentRoom);
            txtTime = v.findViewById(R.id.txtAppointmentTime);
            txtNote = v.findViewById(R.id.txtAppointmentNote);
            txtStatus = v.findViewById(R.id.txtAppointmentStatus);
            btnCancel = v.findViewById(R.id.btnCancelAppointment);
        }
    }
}
