package id.project.lab.ui.profile;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.project.lab.R;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etWa, etDiscord;
    private Spinner spinnerRole;
    private LinearLayout containerLinks;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String uid;
    private String[] roles = {"DEVELOPER", "DESAINER", "WRITER", "MARKETER", "VIDEOGRAFER", "LAINNYA"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        etName = findViewById(R.id.et_name);
        etWa = findViewById(R.id.et_wa);
        etDiscord = findViewById(R.id.et_discord);
        spinnerRole = findViewById(R.id.spinner_role);
        containerLinks = findViewById(R.id.container_links);
        ImageView btnBack = findViewById(R.id.btn_back);
        TextView btnTambahLink = findViewById(R.id.btn_tambah_link);
        Button btnSave = findViewById(R.id.btn_save);

        // Setup Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        btnTambahLink.setOnClickListener(v -> addLinkField(""));

        btnSave.setOnClickListener(v -> saveChanges());

        loadUserData();
    }

    private void addLinkField(String url) {
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.topMargin = (int) (12 * getResources().getDisplayMetrics().density);
        rowLayout.setLayoutParams(rowParams);

        EditText newLink = new EditText(this);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                0,
                (int) (60 * getResources().getDisplayMetrics().density),
                1f
        );
        newLink.setLayoutParams(inputParams);
        newLink.setBackgroundResource(R.drawable.bg_brutalist_shadow);
        newLink.setHint("https://github.com/username");
        newLink.setText(url);
        newLink.setPadding(
                (int) (16 * getResources().getDisplayMetrics().density),
                (int) (16 * getResources().getDisplayMetrics().density),
                (int) (16 * getResources().getDisplayMetrics().density),
                (int) (16 * getResources().getDisplayMetrics().density)
        );
        newLink.setTextSize(14f);
        newLink.setTextColor(android.graphics.Color.parseColor("#1D1B20"));

        TextView deleteBtn = new TextView(this);
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                (int) (60 * getResources().getDisplayMetrics().density),
                (int) (60 * getResources().getDisplayMetrics().density)
        );
        deleteParams.leftMargin = (int) (8 * getResources().getDisplayMetrics().density);
        deleteBtn.setLayoutParams(deleteParams);
        deleteBtn.setBackgroundResource(R.drawable.bg_brutalist_red_shadow);
        deleteBtn.setText("X");
        deleteBtn.setTextColor(android.graphics.Color.parseColor("#1D1B20"));
        deleteBtn.setTextSize(18f);
        deleteBtn.setGravity(Gravity.CENTER);

        deleteBtn.setOnClickListener(v -> containerLinks.removeView(rowLayout));

        try {
            Typeface font = ResourcesCompat.getFont(this, R.font.space_grotesk);
            newLink.setTypeface(font);
            deleteBtn.setTypeface(font, Typeface.BOLD);
        } catch (Exception e) {}

        rowLayout.addView(newLink);
        rowLayout.addView(deleteBtn);
        containerLinks.addView(rowLayout);
    }

    private void loadUserData() {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etName.setText(documentSnapshot.getString("name"));
                        etWa.setText(documentSnapshot.getString("wa"));
                        etDiscord.setText(documentSnapshot.getString("discord"));

                        String role = documentSnapshot.getString("role");
                        if (role != null) {
                            for (int i = 0; i < roles.length; i++) {
                                if (roles[i].equals(role)) {
                                    spinnerRole.setSelection(i);
                                    break;
                                }
                            }
                        }

                        String portfolio = documentSnapshot.getString("portfolio");
                        if (portfolio != null && !portfolio.isEmpty()) {
                            String[] links = portfolio.split(", ");
                            for (String link : links) {
                                addLinkField(link);
                            }
                        }
                    }
                });
    }

    private void saveChanges() {
        String name = etName.getText().toString().trim();
        String wa = etWa.getText().toString().trim();
        String discord = etDiscord.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder portfolios = new StringBuilder();
        for (int i = 0; i < containerLinks.getChildCount(); i++) {
            View child = containerLinks.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) child;
                if (row.getChildAt(0) instanceof EditText) {
                    String link = ((EditText) row.getChildAt(0)).getText().toString().trim();
                    if (!link.isEmpty()) {
                        if (portfolios.length() > 0) portfolios.append(", ");
                        portfolios.append(link);
                    }
                }
            }
        }

        Map<String, Object> update = new HashMap<>();
        update.put("name", name);
        update.put("wa", wa);
        update.put("discord", discord);
        update.put("role", role);
        update.put("portfolio", portfolios.toString());

        db.collection("users").document(uid)
                .update(update)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memperbarui profil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
