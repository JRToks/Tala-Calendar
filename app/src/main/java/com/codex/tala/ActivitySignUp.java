package com.codex.tala;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class ActivitySignUp extends AppCompatActivity {

    private TextInputEditText usernameInput, emailInput, pwdInput, retypepwdInput;
    private Button createAccountButton;
    private CheckBox acceptTerms;
    private ActivityResultLauncher<Intent> launcher;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        usernameInput = (TextInputEditText) findViewById(R.id.username_editText);
        emailInput = (TextInputEditText) findViewById(R.id.email_editText);
        pwdInput = (TextInputEditText) findViewById(R.id.password_editText);
        retypepwdInput = (TextInputEditText) findViewById(R.id.retypepass_editText);
        acceptTerms = (CheckBox) findViewById(R.id.terms_and_privacy_textview);
        TextView signin_btn = (TextView) findViewById(R.id.signin_btn);
        createAccountButton = (Button) findViewById(R.id.create_account);
        db = new DBHelper(this);

        createAccountButton.setEnabled(false);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    String email = emailInput.getText().toString();
                    String username = usernameInput.getText().toString();
                    String password = pwdInput.getText().toString();

                    Boolean checkemail = db.checkemail(email); //runs the checkemail function in the dbhelper class
                    if (!checkemail) {
                        Boolean insert = db.insertUsersData(email, username, password); //runs the insertdata function in the dbhelper class
                        if (insert) {
                            finish();
                        } else {
                            Toast.makeText(ActivitySignUp.this, "Something went wrong. Please try again later", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ActivitySignUp.this, "Email already exist! Please enter a different email", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitySignUp.this, ActivityLogin.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        acceptTerms.setChecked(true);
                        createAccountButton.setEnabled(true);
                    }else{
                        acceptTerms.setChecked(false);
                        createAccountButton.setEnabled(false);
                    }
                }
        );

        acceptTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Intent intent = new Intent(ActivitySignUp.this, ActivityLegalAgreements.class);
                launcher.launch(intent);
            }else{
                createAccountButton.setEnabled(false);
            }
        });
    }

    private boolean validateInputs() {
        if (usernameInput.getText().toString().isEmpty() ||
                emailInput.getText().toString().isEmpty() ||
                pwdInput.getText().toString().isEmpty() ||
                retypepwdInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput.getText().toString()).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!pwdInput.getText().toString().equals(retypepwdInput.getText().toString())) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}