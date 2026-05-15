package id.project.lab.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import id.project.lab.MainActivity;
import id.project.lab.R;
import java.util.HashMap;
import java.util.Map;

public class OnboardingActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String selectedRole = "DESAINER"; // Default selected as per design

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText etUsername = findViewById(R.id.et_username);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etDiscord = findViewById(R.id.et_discord);
        EditText etWa = findViewById(R.id.et_wa);
        Button nextBtn = findViewById(R.id.btn_next);

        // Pre-fill email if available from Firebase Auth
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            etEmail.setText(currentUser.getEmail());
        }

        // Role Selection Logic
        TextView tvDev = findViewById(R.id.tv_role_developer);
        TextView tvDes = findViewById(R.id.tv_role_desainer);
        TextView tvWri = findViewById(R.id.tv_role_writer);
        TextView tvMar = findViewById(R.id.tv_role_marketer);
        TextView tvVid = findViewById(R.id.tv_role_videografer);
        TextView tvLai = findViewById(R.id.tv_role_lainnya);

        TextView[] roles = {tvDev, tvDes, tvWri, tvMar, tvVid, tvLai};

        for (TextView tv : roles) {
            if (tv != null) {
                tv.setOnClickListener(v -> {
                    // Reset all to unselected
                    for (TextView r : roles) {
                        if (r != null) r.setBackgroundResource(R.drawable.bg_brutalist_shadow);
                    }
                    // Set clicked to selected
                    tv.setBackgroundResource(R.drawable.bg_brutalist_selected);
                    selectedRole = tv.getText().toString();
                });
            }
        }

        if (nextBtn != null) {
            nextBtn.setOnClickListener(v -> {
                String username = etUsername.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String discord = etDiscord.getText().toString().trim();
                String wa = etWa.getText().toString().trim();

                if (username.isEmpty() || email.isEmpty()) {
                    Toast.makeText(this, "Username dan Email wajib diisi", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveUserData(username, email, discord, wa);
            });
        }
    }

    private void saveUserData(String username, String email, String discord, String wa) {
        Intent intent = new Intent(this, Onboarding2Activity.class);
        intent.putExtra("name", username);
        intent.putExtra("email", email);
        intent.putExtra("role", selectedRole);
        intent.putExtra("discord", discord);
        intent.putExtra("wa", wa);
        startActivity(intent);
    }
}
