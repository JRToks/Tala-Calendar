package com.codex.tala;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ActivitySignUp extends AppCompatActivity {

    private TextInputEditText usernameInput, emailInput, pwdInput, retypepwdInput;
    private Button createAccountButton;
    private CheckBox acceptTerms;
    private ActivityResultLauncher<Intent> launcher;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        usernameInput = (TextInputEditText) findViewById(R.id.username_editText);
        emailInput = (TextInputEditText) findViewById(R.id.email_editText);
        pwdInput = (TextInputEditText) findViewById(R.id.password_editText);
        retypepwdInput = (TextInputEditText) findViewById(R.id.retypepass_editText);
        acceptTerms = (CheckBox) findViewById(R.id.terms_and_privacy_textview);
        TextView signin_btn = (TextView) findViewById(R.id.signin_btn);
        createAccountButton = (Button) findViewById(R.id.create_account);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("users");

        createAccountButton.setEnabled(false);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isNetworkAvailable(ActivitySignUp.this)){
                    if (validateInputs()) {
                        String email = emailInput.getText().toString();
                        String username = usernameInput.getText().toString();
                        String password = pwdInput.getText().toString();

                        createUser(email, password, username);
                    }
                }else{
                    Toast.makeText(ActivitySignUp.this, "An internet connection is required to create an account.", Toast.LENGTH_SHORT).show();
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

    private void createUser(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();

                                user.updateProfile(profileUpdates).addOnCompleteListener(profileUpdateTask -> {
                                    if (profileUpdateTask.isSuccessful()) {
                                        User newUser = new User(username, email);
                                        userRef.child(uid).setValue(newUser).addOnCompleteListener(databaseTask -> {
                                            if (databaseTask.isSuccessful()) {
                                                user.sendEmailVerification().addOnCompleteListener(emailVerificationTask -> {
                                                    if (emailVerificationTask.isSuccessful()) {
                                                        Toast.makeText(ActivitySignUp.this, "Verification email sent. Please check your email.", Toast.LENGTH_SHORT).show();
                                                        mAuth.signOut();
                                                        finish();
                                                    } else {
                                                        Toast.makeText(ActivitySignUp.this, "Failed to send verification email. Please try again.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(ActivitySignUp.this, "Failed to create user profile. Please try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Toast.makeText(ActivitySignUp.this, "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(ActivitySignUp.this, "This email is already registered. Please use another email.", Toast.LENGTH_SHORT).show();
                            } else if(task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                Toast.makeText(ActivitySignUp.this, "Password is too weak. Please choose a stronger password.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ActivitySignUp.this, "Authentication failed. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}