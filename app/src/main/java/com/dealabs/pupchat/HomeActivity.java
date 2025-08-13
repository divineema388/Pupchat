package com.dealabs.pupchat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Navigation constants
    private static final int NAV_HOME = R.id.nav_home;
    private static final int NAV_PROFILE = R.id.nav_profile;
    private static final int NAV_MESSAGES = R.id.nav_messages;
    private static final int NAV_NOTIFICATIONS = R.id.nav_notifications;
    private static final int NAV_ACCOUNT_SETTINGS = R.id.nav_account_settings;
    private static final int NAV_PRIVACY = R.id.nav_privacy;
    private static final int NAV_LOGOUT = R.id.nav_logout;
    private static final int NAV_HELP = R.id.nav_help;
    private static final int NAV_ABOUT = R.id.nav_about;

    private TextView textViewWelcome;
    private TextView textViewEmail;
    private EditText editTextPost;
    private LinearLayout linearLayoutPosts;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userName = "";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewEmail = findViewById(R.id.textViewEmail);
        editTextPost = findViewById(R.id.editTextPost);
        linearLayoutPosts = findViewById(R.id.linearLayoutPosts);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Set up navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Set up post button
        findViewById(R.id.buttonPost).setOnClickListener(v -> createPost());

        loadUserData();
        loadPosts();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == NAV_HOME) {
            // Already home
        } else if (id == NAV_PROFILE) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == NAV_MESSAGES) {
            startActivity(new Intent(this, MessagesActivity.class));
        } else if (id == NAV_NOTIFICATIONS) {
            startActivity(new Intent(this, NotificationsActivity.class));
        } else if (id == NAV_ACCOUNT_SETTINGS) {
            startActivity(new Intent(this, AccountSettingsActivity.class));
        } else if (id == NAV_PRIVACY) {
            startActivity(new Intent(this, PrivacyActivity.class));
        } else if (id == NAV_LOGOUT) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else if (id == NAV_HELP) {
            startActivity(new Intent(this, HelpActivity.class));
        } else if (id == NAV_ABOUT) {
            startActivity(new Intent(this, AboutActivity.class));
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
        }
    }

    private void createPost() {
        String postContent = editTextPost.getText().toString().trim();
        if (TextUtils.isEmpty(postContent)) {
            Toast.makeText(this, "Please write something to post", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String userEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "Unknown";
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

            Map<String, Object> postValues = new HashMap<>();
            postValues.put("content", postContent);
            postValues.put("userId", userId);
            postValues.put("userName", userName);
            postValues.put("userEmail", userEmail);
            postValues.put("timestamp", timestamp);

            String postId = mDatabase.child("Posts").push().getKey();
            if (postId != null) {
                mDatabase.child("Posts").child(postId).setValue(postValues)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                editTextPost.setText("");
                                Toast.makeText(HomeActivity.this, "Posted successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(HomeActivity.this, "Failed to post", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private void loadPosts() {
        mDatabase.child("Posts").orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                linearLayoutPosts.removeAllViews();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String content = postSnapshot.child("content").getValue(String.class);
                    String userName = postSnapshot.child("userName").getValue(String.class);
                    String timestamp = postSnapshot.child("timestamp").getValue(String.class);

                    if (content != null && userName != null && timestamp != null) {
                        addPostToView(userName, content, timestamp);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPostToView(String userName, String content, String timestamp) {
        CardView postCard = (CardView) LayoutInflater.from(this)
                .inflate(R.layout.item_post, linearLayoutPosts, false);

        TextView textViewUserName = postCard.findViewById(R.id.textViewUserName);
        TextView textViewContent = postCard.findViewById(R.id.textViewContent);
        TextView textViewTimestamp = postCard.findViewById(R.id.textViewTimestamp);

        textViewUserName.setText(userName);
        textViewContent.setText(content);
        textViewTimestamp.setText(timestamp);

        linearLayoutPosts.addView(postCard, 0); // Add new posts at the top
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            
            View headerView = navigationView.getHeaderView(0);
            TextView navUsername = headerView.findViewById(R.id.textViewNavUsername);
            TextView navEmail = headerView.findViewById(R.id.textViewNavEmail);

            // Set fallback data immediately
            textViewEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "No email");
            textViewWelcome.setText("Welcome!");
            
            // Load data from Firebase Database
            mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        
                        // Update welcome message with name
                        if (name != null && !name.trim().isEmpty()) {
                            userName = name;
                            textViewWelcome.setText(String.format("Welcome, %s!", name));
                        } else {
                            // Fallback to display name from FirebaseUser
                            String displayName = currentUser.getDisplayName();
                            if (displayName != null && !displayName.trim().isEmpty()) {
                                userName = displayName;
                                textViewWelcome.setText(String.format("Welcome, %s!", displayName));
                            } else {
                                textViewWelcome.setText("Welcome!");
                            }
                        }
                        
                        // Update email
                        if (email != null && !email.trim().isEmpty()) {
                            textViewEmail.setText(email);
                        }
                        
                        // Set navigation header values
                        navUsername.setText(userName);
                        navEmail.setText(currentUser.getEmail());
                    } else {
                        // No data found in database, use FirebaseAuth data
                        String displayName = currentUser.getDisplayName();
                        if (displayName != null && !displayName.trim().isEmpty()) {
                            userName = displayName;
                            textViewWelcome.setText(String.format("Welcome, %s!", displayName));
                        } else {
                            textViewWelcome.setText("Welcome!");
                        }
                        
                        // Set navigation header values
                        navUsername.setText(userName);
                        navEmail.setText(currentUser.getEmail());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Failed to load user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    
                    // Fallback to FirebaseAuth data on error
                    String displayName = currentUser.getDisplayName();
                    if (displayName != null && !displayName.trim().isEmpty()) {
                        userName = displayName;
                        textViewWelcome.setText(String.format("Welcome, %s!", displayName));
                    } else {
                        textViewWelcome.setText("Welcome!");
                    }
                    
                    // Set navigation header values
                    navUsername.setText(userName);
                    navEmail.setText(currentUser.getEmail());
                }
            });
        } else {
            // User not logged in, redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_p) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}