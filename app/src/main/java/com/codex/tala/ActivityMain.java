package com.codex.tala;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDate;
import java.util.Objects;

public class ActivityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private SessionManager sessionManager;
    private FragmentManager fragmentManager;
    private DrawerLayout drawerLayout;
    private FABHandler FAB;
    private String userId, rememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rememberMe = getIntent().getStringExtra("rememberMe");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        sessionManager = new SessionManager(ActivityMain.this);
        FAB = new FABHandler(this);

        if (currentUser != null ){
            userId = currentUser.getUid();
            fragmentManager = getSupportFragmentManager();
            CalendarUtils.selectedDate = (LocalDate) LocalDate.now();
            openFragment(new MonthFragment(userId));

            setNavHeaders();
            setupBtnClickListeners();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean isLoggedIn = sessionManager.isLoggedIn();
        if (!isLoggedIn && rememberMe == null){ // if current user is null it means no user is logged in
            if (currentUser != null){
                mAuth.signOut();
            }
            Intent i = new Intent(ActivityMain.this, ActivityLogin.class);
            startActivity(i);
            finish();
        }
    }

    private void setNavHeaders() {
        NavigationView navigationView = findViewById(R.id.nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);

        TextView textViewUsername = headerView.findViewById(R.id.nav_username);
        TextView textViewEmail = headerView.findViewById(R.id.nav_email);

        textViewUsername.setText(currentUser.getDisplayName());
        textViewEmail.setText(currentUser.getEmail());
    }

    private void setupBtnClickListeners() {
        FloatingActionButton add_btn = findViewById(R.id.add_btn);
        FloatingActionButton add_cal = findViewById(R.id.event_shortcut_btn);
        FloatingActionButton talk_ai = findViewById(R.id.talk_ai_btn);
        View dimView = findViewById(R.id.dimView);
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FAB.onButtonClicked();
            }
        });
        add_cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FAB.onButtonClicked();
                Intent intent = new Intent(ActivityMain.this, ActivityEventAdd.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up_anim,0);
            }
        });
        talk_ai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FAB.onButtonClicked();
                Intent intent = new Intent(ActivityMain.this, ActivityAI.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up_anim,0);
            }
        });

        dimView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FAB.onButtonClicked();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.month_view) {
            openFragment(new MonthFragment(userId));
        } else if (itemId == R.id.day_view) {
            openFragment(new DayFragment(userId));
        } else if (itemId == R.id.about_us) {
            Intent intent = new Intent(ActivityMain.this, ActivityAboutUs.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left,0);
        } else if (itemId == R.id.logout_btn) {
            mAuth.signOut();
            sessionManager.removeSession();
            Intent i = new Intent(ActivityMain.this, ActivityLogin.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
