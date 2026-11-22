package com.example.user.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.TextView;




public final class StatusUtil {

    private static final String GREEN = "#1E9E5A";
    private static final String ORANGE = "#E08A00";
    private static final String RED = "#D93A3A";
    private static final String BLUE = "#2F6FDE";
    private static final String GRAY = "#8B8992";
    private static final String PINK = "#E75491";

    private StatusUtil() {
    }

    public static String label(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {

            case "AVAILABLE": return "Còn trống";
            case "RESERVED": return "Đang giữ chỗ";
            case "RENTED": return "Đã cho thuê";
            case "INACTIVE": return "Ngừng cho thuê";
            case "FAILED": return "Thất bại";
            case "REFUNDED": return "Đã hoàn tiền";
            case "BLOCKED": return "Bị khóa";

            case "PENDING": return "Chờ xử lý";
            case "CONFIRMED": return "Đã xác nhận";
            case "REJECTED": return "Bị từ chối";
            case "CANCELLED": return "Đã hủy";
            case "DONE": return "Hoàn thành";
            case "IN_PROGRESS": return "Đang xử lý";
            case "APPROVED": return "Đã duyệt";
            case "COMPLETED": return "Đã hoàn tất";

            case "PENDING_SIGN": return "Chờ bạn xác nhận";
            case "SIGNED": return "Chờ đặt cọc";
            case "ACTIVE": return "Đang thuê";
            case "ENDED": return "Đã kết thúc";

            case "UNPAID": return "Chưa thanh toán";
            case "PENDING_CONFIRM": return "Chờ chủ nhà xác nhận";
            case "PAID": return "Đã thanh toán";
            default: return status;
        }
    }

    public static String color(String status) {
        if (status == null) {
            return GRAY;
        }
        switch (status) {
            case "AVAILABLE":
            case "CONFIRMED":
            case "ACTIVE":
            case "PAID":
            case "DONE":
            case "APPROVED":
            case "COMPLETED":
                return GREEN;
            case "PENDING":
            case "PENDING_CONFIRM":
            case "SIGNED":
            case "RESERVED":
                return ORANGE;
            case "IN_PROGRESS":
                return BLUE;
            case "PENDING_SIGN":
                return PINK;
            case "UNPAID":
            case "REJECTED":
            case "FAILED":
            case "BLOCKED":
                return RED;
            case "REFUNDED":
                return BLUE;
            default:
                return GRAY;
        }
    }


    public static void apply(TextView view, String status) {
        int c = Color.parseColor(color(status));
        view.setText(label(status));
        view.setTextColor(c);
        view.setBackgroundTintList(ColorStateList.valueOf(
                Color.argb(32, Color.red(c), Color.green(c), Color.blue(c))));
    }


    public static String priority(String p) {
        if ("HIGH".equals(p)) return "Khẩn cấp";
        if ("LOW".equals(p)) return "Thấp";
        return "Bình thường";
    }


    public static String paymentType(String t) {
        if ("DEPOSIT".equals(t)) return "Tiền cọc";
        if ("RENT".equals(t)) return "Tiền thuê";
        return "Khoản khác";
    }
}
