package id.project.lab.ui.myproject;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import id.project.lab.R;
import id.project.lab.model.Project;

public class ProjekkuFragment extends Fragment {

    private RecyclerView rvProjekku;
    private MyProjectAdapter adapter;
    private List<Project> projectList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_projekku, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        projectList = new ArrayList<>();

        rvProjekku = view.findViewById(R.id.rv_projekku);
        rvProjekku.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new MyProjectAdapter(projectList, new MyProjectAdapter.OnProjectActionListener() {
            @Override
            public void onManage(Project project) {
                android.content.Intent intent = new android.content.Intent(getActivity(), KelolaProjekActivity.class);
                intent.putExtra("PROJECT_ID", project.getId());
                startActivity(intent);
            }

            @Override
            public void onEdit(Project project) {
                android.content.Intent intent = new android.content.Intent(getActivity(), EditProjekActivity.class);
                intent.putExtra("PROJECT_ID", project.getId());
                startActivity(intent);
            }
        });
        rvProjekku.setAdapter(adapter);

        fetchMyProjects();

        View fabCreateProject = view.findViewById(R.id.fab_create_project);
        if (fabCreateProject != null) {
            fabCreateProject.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(getActivity(), BuatProjekActivity.class);
                startActivity(intent);
            });
        }
        return view;
    }

    private void fetchMyProjects() {
        String uid = auth.getUid();
        if (uid == null) return;

        db.collection("projects")
                .whereEqualTo("ownerId", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Gagal mengambil data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        projectList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Project p = doc.toObject(Project.class);
                            p.setId(doc.getId());
                            projectList.add(p);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
