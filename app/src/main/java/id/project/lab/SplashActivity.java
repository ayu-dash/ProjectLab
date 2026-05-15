package id.project.lab;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import id.project.lab.ui.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Using dedicated @font/space_grotesk_bold in XML now

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAuthState();
            }
        }, 2000); // 2 seconds delay
    }

    private void checkAuthState() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            // User is signed in, go to Main
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            // No user is signed in, go to Login
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
        finish();
    }
}
