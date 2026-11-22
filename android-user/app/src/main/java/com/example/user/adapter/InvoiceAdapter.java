package com.example.user.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.user.R;
import com.example.user.model.Payment;
import com.example.user.utils.Format;
import com.example.user.utils.StatusUtil;

import java.util.ArrayList;
import java.util.List;




public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.ViewHolder> {

    public interface Listener {
        void onInvoiceClick(Payment payment);
    }

    private final List<Payment> items = new ArrayList<>();
    private final Listener listener;

    public InvoiceAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Payment> list) {
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
                .inflate(R.layout.item_invoice, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Payment p = items.get(position);

        String title = StatusUtil.paymentType(p.getPaymentType());
        if (!TextUtils.isEmpty(p.getPaymentPeriod())) {
            title += " • Kỳ " + Format.period(p.getPaymentPeriod());
        }
        h.txtMonth.setText(title);
        h.txtRoom.setText(TextUtils.isEmpty(p.getRoomTitle())
                ? "Hợp đồng #" + p.getContractId()
                : p.getRoomTitle());
        h.txtUsage.setText(TextUtils.isEmpty(p.getNote()) ? "Mã thanh toán #" + p.getId() : p.getNote());
        h.txtDue.setText(p.getPaidAt() != null
                ? "Đã trả: " + Format.date(p.getPaidAt())
                : "Tạo ngày: " + Format.date(p.getCreatedAt()));
        h.txtTotal.setText(Format.money(p.getAmount()));
        StatusUtil.apply(h.txtStatus, p.getStatus());
        h.itemView.setOnClickListener(v -> listener.onInvoiceClick(p));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtMonth;
        final TextView txtStatus;
        final TextView txtRoom;
        final TextView txtUsage;
        final TextView txtDue;
        final TextView txtTotal;

        ViewHolder(@NonNull View v) {
            super(v);
            txtMonth = v.findViewById(R.id.txtInvoiceMonth);
            txtStatus = v.findViewById(R.id.txtInvoiceStatus);
            txtRoom = v.findViewById(R.id.txtInvoiceRoom);
            txtUsage = v.findViewById(R.id.txtInvoiceUsage);
            txtDue = v.findViewById(R.id.txtInvoiceDue);
            txtTotal = v.findViewById(R.id.txtInvoiceTotal);
        }
    }
}
