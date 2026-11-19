package com.example.user.utils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

/**
 * Các hàm giao diện dùng chung để code Activity ngắn gọn hơn.
 */
public final class Ui {

    public interface OnDatePicked {
        void onPicked(Calendar calendar);
    }

    /** Số điện thoại liên hệ chủ nhà (backend chưa có trường này) – đổi theo nhóm bạn */
    public static final String HOTLINE = "0909000000";

    private Ui() {
    }

    /** Gắn tiêu đề + nút quay lại cho layout_header */
    public static void setupHeader(Activity activity, String title) {
        TextView txtTitle = activity.findViewById(R.id.txtHeaderTitle);
        View btnBack = activity.findViewById(R.id.btnBack);
        if (txtTitle != null) {
            txtTitle.setText(title);
        }
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> activity.finish());
        }
    }

    /** Thêm 1 dòng "Nhãn : Giá trị" vào container */
    public static TextView addRow(LinearLayout container, String label, String value) {
        return addRow(container, label, value, false);
    }

    public static TextView addRow(LinearLayout container, String label, String value, boolean highlight) {
        Context context = container.getContext();
        int pad = dp(context, 7);

        // Tạo dòng bằng code => không cần thêm file layout
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, pad, 0, pad);

        TextView txtLabel = new TextView(context);
        txtLabel.setText(label);
        txtLabel.setTextSize(14);
        txtLabel.setTextColor(Color.parseColor("#777777"));
        row.addView(txtLabel, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView txtValue = new TextView(context);
        txtValue.setText(value == null ? "" : value);
        txtValue.setTextSize(14);
        txtValue.setTextColor(context.getColor(R.color.nn_text));
        txtValue.setGravity(Gravity.END);
        LinearLayout.LayoutParams valueParams =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.4f);
        valueParams.setMarginStart(dp(context, 12));
        row.addView(txtValue, valueParams);

        if (highlight) {
            txtLabel.setTypeface(Typeface.DEFAULT_BOLD);
            txtLabel.setTextColor(context.getColor(R.color.nn_text));
            txtValue.setTypeface(Typeface.DEFAULT_BOLD);
            txtValue.setTextColor(context.getColor(R.color.nn_primary));
            txtValue.setTextSize(16);
        }

        container.addView(row);
        return txtValue;
    }

    public static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void visible(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public static String text(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    /**
     * Mở hộp chọn ngày, không cho chọn ngày trước minDaysFromToday.
     */
    public static void pickDate(Context context, Calendar current, int minDaysFromToday,
                                OnDatePicked listener) {
        DatePickerDialog dialog = new DatePickerDialog(
                context,
                (view, year, month, day) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(year, month, day, 0, 0, 0);
                    listener.onPicked(picked);
                },
                current.get(Calendar.YEAR),
                current.get(Calendar.MONTH),
                current.get(Calendar.DAY_OF_MONTH)
        );
        Calendar min = Calendar.getInstance();
        min.add(Calendar.DAY_OF_MONTH, minDaysFromToday);
        dialog.getDatePicker().setMinDate(min.getTimeInMillis() - 1000);
        dialog.show();
    }
}
