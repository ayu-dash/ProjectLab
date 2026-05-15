package id.project.lab.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import id.project.lab.MainActivity;
import id.project.lab.R;

public class Onboarding2Activity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Map<String, Object> userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_2);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Retrieve data from Intent
        Intent intent = getIntent();
        userData = new HashMap<>();
        userData.put("name", intent.getStringExtra("name"));
        userData.put("email", intent.getStringExtra("email"));
        userData.put("role", intent.getStringExtra("role"));
        userData.put("discord", intent.getStringExtra("discord"));
        userData.put("wa", intent.getStringExtra("wa"));

        android.widget.LinearLayout containerLinks = findViewById(R.id.container_links);
        android.widget.TextView btnTambahLink = findViewById(R.id.btn_tambah_link);
        Button btnFinish = findViewById(R.id.btn_finish);
        android.widget.TextView btnLewati = findViewById(R.id.btn_lewati);

        if (btnTambahLink != null) {
            btnTambahLink.setOnClickListener(v -> {
                android.widget.LinearLayout rowLayout = new android.widget.LinearLayout(this);
                rowLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                android.widget.LinearLayout.LayoutParams rowParams = new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );
                rowParams.topMargin = (int) (12 * getResources().getDisplayMetrics().density);
                rowLayout.setLayoutParams(rowParams);

                EditText newLink = new EditText(this);
                android.widget.LinearLayout.LayoutParams inputParams = new android.widget.LinearLayout.LayoutParams(
                        0,
                        (int) (60 * getResources().getDisplayMetrics().density),
                        1f
                );
                newLink.setLayoutParams(inputParams);
                newLink.setBackgroundResource(R.drawable.bg_brutalist_shadow);
                newLink.setHint("https://github.com/username");
                newLink.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_URI);
                newLink.setPadding(
                        (int) (16 * getResources().getDisplayMetrics().density),
                        (int) (16 * getResources().getDisplayMetrics().density),
                        (int) (16 * getResources().getDisplayMetrics().density),
                        (int) (16 * getResources().getDisplayMetrics().density)
                );
                newLink.setTextSize(14f);
                newLink.setTextColor(android.graphics.Color.parseColor("#1D1B20"));
                newLink.setHintTextColor(android.graphics.Color.parseColor("#CBC4D2"));

                android.widget.TextView deleteBtn = new android.widget.TextView(this);
                android.widget.LinearLayout.LayoutParams deleteParams = new android.widget.LinearLayout.LayoutParams(
                        (int) (60 * getResources().getDisplayMetrics().density),
                        (int) (60 * getResources().getDisplayMetrics().density)
                );
                deleteParams.leftMargin = (int) (8 * getResources().getDisplayMetrics().density);
                deleteBtn.setLayoutParams(deleteParams);
                deleteBtn.setBackgroundResource(R.drawable.bg_brutalist_red_shadow);
                deleteBtn.setText("X");
                deleteBtn.setTextColor(android.graphics.Color.parseColor("#1D1B20"));
                deleteBtn.setTextSize(18f);
                deleteBtn.setGravity(android.view.Gravity.CENTER);

                deleteBtn.setOnClickListener(deleteView -> {
                    containerLinks.removeView(rowLayout);
                });

                try {
                    android.graphics.Typeface font = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.space_grotesk);
                    newLink.setTypeface(font);
                    deleteBtn.setTypeface(font, android.graphics.Typeface.BOLD);
                } catch (Exception e) {}

                rowLayout.addView(newLink);
                rowLayout.addView(deleteBtn);

                containerLinks.addView(rowLayout);
            });
        }

        btnFinish.setOnClickListener(v -> {
            StringBuilder portfolios = new StringBuilder();
            for (int i = 0; i < containerLinks.getChildCount(); i++) {
                android.view.View child = containerLinks.getChildAt(i);
                EditText et = null;
                if (child instanceof EditText) {
                    et = (EditText) child;
                } else if (child instanceof android.widget.LinearLayout) {
                    android.widget.LinearLayout row = (android.widget.LinearLayout) child;
                    if (row.getChildCount() > 0 && row.getChildAt(0) instanceof EditText) {
                        et = (EditText) row.getChildAt(0);
                    }
                }
                
                if (et != null) {
                    String text = et.getText().toString().trim();
                    if (!text.isEmpty()) {
                        if (portfolios.length() > 0) portfolios.append(", ");
                        portfolios.append(text);
                    }
                }
            }
            saveData(portfolios.toString());
        });

        if (btnLewati != null) {
            btnLewati.setOnClickListener(v -> {
                saveData(""); // portfolio kosong
            });
        }
    }

    private void saveData(String portfolio) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        userData.put("id", uid);
        userData.put("portfolio", portfolio);
        userData.put("onboardingCompleted", true);
        if (currentUser.getPhotoUrl() != null) {
            userData.put("profilePicture", currentUser.getPhotoUrl().toString());
        }

        db.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Registrasi Selesai!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal menyimpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
