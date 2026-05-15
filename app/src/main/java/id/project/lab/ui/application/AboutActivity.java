package id.project.lab.ui.application;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import id.project.lab.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.btn_ig_adrian).setOnClickListener(v -> openUrl("https://instagram.com/_ydhrs"));
        findViewById(R.id.btn_ig_kia).setOnClickListener(v -> openUrl("https://instagram.com/kianieyo"));
        findViewById(R.id.btn_ig_pani).setOnClickListener(v -> openUrl("https://instagram.com/pani"));
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal membuka tautan", Toast.LENGTH_SHORT).show();
        }
    }
}
