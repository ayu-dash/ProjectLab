package id.project.lab.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import id.project.lab.R;
import id.project.lab.model.Project;
import id.project.lab.ui.ProjectAdapter;
import id.project.lab.ui.project.ProjectDetailActivity;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProjectAdapter adapter;
    private List<Project> projectList; // Current displayed list
    private List<Project> allProjects; // Master list
    private String currentFilter = "SEMUA";
    private android.widget.TextView btnAll, btnSukarela, btnBerbayar, btnBagiHasil;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_projects);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        projectList = new ArrayList<>();
        allProjects = new ArrayList<>();
        adapter = new ProjectAdapter(projectList, project -> {
            Intent intent = new Intent(requireContext(), ProjectDetailActivity.class);
            intent.putExtra("title", project.getTitle());
            intent.putExtra("type", project.getType());
            intent.putExtra("description", project.getDescription());
            intent.putExtra("ownerId", project.getOwnerId());
            intent.putExtra("projectId", project.getId());
            intent.putExtra("reward", project.getType().equals("SUKARELA") ? "Sertifikat" : project.getType().equals("BERBAYAR") ? "Rp 2.000.000" : "Bagi Hasil 10%");
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Initialize Filters
        btnAll = view.findViewById(R.id.filter_all);
        btnSukarela = view.findViewById(R.id.filter_sukarela);
        btnBerbayar = view.findViewById(R.id.filter_berbayar);
        btnBagiHasil = view.findViewById(R.id.filter_bagi_hasil);

        if (btnAll != null) btnAll.setOnClickListener(v -> applyFilter("SEMUA"));
        if (btnSukarela != null) btnSukarela.setOnClickListener(v -> applyFilter("SUKARELA"));
        if (btnBerbayar != null) btnBerbayar.setOnClickListener(v -> applyFilter("BERBAYAR"));
        if (btnBagiHasil != null) btnBagiHasil.setOnClickListener(v -> applyFilter("BAGI HASIL"));

        loadProjectsFromFirestore();

        View fabCreateProject = view.findViewById(R.id.fab_create_project);
        if (fabCreateProject != null) {
            fabCreateProject.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), id.project.lab.ui.myproject.BuatProjekActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        updateFilterUI();
        
        projectList.clear();
        for (Project p : allProjects) {
            if (filter.equals("SEMUA") || filter.equalsIgnoreCase(p.getType())) {
                projectList.add(p);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateFilterUI() {
        android.widget.TextView[] buttons = {btnAll, btnSukarela, btnBerbayar, btnBagiHasil};
        String[] filters = {"SEMUA", "SUKARELA", "BERBAYAR", "BAGI HASIL"};
        
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] == null) continue;
            if (filters[i].equals(currentFilter)) {
                buttons[i].setBackgroundResource(R.drawable.bg_brutalist_selected);
            } else {
                buttons[i].setBackgroundResource(R.drawable.bg_brutalist_border);
            }
        }
    }

    private void loadProjectsFromFirestore() {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("projects")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        android.util.Log.e("HomeFragment", "Firestore error: " + error.getMessage());
                        return;
                    }
                    if (value != null) {
                        allProjects.clear();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                            try {
                                Project project = doc.toObject(Project.class);
                                project.setId(doc.getId());
                                
                                // SAFETY: If status is null, treat as BUKA
                                String status = project.getStatus() != null ? project.getStatus() : "BUKA";
                                if ("TUTUP".equals(status)) continue;
                                
                                // Normalize category display label
                                String category = "[PROJEK]";
                                if (project.getRoles() != null && !project.getRoles().isEmpty()) {
                                    Object roleNameObj = project.getRoles().get(0).get("roleName");
                                    String firstRole = roleNameObj != null ? roleNameObj.toString() : "MEMBER";
                                    if (project.getRoles().size() > 1) {
                                        category = "[" + firstRole.toUpperCase() + ", " + (project.getRoles().size() - 1) + " ROLE LAINNYA]";
                                    } else {
                                        category = "[" + firstRole.toUpperCase() + "]";
                                    }
                                }
                                project.setCategory(category);
                                
                                allProjects.add(project);
                            } catch (Exception e) {
                                android.util.Log.e("HomeFragment", "Error mapping project: " + e.getMessage());
                            }
                        }
                        applyFilter(currentFilter);
                        
                        // Show toast if no projects found at all
                        if (allProjects.isEmpty()) {
                            Toast.makeText(getContext(), "Belum ada projek yang tersedia", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
