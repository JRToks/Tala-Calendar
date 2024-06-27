package com.codex.tala;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityLegalAgreements extends AppCompatActivity {
    private CheckBox acceptTerms;
    private CheckBox acceptPolicy;
    private Button acceptBtn;
    private Button cancelBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_agreements);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        acceptTerms = findViewById(R.id.checkbox_accept_terms);
        acceptPolicy = findViewById(R.id.checkbox_accept_policy);
        acceptBtn = findViewById(R.id.sign_up_accept);
        cancelBtn = findViewById(R.id.sign_up_cancel);

        acceptBtn.setEnabled(false);

        acceptTerms.setOnCheckedChangeListener((buttonView, isChecked) -> updateAcceptButtonState());
        acceptPolicy.setOnCheckedChangeListener((buttonView, isChecked) -> updateAcceptButtonState());

        acceptBtn.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });

        cancelBtn.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

    }

    private void updateAcceptButtonState() {
        if (acceptTerms.isChecked() && acceptPolicy.isChecked()) {
            acceptBtn.setEnabled(true);
        } else {
            acceptBtn.setEnabled(false);
        }
    }
}
