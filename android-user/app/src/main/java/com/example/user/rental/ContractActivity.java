package com.example.user.rental;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.user.R;
import com.example.user.model.Contract;
import com.example.user.network.ApiCallback;
import com.example.user.repository.RentalRepository;
import com.example.user.utils.Format;
import com.example.user.utils.SessionManager;
import com.example.user.utils.StatusUtil;
import com.example.user.utils.Ui;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;







public class ContractActivity extends AppCompatActivity {

    public static final String EXTRA_CONTRACT_ID = "contract_id";

    private TextView txtContractCode;
    private TextView txtContractStatus;
    private TextView txtContractHint;
    private TextView txtTerms;
    private TextView txtEmpty;
    private LinearLayout layoutParties;
    private LinearLayout layoutRentInfo;
    private View layoutActions;
    private View scrollContract;
    private MaterialButton btnPrimary;
    private MaterialButton btnSecondary;
    private LinearProgressIndicator progressBar;

    private final RentalRepository repository = new RentalRepository();
    private int contractId;
    private Contract contract;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract);
        Ui.setupHeader(this, "Hợp đồng thuê");

        contractId = getIntent().getIntExtra(EXTRA_CONTRACT_ID, 0);

        txtContractCode = findViewById(R.id.txtContractCode);
        txtContractStatus = findViewById(R.id.txtContractStatus);
        txtContractHint = findViewById(R.id.txtContractHint);
        txtTerms = findViewById(R.id.txtTerms);
        txtEmpty = findViewById(R.id.txtEmpty);
        layoutParties = findViewById(R.id.layoutParties);
        layoutRentInfo = findViewById(R.id.layoutRentInfo);
        layoutActions = findViewById(R.id.layoutActions);
        scrollContract = findViewById(R.id.scrollContract);
        btnPrimary = findViewById(R.id.btnPrimary);
        btnSecondary = findViewById(R.id.btnSecondary);
        progressBar = findViewById(R.id.progressBar);

        loadContract();
    }

    private void loadContract() {
        Ui.visible(progressBar, true);
        if (contractId > 0) {
            repository.getContract(contractId, new ApiCallback<Contract>() {
                @Override
                public void onSuccess(Contract data) {
                    Ui.visible(progressBar, false);
                    show(data);
                }

                @Override
                public void onError(String message) {
                    Ui.visible(progressBar, false);
                    Ui.toast(ContractActivity.this, message);
                }
            });
            return;
        }


        repository.getContracts(new ApiCallback<List<Contract>>() {
            @Override
            public void onSuccess(List<Contract> list) {
                if (list == null || list.isEmpty()) {
                    Ui.visible(progressBar, false);
                    show(null);
                    return;
                }
                Contract pick = list.get(0);
                for (Contract c : list) {
                    if ("ACTIVE".equalsIgnoreCase(c.getStatus())) {
                        pick = c;
                        break;
                    }
                }
                contractId = pick.getId();
                loadContract();
            }

            @Override
            public void onError(String message) {
                Ui.visible(progressBar, false);
                Ui.toast(ContractActivity.this, message);
            }
        });
    }

    private void show(Contract data) {
        contract = data;
        if (contract == null) {
            Ui.visible(txtEmpty, true);
            scrollContract.setVisibility(View.GONE);
            layoutActions.setVisibility(View.GONE);
            return;
        }
        Ui.visible(txtEmpty, false);
        scrollContract.setVisibility(View.VISIBLE);

        txtContractCode.setText("Hợp đồng #" + contract.getId());
        StatusUtil.apply(txtContractStatus, contract.getStatus());

        String status = contract.getStatus() == null ? "" : contract.getStatus().toUpperCase();
        if ("ACTIVE".equals(status)) {
            txtContractHint.setText("Hợp đồng đang có hiệu lực.");
        } else if ("ENDED".equals(status)) {
            txtContractHint.setText("Hợp đồng đã kết thúc.");
        } else {
            txtContractHint.setText("Hợp đồng đã bị hủy.");
        }

        SessionManager session = SessionManager.getInstance();
        layoutParties.removeAllViews();
        Ui.addRow(layoutParties, "Bên cho thuê", "N&N Home");
        Ui.addRow(layoutParties, "Bên thuê", firstNotEmpty(contract.getTenantName(), session.getFullName()));
        Ui.addRow(layoutParties, "Email", firstNotEmpty(contract.getTenantEmail(), session.getEmail()));
        Ui.addRow(layoutParties, "Số điện thoại", firstNotEmpty(contract.getTenantPhone(), session.getPhone()));

        layoutRentInfo.removeAllViews();
        Ui.addRow(layoutRentInfo, "Phòng", firstNotEmpty(contract.getRoomTitle(), "Phòng #" + contract.getRoomId()));
        Ui.addRow(layoutRentInfo, "Địa chỉ", contract.getRoomFullAddress());
        Ui.addRow(layoutRentInfo, "Ngày bắt đầu", Format.date(contract.getStartDate()));
        Ui.addRow(layoutRentInfo, "Ngày kết thúc",
                contract.getEndDate() == null ? "Không thời hạn" : Format.date(contract.getEndDate()));
        Ui.addRow(layoutRentInfo, "Tiền thuê / tháng", Format.money(contract.getMonthlyPrice()));
        Ui.addRow(layoutRentInfo, "Tiền cọc", Format.money(contract.getDepositAmount()), true);
        Ui.addRow(layoutRentInfo, "Ngày tạo", Format.date(contract.getCreatedAt()));

        txtTerms.setText(buildTerms());


        layoutActions.setVisibility(View.VISIBLE);
        btnSecondary.setText("Thanh toán");
        btnSecondary.setOnClickListener(v -> startActivity(
                new Intent(this, com.example.user.invoice.InvoiceListActivity.class)));
        btnPrimary.setText("Thông tin cọc");
        btnPrimary.setOnClickListener(v -> {
            Intent intent = new Intent(this, DepositActivity.class);
            intent.putExtra(DepositActivity.EXTRA_CONTRACT_ID, contract.getId());
            startActivity(intent);
        });
    }


    private String buildTerms() {
        return "1. Bên cho thuê đồng ý cho Bên thuê thuê phòng \"" + Format.orEmpty(contract.getRoomTitle())
                + "\" tại " + contract.getRoomFullAddress() + ".\n"
                + "2. Thời hạn từ " + Format.date(contract.getStartDate())
                + (contract.getEndDate() == null ? " (không xác định ngày kết thúc)" : " đến " + Format.date(contract.getEndDate())) + ".\n"
                + "3. Giá thuê " + Format.money(contract.getMonthlyPrice()) + "/tháng.\n"
                + "4. Tiền cọc " + Format.money(contract.getDepositAmount())
                + ", hoàn trả khi kết thúc hợp đồng nếu không có hư hại.\n"
                + "5. Các khoản thanh toán được ghi nhận và xác nhận bởi chủ nhà trên hệ thống.";
    }

    private static String firstNotEmpty(String a, String b) {
        return TextUtils.isEmpty(a) ? Format.orEmpty(b) : a;
    }
}
