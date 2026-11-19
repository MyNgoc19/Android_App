package com.example.user.utils;

import android.app.Activity;
import android.content.Intent;

import com.example.user.R;
import com.example.user.appointment.MyAppointmentsActivity;
import com.example.user.auth.ProfileActivity;
import com.example.user.room.HomeActivity;
import com.example.user.room.SearchActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Dùng chung thanh điều hướng dưới cho 4 tab: Trang chủ, Tìm phòng, Lịch hẹn, Cá nhân.
 * REORDER_TO_FRONT giúp không tạo chồng nhiều màn hình giống nhau.
 */
public final class BottomNavHelper {

    private BottomNavHelper() {
    }

    public static void setup(Activity activity, BottomNavigationView nav, int selectedItemId) {
        nav.getMenu().findItem(selectedItemId).setChecked(true);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) {
                return true;
            }

            Class<?> target = null;
            if (id == R.id.navHome) {
                target = HomeActivity.class;
            } else if (id == R.id.navSearch) {
                target = SearchActivity.class;
            } else if (id == R.id.navAppointment) {
                target = MyAppointmentsActivity.class;
            } else if (id == R.id.navProfile) {
                target = ProfileActivity.class;
            }

            if (target != null) {
                Intent intent = new Intent(activity, target);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
            return false;
        });
    }

    /** Gọi trong onResume để tô lại đúng tab khi quay về */
    public static void refresh(BottomNavigationView nav, int selectedItemId) {
        if (nav != null) {
            nav.getMenu().findItem(selectedItemId).setChecked(true);
        }
    }
}
