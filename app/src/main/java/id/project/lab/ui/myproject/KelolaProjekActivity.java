package id.project.lab.ui.myproject;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.project.lab.R;
import id.project.lab.model.Applicant;
import id.project.lab.model.Project;

public class KelolaProjekActivity extends AppCompatActivity {

    private String projectId;
    private Project project;
    private FirebaseFirestore db;

    private TextView tvProjectTitle, tvTotalPelamar, tvProgresTim, tvTeamSlotStatus;
    private View btnBukaProjek, btnTutupProjek;
    private RecyclerView rvPelamar, rvTim;
    
    private List<Applicant> applicantList = new ArrayList<>();
    private List<TeamAdapter.TeamSlot> teamSlots = new ArrayList<>();
    private ApplicantAdapter applicantAdapter;
    private TeamAdapter teamAdapter;

    private String currentStatus = "BUKA";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kelola_projek);

        projectId = getIntent().getStringExtra("PROJECT_ID");
        if (projectId == null) {
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        initViews();
        setupAdapters();
        fetchProjectData();
    }

    private void initViews() {
        tvProjectTitle = findViewById(R.id.tv_project_title);
        tvTotalPelamar = findViewById(R.id.tv_total_pelamar);
        tvProgresTim = findViewById(R.id.tv_progres_tim);
        tvTeamSlotStatus = findViewById(R.id.tv_team_slot_status);
        
        btnBukaProjek = findViewById(R.id.btn_buka_projek);
        btnTutupProjek = findViewById(R.id.btn_tutup_projek);
        
        rvPelamar = findViewById(R.id.rv_pelamar);
        rvTim = findViewById(R.id.rv_tim);
        
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        btnBukaProjek.setOnClickListener(v -> updateProjectStatus("BUKA"));
        btnTutupProjek.setOnClickListener(v -> updateProjectStatus("TUTUP"));
        
        findViewById(R.id.btn_simpan_perubahan).setOnClickListener(v -> {
            Toast.makeText(this, "Perubahan disimpan!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setupAdapters() {
        applicantAdapter = new ApplicantAdapter(applicantList, new ApplicantAdapter.OnApplicantActionListener() {
            @Override
            public void onAccept(Applicant applicant) {
                showAcceptDialog(applicant);
            }

            @Override
            public void onReject(Applicant applicant) {
                db.collection("projects").document(projectId)
                        .collection("applications").document(applicant.getId())
                        .update("status", "REJECTED")
                        .addOnSuccessListener(aVoid -> {
                            // Update global application status
                            db.collection("applications").document(applicant.getId()).update("status", "REJECTED");

                            // Notify Applicant
                            id.project.lab.model.Notification notif = new id.project.lab.model.Notification(
                                applicant.getUserId(),
                                "Update Lamaran",
                                "Mohon maaf, lamaran kamu untuk role " + applicant.getRole() + " di projek " + project.getTitle() + " belum dapat diterima saat ini.",
                                "STATUS_CHANGE",
                                applicant.getId()
                            );
                            db.collection("users").document(applicant.getUserId()).collection("notifications").add(notif);
                        });
            }
        });
        rvPelamar.setLayoutManager(new LinearLayoutManager(this));
        rvPelamar.setAdapter(applicantAdapter);

        teamAdapter = new TeamAdapter(teamSlots);
        rvTim.setLayoutManager(new GridLayoutManager(this, 2));
        rvTim.setAdapter(teamAdapter);
    }

    private void showAcceptDialog(Applicant applicant) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_terima_pelamar, null);
        
        view.findViewById(R.id.btn_ya_terima).setOnClickListener(v -> {
            db.collection("projects").document(projectId)
                    .collection("applications").document(applicant.getId())
                    .update("status", "ACCEPTED")
                    .addOnSuccessListener(aVoid -> {
                        // Update accepted count in project doc
                        db.collection("projects").document(projectId)
                                .update("acceptedCount", com.google.firebase.firestore.FieldValue.increment(1));
                        
                        // Update global application status
                        db.collection("applications").document(applicant.getId()).update("status", "ACCEPTED");

                        // Notify Applicant
                        id.project.lab.model.Notification notif = new id.project.lab.model.Notification(
                            applicant.getUserId(),
                            "Lamaran Diterima!",
                            "Selamat! Lamaran kamu untuk role " + applicant.getRole() + " di projek " + project.getTitle() + " telah diterima.",
                            "STATUS_CHANGE",
                            applicant.getId()
                        );
                        db.collection("users").document(applicant.getUserId()).collection("notifications").add(notif);

                        Toast.makeText(this, applicant.getName() + " berhasil diterima!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });
        
        view.findViewById(R.id.btn_batalkan).setOnClickListener(v -> dialog.dismiss());
        
        dialog.setContentView(view);
        dialog.show();
    }

    private com.google.firebase.firestore.ListenerRegistration applicantsListener;

    private void fetchProjectData() {
        db.collection("projects").document(projectId)
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        project = value.toObject(Project.class);
                        if (project != null) {
                            tvProjectTitle.setText(project.getTitle());
                            currentStatus = project.getStatus() != null ? project.getStatus() : "BUKA";
                            updateStatusUI();
                            
                            // Only fetch applicants once or refresh if needed
                            if (applicantsListener == null) {
                                fetchApplicants();
                            } else {
                                // If already listening, just trigger a refresh of the team list 
                                // with the new project roles data if applicants are already loaded
                                if (applicantList != null) {
                                    // We need to re-filter acceptedOnes from current data or wait for next trigger
                                    // For now, fetchApplicants handles the listener
                                }
                            }
                        }
                    }
                });
    }

    private void fetchApplicants() {
        if (applicantsListener != null) applicantsListener.remove();
        
        applicantsListener = db.collection("projects").document(projectId).collection("applications")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        applicantList.clear();
                        List<Applicant> acceptedOnes = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Applicant a = doc.toObject(Applicant.class);
                            a.setId(doc.getId());
                            if ("PENDING".equals(a.getStatus()) || a.getStatus() == null) {
                                applicantList.add(a);
                            } else if ("ACCEPTED".equals(a.getStatus())) {
                                acceptedOnes.add(a);
                            }
                        }
                        applicantAdapter.notifyDataSetChanged();
                        tvTotalPelamar.setText(String.valueOf(applicantList.size()));
                        updateTeamList(acceptedOnes);
                    }
                });
    }

    private void updateStatusUI() {
        if (currentStatus.equals("BUKA")) {
            btnBukaProjek.setBackgroundResource(R.drawable.bg_brutalist_selected);
            btnTutupProjek.setBackgroundResource(R.drawable.bg_brutalist_unselected);
        } else {
            btnBukaProjek.setBackgroundResource(R.drawable.bg_brutalist_unselected);
            btnTutupProjek.setBackgroundResource(R.drawable.bg_brutalist_selected);
        }
    }

    private void updateProjectStatus(String status) {
        currentStatus = status;
        updateStatusUI();
        db.collection("projects").document(projectId).update("status", status);
    }

    private void updateTeamList(List<Applicant> acceptedApplicants) {
        teamSlots.clear();
        int totalSlots = 0;
        int filledCount = acceptedApplicants.size();
        
        List<Applicant> processedApplicants = new ArrayList<>(acceptedApplicants);

        if (project != null && project.getRoles() != null) {
            for (Map<String, Object> roleMap : project.getRoles()) {
                String roleName = (String) roleMap.get("roleName");
                int slotsCount = 0;
                Object sObj = roleMap.get("slot");
                
                if (sObj instanceof Long) slotsCount = ((Long) sObj).intValue();
                else if (sObj instanceof Integer) slotsCount = (Integer) sObj;
                else if (sObj instanceof String) {
                    try {
                        slotsCount = Integer.parseInt((String) sObj);
                    } catch (Exception e) {}
                }

                totalSlots += slotsCount;

                int acceptedForThisRole = 0;
                // Use iterator to remove processed ones
                java.util.Iterator<Applicant> it = processedApplicants.iterator();
                while (it.hasNext()) {
                    Applicant a = it.next();
                    if (roleName != null && a.getRole() != null && roleName.equalsIgnoreCase(a.getRole())) {
                        teamSlots.add(new TeamAdapter.TeamSlot(roleName, a.getName(), a.getProfilePicture(), true));
                        acceptedForThisRole++;
                        it.remove(); // Mark as handled
                    }
                }

                // Remaining empty slots for this role
                for (int i = acceptedForThisRole; i < slotsCount; i++) {
                    teamSlots.add(new TeamAdapter.TeamSlot(roleName, null, null, false));
                }
            }
        }
        
        // Handle orphaned applicants (roles that no longer exist)
        for (Applicant orphaned : processedApplicants) {
            teamSlots.add(new TeamAdapter.TeamSlot(orphaned.getRole(), orphaned.getName(), orphaned.getProfilePicture(), true));
            // Should we count these in totalSlots? Usually yes, they occupy a "ghost" slot
            totalSlots++;
        }

        teamAdapter.notifyDataSetChanged();
        tvProgresTim.setText(filledCount + "/" + totalSlots);
        tvTeamSlotStatus.setText("SLOT: " + filledCount + "/" + totalSlots + " TERISI");
    }
}
