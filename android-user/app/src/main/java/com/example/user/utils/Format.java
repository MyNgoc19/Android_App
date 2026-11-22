package com.example.user.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;




public final class Format {

    private static final Locale VN = new Locale("vi", "VN");

    private Format() {
    }


    public static String money(double amount) {
        return NumberFormat.getNumberInstance(VN).format(Math.round(amount)) + " đ";
    }

    public static String money(Double amount) {
        return amount == null ? "" : money(amount.doubleValue());
    }


    public static String pricePerMonth(double price) {
        if (price >= 1_000_000) {
            double million = price / 1_000_000.0;
            String text = (million == Math.floor(million))
                    ? String.valueOf((long) million)
                    : String.format(VN, "%.1f", million);
            return text + " triệu/tháng";
        }
        return money(price) + "/tháng";
    }


    public static String area(Double area) {
        if (area == null) {
            return "—";
        }
        double a = area;
        if (a == Math.floor(a)) {
            return (long) a + " m²";
        }
        return a + " m²";
    }


    public static String date(String isoDate) {
        if (isoDate == null || isoDate.length() < 10) {
            return isoDate == null ? "" : isoDate;
        }
        String d = isoDate.substring(0, 10);
        return d.substring(8, 10) + "/" + d.substring(5, 7) + "/" + d.substring(0, 4);
    }


    public static String month(String yearMonth) {
        if (yearMonth == null || yearMonth.length() < 7) {
            return "";
        }
        return "Tháng " + yearMonth.substring(5, 7) + "/" + yearMonth.substring(0, 4);
    }


    public static String toApiDate(Calendar calendar) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
    }


    public static String addMonths(String isoDate, int months) {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date d = f.parse(isoDate);
            Calendar c = Calendar.getInstance();
            if (d != null) {
                c.setTime(d);
            }
            c.add(Calendar.MONTH, months);
            return date(f.format(c.getTime()));
        } catch (ParseException e) {
            return "";
        }
    }

    public static String roomType(boolean apartment) {
        return apartment ? "Căn hộ" : "Phòng trọ";
    }


    public static String period(String value) {
        if (value != null && value.matches("\\d{4}-\\d{2}")) {
            return value.substring(5, 7) + "/" + value.substring(0, 4);
        }
        return value == null ? "" : value;
    }

    public static String orEmpty(String s) {
        return s == null ? "" : s;
    }
}
