package id.project.lab.ui.project;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import id.project.lab.R;

public class LamarProjectActivity extends AppCompatActivity {

    private ImageView btnBack;
    private AppCompatButton btnSend;
    private android.widget.LinearLayout containerPortfolios;
    private android.widget.RadioGroup rgRoles;
    private android.widget.EditText etMessage;
    private android.widget.TextView tvSummaryNote;
    private com.google.firebase.firestore.FirebaseFirestore db;
    private com.google.firebase.auth.FirebaseAuth mAuth;
    private String projectId, projectTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lamar_project);

        db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();

        projectId = getIntent().getStringExtra("projectId");
        projectTitle = getIntent().getStringExtra("projectTitle");

        // Initialize views
        btnBack = findViewById(R.id.btn_back);
        btnSend = findViewById(R.id.btn_send_application);
        containerPortfolios = findViewById(R.id.container_portfolios);
        rgRoles = findViewById(R.id.rg_roles);
        etMessage = findViewById(R.id.et_message);
        tvSummaryNote = findViewById(R.id.tv_summary_note);

        if (projectTitle != null) {
            tvSummaryNote.setText("Kamu melamar di " + projectTitle.toUpperCase() + ". Pastikan link portofolio kamu dapat diakses oleh publik.");
        }

        loadUserPortfolios();
        loadProjectRoles();

        // Set click listeners
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to previous screen
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendApplication();
            }
        });

        checkExistingApplication();
    }

    private void checkExistingApplication() {
        if (mAuth.getCurrentUser() == null || projectId == null) return;
        
        db.collection("applications")
            .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
            .whereEqualTo("projectId", projectId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    String status = doc.getString("status");
                    if ("PENDING".equals(status) || "ACCEPTED".equals(status)) {
                        btnSend.setEnabled(false);
                        btnSend.setText("SUDAH DILAMAR");
                        btnSend.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY));
                        break;
                    }
                }
            });
    }

    private void loadProjectRoles() {
        if (projectId == null) return;
        db.collection("projects").document(projectId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                java.util.List<java.util.Map<String, Object>> roles = (java.util.List<java.util.Map<String, Object>>) documentSnapshot.get("roles");
                if (roles != null) {
                    rgRoles.removeAllViews();
                    for (int i = 0; i < roles.size(); i++) {
                        String roleName = (String) roles.get(i).get("roleName");
                        android.widget.RadioButton rb = new android.widget.RadioButton(this);
                        android.widget.RadioGroup.LayoutParams params = new android.widget.RadioGroup.LayoutParams(
                                android.widget.RadioGroup.LayoutParams.MATCH_PARENT,
                                android.widget.RadioGroup.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 0, (int) (8 * getResources().getDisplayMetrics().density));
                        rb.setLayoutParams(params);
                        rb.setBackgroundResource(R.drawable.bg_brutalist_unselected);
                        rb.setButtonDrawable(null);
                        rb.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sl_radio_square, 0, 0, 0);
                        rb.setCompoundDrawablePadding((int) (12 * getResources().getDisplayMetrics().density));
                        rb.setPadding(
                                (int) (16 * getResources().getDisplayMetrics().density),
                                (int) (16 * getResources().getDisplayMetrics().density),
                                (int) (16 * getResources().getDisplayMetrics().density),
                                (int) (16 * getResources().getDisplayMetrics().density)
                        );
                        rb.setText(roleName != null ? roleName.toUpperCase() : "ROLE");
                        rb.setTextSize(18f);
                        rb.setTextColor(android.graphics.Color.parseColor("#FF1D1B20"));
                        try {
                            android.graphics.Typeface font = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.space_grotesk);
                            rb.setTypeface(font, android.graphics.Typeface.BOLD);
                        } catch (Exception e) {
                            rb.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                        }
                        
                        rb.setId(View.generateViewId());
                        rgRoles.addView(rb);
                        if (i == 0) rb.setChecked(true);
                    }
                }
            }
        });
    }

    private void sendApplication() {
        if (mAuth.getCurrentUser() == null) return;

        int selectedRoleId = rgRoles.getCheckedRadioButtonId();
        if (selectedRoleId == -1) {
            Toast.makeText(this, "Pilih role terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        android.widget.RadioButton rb = findViewById(selectedRoleId);
        String selectedRole = rb.getText().toString();

        java.util.List<String> selectedPortfolios = new java.util.ArrayList<>();
        for (int i = 0; i < containerPortfolios.getChildCount(); i++) {
            android.widget.CheckBox cb = (android.widget.CheckBox) containerPortfolios.getChildAt(i);
            if (cb.isChecked()) {
                selectedPortfolios.add(cb.getText().toString());
            }
        }

        if (selectedPortfolios.isEmpty()) {
            Toast.makeText(this, "Pilih minimal satu portofolio", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSend.setEnabled(false); // Prevent double clicks
        String message = etMessage.getText().toString();
        String uid = mAuth.getCurrentUser().getUid();

        // Final check before sending
        db.collection("applications")
            .whereEqualTo("userId", uid)
            .whereEqualTo("projectId", projectId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                boolean hasActive = false;
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    String status = doc.getString("status");
                    if ("PENDING".equals(status) || "ACCEPTED".equals(status)) {
                        hasActive = true;
                        break;
                    }
                }
                
                if (hasActive) {
                    Toast.makeText(this, "Kamu punya lamaran aktif di projek ini", Toast.LENGTH_SHORT).show();
                    btnSend.setText("SUDAH DILAMAR");
                    return;
                }
                
                // Continue with application
                db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
            String userName = userDoc.getString("name");
            
            id.project.lab.model.Applicant applicant = new id.project.lab.model.Applicant();
            applicant.setUserId(uid);
            applicant.setName(userName != null ? userName : "User");
            applicant.setProfilePicture(userDoc.getString("profilePicture"));
            applicant.setRole(selectedRole);
            applicant.setMessage(message);
            applicant.setPortfolioLinks(selectedPortfolios);
            applicant.setStatus("PENDING");
            applicant.setProjectId(projectId);
            applicant.setProjectTitle(projectTitle);
            applicant.setTimestamp(System.currentTimeMillis());

            // Save to project's applications
            db.collection("projects").document(projectId).collection("applications")
                .add(applicant)
                .addOnSuccessListener(docRef -> {
                    // Also save to global applications for easy access by applicant
                    db.collection("applications").document(docRef.getId()).set(applicant);
                    
                    // Notify Project Owner
                    db.collection("projects").document(projectId).get().addOnSuccessListener(projectDoc -> {
                        // Increment applicants count in project doc
                        db.collection("projects").document(projectId)
                                .update("applicantsCount", com.google.firebase.firestore.FieldValue.increment(1));

                        String ownerId = projectDoc.getString("ownerId");
                        if (ownerId != null) {
                            id.project.lab.model.Notification notif = new id.project.lab.model.Notification(
                                ownerId,
                                "Lamaran Baru!",
                                userName + " melamar untuk role " + selectedRole + " di projek " + projectTitle,
                                "APPLICATION",
                                docRef.getId()
                            );
                            db.collection("users").document(ownerId).collection("notifications").add(notif);
                        }
                    });

                    Toast.makeText(LamarProjectActivity.this, "Lamaran berhasil dikirim!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LamarProjectActivity.this, "Gagal mengirim lamaran", Toast.LENGTH_SHORT).show();
                });
                });
            });
    }

    private void loadUserPortfolios() {
        if (mAuth.getCurrentUser() == null) return;
        
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String portfolioStr = documentSnapshot.getString("portfolio");
                    if (portfolioStr != null && !portfolioStr.isEmpty()) {
                        String[] portfolios = portfolioStr.split(", ");
                        containerPortfolios.removeAllViews();
                        for (String link : portfolios) {
                            android.widget.CheckBox cb = new android.widget.CheckBox(this);
                            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(0, 0, 0, (int) (8 * getResources().getDisplayMetrics().density));
                            cb.setLayoutParams(params);
                            cb.setBackgroundResource(R.drawable.bg_brutalist_unselected);
                            cb.setButtonDrawable(null);
                            cb.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sl_checkbox_brutalist, 0, 0, 0);
                            cb.setCompoundDrawablePadding((int) (12 * getResources().getDisplayMetrics().density));
                            cb.setPadding(
                                    (int) (16 * getResources().getDisplayMetrics().density),
                                    (int) (16 * getResources().getDisplayMetrics().density),
                                    (int) (16 * getResources().getDisplayMetrics().density),
                                    (int) (16 * getResources().getDisplayMetrics().density)
                            );
                            cb.setText(link);
                            cb.setTextSize(16f);
                            cb.setTextColor(android.graphics.Color.parseColor("#FF494551"));
                            try {
                                cb.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(this, R.font.space_grotesk));
                            } catch (Exception e) {}
                            
                            containerPortfolios.addView(cb);
                        }
                    }
                }
            });
    }
}
