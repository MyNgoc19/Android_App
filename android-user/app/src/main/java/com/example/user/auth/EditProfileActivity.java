
package com.example.user.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.user.R;
import com.example.user.model.User;
import com.example.user.network.ApiCallback;
import com.example.user.repository.AuthRepository;
import com.example.user.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditProfileActivity extends AppCompatActivity {

    // Thông tin giao diện
    private TextView btnBack;
    private TextView btnChangeAvatar;
    private ImageView imgEditAvatar;

    private TextInputLayout layoutFullName;
    private TextInputLayout layoutPhone;

    private TextInputEditText edtFullName;
    private TextInputEditText edtEmail;
    private TextInputEditText edtPhone;

    private RadioGroup radioGroupGender;
    private RadioButton radioFemale;
    private RadioButton radioMale;
    private RadioButton radioOther;

    private MaterialButton btnSaveProfile;

    // Dữ liệu mẫu lưu trên thiết bị
    private SharedPreferences preferences;
    private String avatarUri = "";

    private final AuthRepository authRepository = new AuthRepository();

    // Chọn ảnh đại diện
    private final ActivityResultLauncher<Intent> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null
                                && result.getData().getData() != null) {

                            Uri uri = result.getData().getData();

                            try {
                                getContentResolver().takePersistableUriPermission(
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );

                                avatarUri = uri.toString();
                                showAvatar(uri);

                            } catch (SecurityException e) {
                                Toast.makeText(
                                        this,
                                        "Không thể truy cập ảnh đã chọn",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        preferences = getSharedPreferences(
                "NN_HOME_PROFILE",
                MODE_PRIVATE
        );

        initViews();
        loadProfile();
        setupEvents();
    }

    // Ánh xạ các thành phần giao diện
    private void initViews() {

        btnBack = findViewById(R.id.btnBack);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        imgEditAvatar = findViewById(R.id.imgEditAvatar);

        layoutFullName = findViewById(R.id.layoutFullName);
        layoutPhone = findViewById(R.id.layoutPhone);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);

        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioFemale = findViewById(R.id.radioFemale);
        radioMale = findViewById(R.id.radioMale);
        radioOther = findViewById(R.id.radioOther);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
    }

    // Hiển thị thông tin cá nhân đã lưu
    private void loadProfile() {

        // Hiện ngay thông tin đã lưu trong phiên đăng nhập
        SessionManager session = SessionManager.getInstance();
        bindUser(session.getFullName(), session.getEmail(), session.getPhone(), session.getGender());

        // Ảnh đại diện chỉ lưu trên máy
        avatarUri = preferences.getString("avatar_uri", "");
        if (!avatarUri.isEmpty()) {
            showAvatar(Uri.parse(avatarUri));
        } else {
            showDefaultAvatar();
        }

        // Lấy bản mới nhất từ server
        authRepository.getProfile(new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                bindUser(user.getFullName(), user.getEmail(), user.getPhone(),
                        SessionManager.getInstance().getGender());
            }

            @Override
            public void onError(String message) {
                Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindUser(String fullName, String email, String phone, String gender) {

        edtFullName.setText(fullName);
        edtEmail.setText(email);
        edtPhone.setText(phone);
        if (gender == null) {
            gender = "";
        }

        // Không cho chỉnh sửa email
        edtEmail.setEnabled(false);

        // Hiển thị giới tính
        if (gender.equals("Nữ")) {
            radioFemale.setChecked(true);
        } else if (gender.equals("Nam")) {
            radioMale.setChecked(true);
        } else if (gender.equals("Khác")) {
            radioOther.setChecked(true);
        } else {
            radioGroupGender.clearCheck();
        }

    }

    // Xử lý sự kiện
    private void setupEvents() {

        // Quay lại Profile
        btnBack.setOnClickListener(v -> finish());

        // Thay đổi ảnh đại diện
        btnChangeAvatar.setOnClickListener(v -> selectAvatar());

        imgEditAvatar.setOnClickListener(v -> selectAvatar());

        // Lưu thông tin
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    // Mở thư viện ảnh
    private void selectAvatar() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        imagePicker.launch(intent);
    }

    // Hiển thị ảnh đại diện đã chọn
    private void showAvatar(Uri uri) {

        try {
            imgEditAvatar.setImageURI(null);
            imgEditAvatar.setImageURI(uri);

            imgEditAvatar.setPadding(0, 0, 0, 0);
            imgEditAvatar.setImageTintList(null);

        } catch (Exception e) {
            showDefaultAvatar();
        }
    }

    // Hiển thị ảnh mặc định
    private void showDefaultAvatar() {

        imgEditAvatar.setImageResource(R.drawable.ic_user);

        imgEditAvatar.setPadding(25, 25, 25, 25);
        imgEditAvatar.setImageTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#E75491")
                )
        );
    }

    // Kiểm tra và lưu thông tin
    private void saveProfile() {

        String fullName = getText(edtFullName);
        String phone = getText(edtPhone);

        layoutFullName.setError(null);
        layoutPhone.setError(null);

        // Kiểm tra họ tên
        if (fullName.isEmpty()) {

            layoutFullName.setError("Vui lòng nhập họ và tên");
            edtFullName.requestFocus();
            return;
        }

        // Kiểm tra số điện thoại
        if (!phone.matches("0[0-9]{9}")) {

            layoutPhone.setError(
                    "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0"
            );

            edtPhone.requestFocus();
            return;
        }

        // Kiểm tra giới tính
        int selectedGenderId =
                radioGroupGender.getCheckedRadioButtonId();

        if (selectedGenderId == -1) {

            Toast.makeText(
                    this,
                    "Vui lòng chọn giới tính",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        RadioButton selectedGender =
                findViewById(selectedGenderId);

        String gender =
                selectedGender.getText().toString();

        // Ảnh đại diện lưu trên máy, thông tin còn lại gửi lên server
        preferences.edit()
                .putString("avatar_uri", avatarUri)
                .apply();

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Đang lưu...");

        // Giới tính backend chưa có => lưu trên máy; họ tên + SĐT gửi PATCH users/me
        SessionManager.getInstance().saveGender(gender);

        authRepository.updateProfile(fullName, phone, new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(EditProfileActivity.this,
                        "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                btnSaveProfile.setEnabled(true);
                btnSaveProfile.setText("Lưu thay đổi");
                Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Lấy nội dung ô nhập
    private String getText(TextInputEditText editText) {

        if (editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }
}
