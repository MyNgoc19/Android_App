package com.example.user.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.user.R;
import com.example.user.model.MaintenanceRequest;
import com.example.user.utils.Format;
import com.example.user.utils.StatusUtil;
import com.example.user.utils.Ui;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;





public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.ViewHolder> {

    private final List<MaintenanceRequest> items = new ArrayList<>();

    public void setItems(List<MaintenanceRequest> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context c = parent.getContext();

        MaterialCardView card = new MaterialCardView(c);
        RecyclerView.LayoutParams cardParams = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = Ui.dp(c, 12);
        card.setLayoutParams(cardParams);
        card.setRadius(Ui.dp(c, 16));
        card.setCardElevation(Ui.dp(c, 3));
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout box = new LinearLayout(c);
        box.setOrientation(LinearLayout.VERTICAL);
        int p = Ui.dp(c, 16);
        box.setPadding(p, p, p, p);
        card.addView(box);


        LinearLayout top = new LinearLayout(c);
        top.setOrientation(LinearLayout.HORIZONTAL);
        TextView txtTitle = new TextView(c);
        txtTitle.setTextSize(15);
        txtTitle.setTypeface(Typeface.DEFAULT_BOLD);
        txtTitle.setTextColor(c.getColor(R.color.nn_text));
        top.addView(txtTitle, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView txtStatus = new TextView(c);
        txtStatus.setTextSize(12);
        txtStatus.setTypeface(Typeface.DEFAULT_BOLD);
        txtStatus.setBackgroundResource(R.drawable.bg_chip);
        txtStatus.setPadding(Ui.dp(c, 10), Ui.dp(c, 4), Ui.dp(c, 10), Ui.dp(c, 4));
        top.addView(txtStatus);
        box.addView(top);

        TextView txtDescription = new TextView(c);
        txtDescription.setTextSize(14);
        txtDescription.setTextColor(c.getColor(R.color.nn_text));
        txtDescription.setPadding(0, Ui.dp(c, 6), 0, 0);
        box.addView(txtDescription);

        TextView txtMeta = new TextView(c);
        txtMeta.setTextSize(12);
        txtMeta.setTextColor(Color.parseColor("#777777"));
        txtMeta.setPadding(0, Ui.dp(c, 6), 0, 0);
        box.addView(txtMeta);

        TextView txtNote = new TextView(c);
        txtNote.setTextSize(13);
        txtNote.setTextColor(c.getColor(R.color.nn_text));
        txtNote.setBackgroundColor(Color.parseColor("#FFF0F6"));
        txtNote.setPadding(Ui.dp(c, 10), Ui.dp(c, 8), Ui.dp(c, 10), Ui.dp(c, 8));
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noteParams.topMargin = Ui.dp(c, 8);
        box.addView(txtNote, noteParams);

        return new ViewHolder(card, txtTitle, txtStatus, txtDescription, txtMeta, txtNote);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        MaintenanceRequest m = items.get(position);
        h.txtTitle.setText(m.getTitle());
        h.txtDescription.setText(m.getDescription());
        h.txtMeta.setText("Mức độ: " + StatusUtil.priority(m.getPriority())
                + " • Gửi ngày " + Format.date(m.getCreatedAt()));
        StatusUtil.apply(h.txtStatus, m.getStatus());

        if (TextUtils.isEmpty(m.getAdminNote())) {
            h.txtNote.setVisibility(View.GONE);
        } else {
            h.txtNote.setVisibility(View.VISIBLE);
            h.txtNote.setText("Chủ nhà: " + m.getAdminNote());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtTitle;
        final TextView txtStatus;
        final TextView txtDescription;
        final TextView txtMeta;
        final TextView txtNote;

        ViewHolder(@NonNull View v, TextView title, TextView status, TextView description,
                   TextView meta, TextView note) {
            super(v);
            txtTitle = title;
            txtStatus = status;
            txtDescription = description;
            txtMeta = meta;
            txtNote = note;
        }
    }
}
