package id.project.lab;

import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import id.project.lab.ui.home.HomeFragment;
import id.project.lab.ui.myproject.ProjekkuFragment;
import id.project.lab.ui.profile.ProfileFragment;
import id.project.lab.ui.application.LamaranKuFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Search Button
        android.view.View btnSearch = findViewById(R.id.btn_search);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(this, id.project.lab.ui.project.SearchActivity.class);
                startActivity(intent);
            });
        }

        // Notification Button
        android.view.View btnNotif = findViewById(R.id.btn_notifications);
        if (btnNotif != null) {
            btnNotif.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(this, id.project.lab.ui.notification.NotificationActivity.class);
                startActivity(intent);
            });
        }

        // Bottom Nav Clicks
        LinearLayout bottomNav = findViewById(R.id.bottom_nav);

        // Home
        bottomNav.getChildAt(0).setOnClickListener(v -> {
            loadFragment(new HomeFragment());
            updateBottomNavSelection(0);
        });

        // Projects
        bottomNav.getChildAt(1).setOnClickListener(v -> {
            loadFragment(new ProjekkuFragment());
            updateBottomNavSelection(1);
        });

        // Applications
        bottomNav.getChildAt(2).setOnClickListener(v -> {
            loadFragment(new LamaranKuFragment());
            updateBottomNavSelection(2);
        });

        // Profile
        bottomNav.getChildAt(3).setOnClickListener(v -> {
            loadFragment(new ProfileFragment());
            updateBottomNavSelection(3);
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void updateBottomNavSelection(int position) {
        LinearLayout bottomNav = findViewById(R.id.bottom_nav);
        int[] solidIcons = {R.drawable.ic_home_solid, R.drawable.ic_work_solid, R.drawable.ic_description_solid, R.drawable.ic_person_solid};
        int[] outlineIcons = {R.drawable.ic_home_outline, R.drawable.ic_work_outline, R.drawable.ic_description_outline, R.drawable.ic_person_outline};

        for (int i = 0; i < bottomNav.getChildCount(); i++) {
            LinearLayout item = (LinearLayout) bottomNav.getChildAt(i);
            android.widget.ImageView icon = (android.widget.ImageView) item.getChildAt(0);
            if (i == position) {
                item.setBackgroundColor(android.graphics.Color.parseColor("#FF1D1B20"));
                icon.setImageResource(solidIcons[i]);
                icon.setColorFilter(android.graphics.Color.parseColor("#FFFFFFFF"));
            } else {
                item.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFFFF"));
                icon.setImageResource(outlineIcons[i]);
                icon.setColorFilter(android.graphics.Color.parseColor("#FF1D1B20"));
            }
        }
    }
}
