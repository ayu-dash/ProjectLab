package id.project.lab.ui.application;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import id.project.lab.R;
import id.project.lab.model.Applicant;

public class DetailLamaranActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvStatus, tvProjectTitle, tvRole, tvDate, tvMessage;
    private TextView tvContactWA, tvContactDiscord, tvContactEmail;
    private LinearLayout containerPortfolios, layoutContactInfo;
    private AppCompatButton btnCancel;
    private FirebaseFirestore db;
    private String applicationId, projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_lamaran);

        db = FirebaseFirestore.getInstance();
        applicationId = getIntent().getStringExtra("applicationId");
        projectId = getIntent().getStringExtra("projectId");

        btnBack = findViewById(R.id.btn_back);
        tvStatus = findViewById(R.id.tv_status);
        tvProjectTitle = findViewById(R.id.tv_project_title);
        tvRole = findViewById(R.id.tv_role);
        tvDate = findViewById(R.id.tv_date);
        tvMessage = findViewById(R.id.tv_message);
        containerPortfolios = findViewById(R.id.container_portfolios);
        btnCancel = findViewById(R.id.btn_cancel);
        
        layoutContactInfo = findViewById(R.id.layout_contact_info);
        tvContactWA = findViewById(R.id.tv_contact_wa);
        tvContactDiscord = findViewById(R.id.tv_contact_discord);
        tvContactEmail = findViewById(R.id.tv_contact_email);

        btnBack.setOnClickListener(v -> finish());
        
        loadApplicationDetail();
    }

    private void loadApplicationDetail() {
        if (applicationId == null) return;

        db.collection("applications").document(applicationId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Applicant app = documentSnapshot.toObject(Applicant.class);
                if (app != null) {
                    displayDetail(app);
                }
            }
        });
    }

    private void displayDetail(Applicant app) {
        tvProjectTitle.setText(app.getProjectTitle() != null ? app.getProjectTitle().toUpperCase() : "PROJEK");
        tvRole.setText(app.getRole() != null ? app.getRole().toUpperCase() : "-");
        
        String status = app.getStatus() != null ? app.getStatus() : "PENDING";
        updateStatusUI(status);

        if (app.getTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            tvDate.setText(sdf.format(new Date(app.getTimestamp())));
        }

        // Real message from applicant
        String message = app.getMessage();
        if (message != null && !message.isEmpty()) {
            tvMessage.setText(message);
        } else {
            tvMessage.setText("Tidak ada pesan tambahan.");
        }

        // Portfolios
        if (app.getPortfolioLinks() != null) {
            containerPortfolios.removeAllViews();
            for (String link : app.getPortfolioLinks()) {
                TextView tvLink = new TextView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, (int) (8 * getResources().getDisplayMetrics().density));
                tvLink.setLayoutParams(params);
                tvLink.setBackgroundResource(R.drawable.bg_brutalist_border);
                tvLink.setPadding(
                        (int) (16 * getResources().getDisplayMetrics().density),
                        (int) (16 * getResources().getDisplayMetrics().density),
                        (int) (16 * getResources().getDisplayMetrics().density),
                        (int) (16 * getResources().getDisplayMetrics().density)
                );
                tvLink.setText(link);
                tvLink.setTextColor(android.graphics.Color.parseColor("#FF494551"));
                tvLink.setTextSize(16f);
                tvLink.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_open_in_new, 0, 0, 0);
                tvLink.setCompoundDrawablePadding((int) (12 * getResources().getDisplayMetrics().density));

                tvLink.setOnClickListener(v -> {
                    String url = link;
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://" + url;
                    }
                    try {
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        android.widget.Toast.makeText(this, "Gagal membuka link", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });

                try {
                    tvLink.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(this, R.font.space_grotesk));
                } catch (Exception e) {}
                
                containerPortfolios.addView(tvLink);
            }
        }

        btnCancel.setOnClickListener(v -> {
            cancelApplication(app);
        });

        // Hide cancel button if already accepted or rejected
        if (!"PENDING".equals(status)) {
            btnCancel.setVisibility(View.GONE);
        }
    }

    private void updateStatusUI(String status) {
        if ("ACCEPTED".equals(status)) {
            tvStatus.setText("DITERIMA");
            tvStatus.setBackgroundResource(R.drawable.bg_brutalist_selected); // Green/Selected color
            layoutContactInfo.setVisibility(View.VISIBLE);
            loadOwnerContact();
        } else if ("REJECTED".equals(status)) {
            tvStatus.setText("DITOLAK");
            tvStatus.setBackgroundResource(R.drawable.bg_brutalist_red_shadow); // Red
            layoutContactInfo.setVisibility(View.GONE);
        } else {
            tvStatus.setText("MENUNGGU");
            tvStatus.setBackgroundResource(R.drawable.bg_brutalist_yellow_shadow); // Yellow
            layoutContactInfo.setVisibility(View.GONE);
        }
    }

    private void loadOwnerContact() {
        if (projectId == null) return;
        db.collection("projects").document(projectId).get().addOnSuccessListener(projectDoc -> {
            String ownerId = projectDoc.getString("ownerId");
            if (ownerId != null) {
                db.collection("users").document(ownerId).get().addOnSuccessListener(userDoc -> {
                    String wa = userDoc.getString("wa");
                    if (wa == null) wa = userDoc.getString("whatsapp"); // fallback
                    
                    String discord = userDoc.getString("discord");
                    String email = userDoc.getString("email");

                    if (wa != null && !wa.isEmpty()) {
                        tvContactWA.setText("WhatsApp: " + wa);
                        String finalWa = wa;
                        tvContactWA.setOnClickListener(v -> {
                            String url = "https://wa.me/" + finalWa.replace("+", "").replace(" ", "").replace("-", "");
                            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
                            startActivity(intent);
                        });
                    } else {
                        tvContactWA.setText("WhatsApp: -");
                    }
                    
                    if (discord != null && !discord.isEmpty()) {
                        tvContactDiscord.setText("Discord: " + discord);
                        String finalDiscord = discord;
                        tvContactDiscord.setOnClickListener(v -> {
                            String url = finalDiscord.trim();
                            if (!url.startsWith("http")) {
                                if (url.contains("discord.gg") || url.contains("discord.com")) {
                                    url = "https://" + url;
                                } else {
                                    url = "https://discord.com/search?q=" + url;
                                }
                            }
                            try {
                                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
                                startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(this, "Gagal membuka Discord", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        tvContactDiscord.setText("Discord: -");
                    }
                    
                    if (email != null && !email.isEmpty()) {
                        tvContactEmail.setText("Email: " + email);
                        String finalEmail = email;
                        tvContactEmail.setOnClickListener(v -> {
                            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
                            intent.setData(android.net.Uri.parse("mailto:" + finalEmail));
                            startActivity(intent);
                        });
                    } else {
                        tvContactEmail.setText("Email: -");
                    }
                });
            }
        });
    }

    private void cancelApplication(Applicant app) {
        if (applicationId == null) return;

        com.google.firebase.firestore.WriteBatch batch = db.batch();
        
        // 1. Delete from global applications
        batch.delete(db.collection("applications").document(applicationId));
        
        // 2. Delete from project's applications
        if (app.getProjectId() != null) {
            batch.delete(db.collection("projects").document(app.getProjectId())
              .collection("applications").document(applicationId));
            
            // Decrement applicants count in project doc
            batch.update(db.collection("projects").document(app.getProjectId()), 
                    "applicantsCount", com.google.firebase.firestore.FieldValue.increment(-1));
        }

        btnCancel.setEnabled(false);
        btnCancel.setText("MEMBATALKAN...");

        batch.commit().addOnSuccessListener(aVoid -> {
            if (!isFinishing()) {
                Toast.makeText(this, "Lamaran telah dibatalkan", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            btnCancel.setEnabled(true);
            btnCancel.setText("BATALKAN LAMARAN");
            Toast.makeText(this, "Gagal membatalkan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
