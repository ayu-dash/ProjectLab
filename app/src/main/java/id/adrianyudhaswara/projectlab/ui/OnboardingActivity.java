package id.adrianyudhaswara.projectlab.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import id.adrianyudhaswara.projectlab.MainActivity;
import id.adrianyudhaswara.projectlab.R;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        Button nextBtn = findViewById(R.id.btn_next);
        if (nextBtn != null) {
            nextBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish(); // Close onboarding
            });
        }
    }
}
