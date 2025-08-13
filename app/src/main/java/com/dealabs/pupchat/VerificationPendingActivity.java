package com.dealabs.pupchat;

import android.content.Intent;
import android.widget.Toast;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerificationPendingActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_pending);

        mAuth = FirebaseAuth.getInstance();

        String email = getIntent().getStringExtra("email");
        TextView textViewEmail = findViewById(R.id.textViewEmail);
        textViewEmail.setText(email);

        Button buttonResend = findViewById(R.id.buttonResend);
        Button buttonCheckVerified = findViewById(R.id.buttonCheckVerified);

        buttonResend.setOnClickListener(v -> resendVerificationEmail());
        buttonCheckVerified.setOnClickListener(v -> checkEmailVerification());
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, 
                            "Verification email resent. Please check your inbox.", 
                            Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, 
                            "Failed to resend verification email: " + task.getException().getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                });
        }
    }

    private void checkEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    // Email is verified, proceed to main activity
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, 
                        "Email not verified yet. Please check your inbox.", 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}