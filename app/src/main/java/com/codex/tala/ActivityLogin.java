package com.codex.tala;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ActivityLogin extends AppCompatActivity {
    private EditText mail, pass;
    private boolean rememberCond;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mail = (EditText) findViewById(R.id.email_editText);
        pass = (EditText) findViewById(R.id.password_editText);
        TextView forgotPass = (TextView) findViewById(R.id.forgotPasswordTextView);
        TextView signUp = (TextView) findViewById(R.id.signUp);
        Switch rememberMe = (Switch) findViewById(R.id.rememberSwitch);
        Button loginBtn = (Button) findViewById(R.id.login_Btn);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("users");

        rememberCond = false;
        rememberMe.setOnCheckedChangeListener((buttonView, isChecked) -> rememberCond = isChecked);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mail.getText().toString().trim();
                if (!email.equals("")){
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ActivityLogin.this, "Email to reset password has been sent.", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(ActivityLogin.this, "Please enter a valid email.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else{
                    mail.setError("Please enter a valid email.");
                }
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityLogin.this, ActivitySignUp.class);
                startActivity(intent);
            }
        });
    }

    private void login() {
        String email = mail.getText().toString().trim();
        String pwd = pass.getText().toString();

        if (email.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(ActivityLogin.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(ActivityLogin.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Query checkUserDB = userRef.orderByChild("email").equalTo(email);
                            checkUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String userid = user.getUid();

                                    if (!snapshot.exists()){
                                        User newUser = new User(user.getDisplayName(), email);
                                        userRef.child(userid).setValue(newUser);
                                    }

                                    if (user.isEmailVerified()){
                                        if (rememberCond) {
                                            SessionManager sessionManager = new SessionManager(ActivityLogin.this);
                                            sessionManager.saveSession(userid);
                                        }
                                        Intent intent = new Intent(ActivityLogin.this, ActivityMain.class);
                                        intent.putExtra("rememberMe", "true");
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        Toast.makeText(ActivityLogin.this, "Please verify your email address before logging in.", Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            Toast.makeText(ActivityLogin.this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show();
                            mail.requestFocus();
                        }
                    }
                });
    }
}