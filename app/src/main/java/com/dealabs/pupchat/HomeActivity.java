package com.dealabs.pupchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private TextView textViewWelcome;
    private TextView textViewEmail;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize TextViews
        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewEmail = findViewById(R.id.textViewEmail);

        loadUserData();
        
        // Logout button click listener
        findViewById(R.id.buttonLogout).setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            
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
                            textViewWelcome.setText(String.format("Welcome, %s!", name));
                        } else {
                            // Fallback to display name from FirebaseUser
                            String displayName = currentUser.getDisplayName();
                            if (displayName != null && !displayName.trim().isEmpty()) {
                                textViewWelcome.setText(String.format("Welcome, %s!", displayName));
                            } else {
                                textViewWelcome.setText("Welcome!");
                            }
                        }
                        
                        // Update email
                        if (email != null && !email.trim().isEmpty()) {
                            textViewEmail.setText(email);
                        }
                    } else {
                        // No data found in database, use FirebaseAuth data
                        String displayName = currentUser.getDisplayName();
                        if (displayName != null && !displayName.trim().isEmpty()) {
                            textViewWelcome.setText(String.format("Welcome, %s!", displayName));
                        } else {
                            textViewWelcome.setText("Welcome!");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Failed to load user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    
                    // Fallback to FirebaseAuth data on error
                    String displayName = currentUser.getDisplayName();
                    if (displayName != null && !displayName.trim().isEmpty()) {
                        textViewWelcome.setText(String.format("Welcome, %s!", displayName));
                    } else {
                        textViewWelcome.setText("Welcome!");
                    }
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
        if (item.getItemId() == R.id.menu_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to login/register after logging in
        moveTaskToBack(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is still authenticated
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}