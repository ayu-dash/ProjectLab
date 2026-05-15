package id.project.lab.ui.application;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import id.project.lab.model.Applicant;

public class LamaranKuFragment extends Fragment {

    private RecyclerView rvApplications;
    private ApplicationAdapter adapter;
    private List<Applicant> applicationList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private View loadingBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lamaranku, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvApplications = view.findViewById(R.id.rv_applications);
        loadingBar = view.findViewById(R.id.pb_loading);

        rvApplications.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ApplicationAdapter(applicationList);
        rvApplications.setAdapter(adapter);

        // Filter buttons
        android.widget.TextView btnAll = view.findViewById(R.id.btn_filter_all);
        android.widget.TextView btnAccepted = view.findViewById(R.id.btn_filter_accepted);
        android.widget.TextView btnPending = view.findViewById(R.id.btn_filter_pending);
        android.widget.TextView btnRejected = view.findViewById(R.id.btn_filter_rejected);

        btnAll.setOnClickListener(v -> {
            fetchApplications(null);
            updateFilterUI(btnAll, btnAccepted, btnPending, btnRejected);
        });
        btnAccepted.setOnClickListener(v -> {
            fetchApplications("ACCEPTED");
            updateFilterUI(btnAccepted, btnAll, btnPending, btnRejected);
        });
        btnPending.setOnClickListener(v -> {
            fetchApplications("PENDING");
            updateFilterUI(btnPending, btnAll, btnAccepted, btnRejected);
        });
        btnRejected.setOnClickListener(v -> {
            fetchApplications("REJECTED");
            updateFilterUI(btnRejected, btnAll, btnAccepted, btnPending);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchApplications(null); // Default refresh all
    }

    private void updateFilterUI(android.widget.TextView active, android.widget.TextView... others) {
        active.setBackgroundResource(R.drawable.bg_brutalist_yellow_shadow);
        for (android.widget.TextView btn : others) {
            btn.setBackgroundResource(R.drawable.bg_brutalist_border);
        }
    }

    private void fetchApplications(String status) {
        if (mAuth.getCurrentUser() == null) return;
        
        loadingBar.setVisibility(View.VISIBLE);
        String uid = mAuth.getCurrentUser().getUid();
        
        com.google.firebase.firestore.Query query = db.collection("applications")
                .whereEqualTo("userId", uid);
        
        if (status != null) {
            query = query.whereEqualTo("status", status);
        }
        
        // query = query.orderBy("timestamp", Query.Direction.DESCENDING);
        
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingBar.setVisibility(View.GONE);
                    applicationList.clear();
                    android.util.Log.d("LamaranKu", "Success: Found " + queryDocumentSnapshots.size() + " applications");
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Applicant app = doc.toObject(Applicant.class);
                        app.setId(doc.getId());
                        applicationList.add(app);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    loadingBar.setVisibility(View.GONE);
                    android.util.Log.e("LamaranKu", "Error: " + e.getMessage());
                    if (getContext() != null) {
                        android.widget.Toast.makeText(getContext(), "Error: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                });
    }
}
