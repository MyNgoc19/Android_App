package com.example.user.room;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.user.R;
import com.example.user.adapter.MaintenanceAdapter;
import com.example.user.model.MaintenanceRequest;
import com.example.user.network.ApiCallback;
import com.example.user.repository.RentalRepository;
import com.example.user.utils.Ui;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;






public class MaintenanceActivity extends AppCompatActivity {

    private TextInputLayout layoutTitle;
    private TextInputLayout layoutDescription;
    private TextInputEditText edtTitle;
    private TextInputEditText edtDescription;
    private ChipGroup chipGroupPriority;
    private MaterialButton btnSubmit;
    private TextView txtEmpty;
    private LinearProgressIndicator progressBar;

    private final MaintenanceAdapter adapter = new MaintenanceAdapter();
    private final RentalRepository repository = new RentalRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);
        Ui.setupHeader(this, "Yêu cầu sửa chữa");

        layoutTitle = findViewById(R.id.layoutTitle);
        layoutDescription = findViewById(R.id.layoutDescription);
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        chipGroupPriority = findViewById(R.id.chipGroupPriority);
        btnSubmit = findViewById(R.id.btnSubmit);
        txtEmpty = findViewById(R.id.txtEmpty);
        progressBar = findViewById(R.id.progressBar);

        RecyclerView rv = findViewById(R.id.rvMaintenance);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        btnSubmit.setOnClickListener(v -> submit());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRequests();
    }

    private void loadRequests() {
        Ui.visible(progressBar, true);
        repository.getMaintenanceRequests(new ApiCallback<List<MaintenanceRequest>>() {
            @Override
            public void onSuccess(List<MaintenanceRequest> data) {
                Ui.visible(progressBar, false);
                adapter.setItems(data);
                Ui.visible(txtEmpty, data.isEmpty());
            }

            @Override
            public void onError(String message) {
                Ui.visible(progressBar, false);
                Ui.toast(MaintenanceActivity.this, message);
            }
        });
    }

    private String selectedPriority() {
        int id = chipGroupPriority.getCheckedChipId();
        if (id == R.id.chipLow) return "LOW";
        if (id == R.id.chipHigh) return "HIGH";
        return "MEDIUM";
    }

    private void submit() {
        String title = Ui.text(edtTitle);
        String description = Ui.text(edtDescription);
        layoutTitle.setError(null);
        layoutDescription.setError(null);

        if (title.isEmpty()) {
            layoutTitle.setError("Vui lòng nhập vấn đề cần sửa");
            return;
        }
        if (description.length() < 5) {
            layoutDescription.setError("Mô tả rõ hơn giúp chủ nhà xử lý nhanh hơn");
            return;
        }

        btnSubmit.setEnabled(false);
        repository.createMaintenanceRequest(title, description, selectedPriority(),
                new ApiCallback<MaintenanceRequest>() {
                    @Override
                    public void onSuccess(MaintenanceRequest data) {
                        btnSubmit.setEnabled(true);
                        Ui.toast(MaintenanceActivity.this, "Đã gửi yêu cầu sửa chữa, chủ nhà sẽ phản hồi sớm");
                        edtTitle.setText("");
                        edtDescription.setText("");
                        chipGroupPriority.check(R.id.chipMedium);
                        loadRequests();
                    }

                    @Override
                    public void onError(String message) {
                        btnSubmit.setEnabled(true);
                        Ui.toast(MaintenanceActivity.this, message);
                    }
                });
    }
}
