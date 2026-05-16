package id.project.lab.ui.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import android.graphics.Typeface;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import id.project.lab.R;
import id.project.lab.model.Project;
import id.project.lab.ui.ProjectAdapter;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private ProjectAdapter adapter;
    private List<Project> searchResults;
    private TextView tvResultLabel;
    private View sectionRecent, sectionResults;
    private LinearLayout containerRecentItems;

    private List<String> selectedRoles = new ArrayList<>();
    private List<String> selectedTypes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        etSearch = findViewById(R.id.et_search);
        recyclerView = findViewById(R.id.recycler_search_results);
        tvResultLabel = findViewById(R.id.tv_result_label);
        sectionRecent = findViewById(R.id.section_recent);
        sectionResults = findViewById(R.id.section_results);
        containerRecentItems = findViewById(R.id.container_recent_items);

        applyFont(tvResultLabel);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResults = new ArrayList<>();
        adapter = new ProjectAdapter(searchResults, project -> {
            Intent intent = new Intent(this, ProjectDetailActivity.class);
            intent.putExtra("projectId", project.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        setupFilters();
        setupRecentSearches();

        findViewById(R.id.btn_do_search).setOnClickListener(v -> performSearch());

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });
    }

    private void setupFilters() {
        // Roles
        int[] roleIds = {R.id.filter_developer, R.id.filter_designer, R.id.filter_marketer, R.id.filter_writer};
        String[] roleNames = {"DEVELOPER", "DESIGNER", "MARKETER", "WRITER"};
        
        for (int i = 0; i < roleIds.length; i++) {
            final String role = roleNames[i];
            TextView btn = findViewById(roleIds[i]);
            if (btn != null) {
                // All start as unselected
                btn.setBackgroundResource(R.drawable.bg_brutalist_border);
                
                btn.setOnClickListener(v -> toggleFilter(btn, role, selectedRoles));
            }
        }

        // Types
        int[] typeIds = {R.id.filter_sukarela, R.id.filter_berbayar, R.id.filter_bagi_hasil};
        String[] typeNames = {"SUKARELA", "BERBAYAR", "BAGI HASIL"};

        for (int i = 0; i < typeIds.length; i++) {
            final String type = typeNames[i];
            TextView btn = findViewById(typeIds[i]);
            if (btn != null) {
                btn.setBackgroundResource(R.drawable.bg_brutalist_border);
                btn.setOnClickListener(v -> toggleFilter(btn, type, selectedTypes));
            }
        }

        findViewById(R.id.btn_pilih_semua_roles).setOnClickListener(v -> {
            selectedRoles.clear();
            for (String r : roleNames) selectedRoles.add(r);
            for (int id : roleIds) {
                TextView btn = findViewById(id);
                if (btn != null) btn.setBackgroundResource(R.drawable.bg_brutalist_selected);
            }
        });
    }

    private void toggleFilter(TextView btn, String value, List<String> list) {
        if (list.contains(value)) {
            list.remove(value);
            btn.setBackgroundResource(R.drawable.bg_brutalist_border);
        } else {
            list.add(value);
            btn.setBackgroundResource(R.drawable.bg_brutalist_selected);
        }
    }

    private void applyFont(TextView tv) {
        Typeface font = ResourcesCompat.getFont(this, R.font.space_grotesk_bold);
        tv.setTypeface(font);
    }

    private void setupRecentSearches() {
        loadRecentSearches();

        findViewById(R.id.btn_clear_recent).setOnClickListener(v -> {
            getSharedPreferences("search_prefs", MODE_PRIVATE).edit().clear().apply();
            containerRecentItems.removeAllViews();
            sectionRecent.setVisibility(View.GONE);
        });
    }

    private void loadRecentSearches() {
        containerRecentItems.removeAllViews();
        android.content.SharedPreferences prefs = getSharedPreferences("search_prefs", MODE_PRIVATE);
        String saved = prefs.getString("recent_queries", "");
        
        if (saved.isEmpty()) {
            sectionRecent.setVisibility(View.GONE);
            return;
        }

        sectionRecent.setVisibility(View.VISIBLE);
        String[] queries = saved.split("\\|");
        // Show max 5 recent searches
        int count = 0;
        for (int i = queries.length - 1; i >= 0 && count < 5; i--) {
            final String q = queries[i];
            if (q.isEmpty()) continue;
            
            View itemView = getLayoutInflater().inflate(R.layout.item_recent_search, containerRecentItems, false);
            TextView tv = itemView.findViewById(R.id.tv_recent_text);
            if (tv != null) tv.setText(q);
            itemView.setOnClickListener(v -> {
                etSearch.setText(q);
                performSearch();
            });
            containerRecentItems.addView(itemView);
            count++;
        }
    }

    private void saveRecentSearch(String query) {
        if (query.isEmpty()) return;
        
        android.content.SharedPreferences prefs = getSharedPreferences("search_prefs", MODE_PRIVATE);
        String saved = prefs.getString("recent_queries", "");
        
        // Avoid duplicates and keep it simple with pipe separator
        if (saved.contains(query)) {
            saved = saved.replace(query + "|", "").replace("|" + query, "").replace(query, "");
        }
        
        if (!saved.isEmpty() && !saved.endsWith("|")) saved += "|";
        saved += query;
        
        prefs.edit().putString("recent_queries", saved).apply();
    }

    private void performSearch() {
        String queryText = etSearch.getText().toString().trim().toLowerCase();
        
        if (!queryText.isEmpty()) {
            saveRecentSearch(etSearch.getText().toString().trim());
        }
        
        sectionRecent.setVisibility(View.GONE);
        sectionResults.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("projects").orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                searchResults.clear();
                String[] queryTokens = queryText.split("\\s+");

                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Project project = doc.toObject(Project.class);
                    project.setId(doc.getId());

                    boolean matches = true;

                    // 1. Fuzzy Token Search (Semua kata harus ada di Judul atau Deskripsi)
                    if (!queryText.isEmpty()) {
                        String fullContent = (project.getTitle() + " " + project.getDescription()).toLowerCase();
                        for (String token : queryTokens) {
                            if (!fullContent.contains(token)) {
                                matches = false;
                                break;
                            }
                        }
                    }

                    if (matches && !selectedTypes.isEmpty()) {
                        if (!selectedTypes.contains(project.getType().toUpperCase())) matches = false;
                    }

                    if (matches && !selectedRoles.isEmpty()) {
                        boolean roleFound = false;
                        if (project.getRoles() != null) {
                            for (Map<String, Object> role : project.getRoles()) {
                                String roleName = (String) role.get("roleName");
                                if (roleName != null) {
                                    for (String sel : selectedRoles) {
                                        if (roleName.toUpperCase().contains(sel)) { roleFound = true; break; }
                                    }
                                }
                                if (roleFound) break;
                            }
                        }
                        if (!roleFound) matches = false;
                    }
                    
                    if (matches && !"BUKA".equals(project.getStatus() != null ? project.getStatus() : "BUKA")) matches = false;

                    if (matches) {
                        String category = "[PROJEK]";
                        if (project.getRoles() != null && !project.getRoles().isEmpty()) {
                            Object rn = project.getRoles().get(0).get("roleName");
                            category = "[" + (rn != null ? rn.toString().toUpperCase() : "MEMBER") + "]";
                        }
                        project.setCategory(category);
                        searchResults.add(project);
                    }
                }
                adapter.notifyDataSetChanged();
                if (searchResults.isEmpty()) tvResultLabel.setText("TIDAK ADA HASIL");
                else tvResultLabel.setText("HASIL PENCARIAN (" + searchResults.size() + ")");
            });
    }
}
