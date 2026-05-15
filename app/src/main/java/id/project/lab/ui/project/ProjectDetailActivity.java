package id.project.lab.ui.project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import id.project.lab.R;

public class ProjectDetailActivity extends AppCompatActivity {

    private TextView tvType, tvTitle, tvDescription, tvOwner;
    private ImageView btnBack, ivBanner;
    private android.widget.LinearLayout rolesContainer;
    private android.widget.ProgressBar progressBar;
    private Button btnApply;
    private com.google.firebase.firestore.FirebaseFirestore db;
    private String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        projectId = getIntent().getStringExtra("projectId");

        // Initialize views
        tvType = findViewById(R.id.tv_detail_type);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvDescription = findViewById(R.id.tv_detail_description);
        tvOwner = findViewById(R.id.tv_detail_owner);
        ivBanner = findViewById(R.id.iv_detail_banner);
        rolesContainer = findViewById(R.id.roles_container);
        progressBar = findViewById(R.id.pb_detail);
        btnBack = findViewById(R.id.btn_back);
        btnApply = findViewById(R.id.btn_apply);

        btnBack.setOnClickListener(v -> finish());

        if (projectId != null) {
            loadProjectDetails();
        } else {
            // Fallback to intent extras
            tvTitle.setText(getIntent().getStringExtra("title"));
            tvType.setText(getIntent().getStringExtra("type"));
            tvDescription.setText(getIntent().getStringExtra("description"));
        }
    }

    private void loadProjectDetails() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        db.collection("projects").document(projectId).get().addOnSuccessListener(documentSnapshot -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (documentSnapshot.exists()) {
                id.project.lab.model.Project project = documentSnapshot.toObject(id.project.lab.model.Project.class);
                if (project != null) {
                    project.setId(documentSnapshot.getId());
                    displayProject(project);
                }
            }
        }).addOnFailureListener(e -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Gagal memuat detail projek", Toast.LENGTH_SHORT).show();
        });
    }

    private void displayProject(id.project.lab.model.Project project) {
        tvTitle.setText(project.getTitle().toUpperCase());
        tvType.setText(project.getType().toUpperCase());
        tvDescription.setText(project.getDescription());
        
        // Fetch owner name
        db.collection("users").document(project.getOwnerId()).get().addOnSuccessListener(userDoc -> {
            if (userDoc.exists()) {
                String ownerName = userDoc.getString("name");
                tvOwner.setText("Oleh: " + (ownerName != null ? ownerName : "Pengguna Lab"));
            }
        });

        // Decode Banner
        String base64Image = project.getBanner_base64();
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                // Remove header if exists (data:image/jpeg;base64,)
                if (base64Image.contains(",")) {
                    base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
                }
                
                byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                
                if (decodedByte != null) {
                    ivBanner.setImageBitmap(decodedByte);
                    ivBanner.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                } else {
                    android.util.Log.e("ProjectDetail", "Failed to decode bitmap from base64");
                }
            } catch (Exception e) {
                android.util.Log.e("ProjectDetail", "Error decoding banner: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Render Roles
        rolesContainer.removeAllViews();
        if (project.getRoles() != null) {
            for (java.util.Map<String, Object> role : project.getRoles()) {
                View roleView = getLayoutInflater().inflate(R.layout.item_role_detail, rolesContainer, false);
                TextView tvRoleName = roleView.findViewById(R.id.tv_role_name);
                TextView tvRoleSlot = roleView.findViewById(R.id.tv_role_slot);
                
                String name = (String) role.get("roleName");
                Object slot = role.get("slot");
                
                tvRoleName.setText(name != null ? name.toUpperCase() : "ROLE");
                tvRoleSlot.setText(slot + " SLOT TERSISA");
                
                rolesContainer.addView(roleView);
                
                // Add separator
                View divider = new View(this);
                divider.setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 2));
                divider.setBackgroundColor(android.graphics.Color.parseColor("#FFE0E0E0"));
                rolesContainer.addView(divider);
            }
        }

        // Action Logic
        String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        boolean isOwner = currentUid != null && currentUid.equals(project.getOwnerId());
        boolean isClosed = "TUTUP".equals(project.getStatus());

        if (isOwner) {
            findViewById(R.id.sticky_bottom_action).setVisibility(View.GONE);
        } else if (isClosed) {
            btnApply.setText("PENDAFTARAN DITUTUP");
            btnApply.setEnabled(false);
            btnApply.setAlpha(0.5f);
        } else {
            // Check if user already applied
            if (currentUid != null) {
                db.collection("applications")
                    .whereEqualTo("userId", currentUid)
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
                            btnApply.setText("SUDAH DILAMAR");
                            btnApply.setEnabled(false);
                            btnApply.setAlpha(0.5f);
                        } else {
                            btnApply.setOnClickListener(v -> {
                                android.content.Intent intent = new android.content.Intent(this, LamarProjectActivity.class);
                                intent.putExtra("projectId", projectId);
                                intent.putExtra("projectTitle", project.getTitle());
                                startActivity(intent);
                            });
                        }
                    });
            }
        }
    }
}
