package id.project.lab.ui.notification;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import id.project.lab.R;
import id.project.lab.model.Notification;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressBar pbLoading;
    private View layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvNotifications = findViewById(R.id.rv_notifications);
        pbLoading = findViewById(R.id.pb_loading);
        layoutEmpty = findViewById(R.id.layout_empty);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notificationList);
        rvNotifications.setAdapter(adapter);

        fetchNotifications();
    }

    private void fetchNotifications() {
        if (mAuth.getCurrentUser() == null) return;

        pbLoading.setVisibility(View.VISIBLE);
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pbLoading.setVisibility(View.GONE);
                    notificationList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Notification notif = doc.toObject(Notification.class);
                        notif.setId(doc.getId());
                        notificationList.add(notif);
                        
                        // Mark as read when opened
                        if (!notif.isRead()) {
                            db.collection("users").document(uid)
                                .collection("notifications").document(doc.getId())
                                .update("read", true);
                        }
                    }
                    
                    if (notificationList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                    }
                    
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal mengambil notifikasi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
