package com.example.user.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.user.R;

/**
 * Tải ảnh phòng bằng Glide, có kiểm tra Activity còn sống để tránh crash.
 */
public final class ImageLoader {

    private ImageLoader() {
    }

    public static void load(ImageView imageView, String url) {
        Context context = imageView.getContext();
        while (context instanceof ContextWrapper && !(context instanceof Activity)) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
        }
        Glide.with(imageView)
                .load(url)
                .placeholder(R.drawable.bg_room_placeholder)
                .error(R.drawable.bg_room_placeholder)
                .centerCrop()
                .into(imageView);
    }
}
