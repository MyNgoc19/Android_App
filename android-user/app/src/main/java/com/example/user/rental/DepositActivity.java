package com.example.user.rental;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.user.R;
import com.example.user.model.Contract;
import com.example.user.model.Payment;
import com.example.user.network.ApiCallback;
import com.example.user.repository.RentalRepository;
import com.example.user.utils.Format;
import com.example.user.utils.StatusUtil;
import com.example.user.utils.Ui;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;






public class DepositActivity extends AppCompatActivity {

    public static final String EXTRA_CONTRACT_ID = "contract_id";


    private static final String BANK_NAME = "Liên hệ chủ nhà để nhận STK";
    private static final String BANK_ACCOUNT = "—";
    private static final String ACCOUNT_HOLDER = "N&N HOME";

    private TextView txtDepositAmount;
    private TextView txtDepositStatus;
    private TextView txtDepositHint;
    private TextView txtBankName;
    private TextView txtBankAccount;
    private TextView txtAccountHolder;
    private TextView txtTransferContent;
    private View layoutContent;
    private View layoutConfirm;
    private MaterialButton btnConfirmDeposit;
    private LinearProgressIndicator progressBar;

    private final RentalRepository repository = new RentalRepository();
    private int contractId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);
        Ui.setupHeader(this, "Thông tin tiền cọc");

        contractId = getIntent().getIntExtra(EXTRA_CONTRACT_ID, 0);

        txtDepositAmount = findViewById(R.id.txtDepositAmount);
        txtDepositStatus = findViewById(R.id.txtDepositStatus);
        txtDepositHint = findViewById(R.id.txtDepositHint);
        txtBankName = findViewById(R.id.txtBankName);
        txtBankAccount = findViewById(R.id.txtBankAccount);
        txtAccountHolder = findViewById(R.id.txtAccountHolder);
        txtTransferContent = findViewById(R.id.txtTransferContent);
        layoutContent = findViewById(R.id.layoutContent);
        layoutConfirm = findViewById(R.id.layoutConfirm);
        btnConfirmDeposit = findViewById(R.id.btnConfirmDeposit);
        progressBar = findViewById(R.id.progressBar);


        Ui.visible(layoutConfirm, false);
        Ui.visible(btnConfirmDeposit, false);
        layoutContent.setVisibility(View.INVISIBLE);

        txtBankName.setText(BANK_NAME);
        txtBankAccount.setText(BANK_ACCOUNT);
        txtAccountHolder.setText(ACCOUNT_HOLDER);
        txtTransferContent.setText("COC HD" + contractId);

        findViewById(R.id.btnCopy).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("Nội dung chuyển khoản",
                    txtTransferContent.getText()));
            Ui.toast(this, "Đã sao chép nội dung chuyển khoản");
        });

        loadDeposit();
    }

    private void loadDeposit() {
        Ui.visible(progressBar, true);
        repository.getContract(contractId, new ApiCallback<Contract>() {
            @Override
            public void onSuccess(Contract contract) {
                Ui.visible(progressBar, false);
                layoutContent.setVisibility(View.VISIBLE);
                txtDepositAmount.setText(Format.money(contract.getDepositAmount()));


                double paid = 0;
                Payment latest = null;
                StringBuilder history = new StringBuilder();
                if (contract.getPayments() != null) {
                    for (Payment p : contract.getPayments()) {
                        if (!"DEPOSIT".equalsIgnoreCase(p.getPaymentType())) continue;
                        if ("PAID".equalsIgnoreCase(p.getStatus())) paid += p.getAmount();
                        if (latest == null || p.getId() > latest.getId()) latest = p;
                        history.append("\n• Khoản #").append(p.getId()).append(": ")
                                .append(Format.money(p.getAmount())).append(" - ")
                                .append(StatusUtil.label(p.getStatus()))
                                .append(p.getNote() == null || p.getNote().isEmpty() ? "" : " (" + p.getNote() + ")");
                    }
                }

                double need = contract.getDepositAmount();
                if (need > 0 && paid >= need) {
                    StatusUtil.apply(txtDepositStatus, "PAID");
                } else if (paid > 0) {
                    StatusUtil.apply(txtDepositStatus, "PENDING");
                    txtDepositStatus.setText("Đã đóng " + Format.money(paid));
                } else if (latest != null && "PENDING".equalsIgnoreCase(latest.getStatus())) {
                    StatusUtil.apply(txtDepositStatus, "PENDING");
                } else {
                    StatusUtil.apply(txtDepositStatus, "UNPAID");
                }

                String hint = "Đã đóng " + Format.money(paid) + " / " + Format.money(need);
                hint += history.length() == 0
                        ? "\nChủ nhà chưa ghi nhận khoản cọc nào."
                        : "\nCác khoản cọc:" + history;
                txtDepositHint.setText(hint);
            }

            @Override
            public void onError(String message) {
                Ui.visible(progressBar, false);
                Ui.toast(DepositActivity.this, message);
            }
        });
    }
}
