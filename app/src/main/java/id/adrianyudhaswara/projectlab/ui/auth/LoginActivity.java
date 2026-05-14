package id.adrianyudhaswara.projectlab.ui.auth;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import id.adrianyudhaswara.projectlab.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LinearLayout googleBtn = findViewById(R.id.google_login_button);
        LinearLayout githubBtn = findViewById(R.id.github_login_button);

        if (googleBtn != null) {
            googleBtn.setOnClickListener(v -> {
                Toast.makeText(this, "Google Login Clicked", Toast.LENGTH_SHORT).show();
            });
        }

        if (githubBtn != null) {
            githubBtn.setOnClickListener(v -> {
                Toast.makeText(this, "GitHub Login Clicked", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
