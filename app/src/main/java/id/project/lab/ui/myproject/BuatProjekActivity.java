package id.project.lab.ui.myproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.project.lab.R;

public class BuatProjekActivity extends AppCompatActivity {

    private ImageView ivBanner;
    private View btnPilihGambar;

    private TextView btnSukarela;
    private TextView btnBagiHasil;
    private TextView btnBerbayar;

    private EditText etPosisi;
    private EditText etSlot;
    private LinearLayout containerRoles;

    private String selectedKompensasi = "SUKARELA";
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivBanner.setImageURI(uri);
                    if (btnPilihGambar != null) {
                        btnPilihGambar.setVisibility(View.GONE);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buat_projek);

        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        ivBanner = findViewById(R.id.iv_banner);
        btnPilihGambar = findViewById(R.id.btn_pilih_gambar);
        
        View containerBanner = findViewById(R.id.container_banner);
        if (containerBanner != null) {
            containerBanner.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        }

        btnSukarela = findViewById(R.id.btn_sukarela);
        btnBagiHasil = findViewById(R.id.btn_bagi_hasil);
        btnBerbayar = findViewById(R.id.btn_berbayar);

        setupCompensationButtons();

        etPosisi = findViewById(R.id.et_posisi);
        etSlot = findViewById(R.id.et_slot);
        containerRoles = findViewById(R.id.container_roles);
        
        View btnAddRole = findViewById(R.id.btn_add_role);
        if (btnAddRole != null) {
            btnAddRole.setOnClickListener(v -> addRole());
        }

        View btnPosting = findViewById(R.id.btn_posting);
        if (btnPosting != null) {
            btnPosting.setOnClickListener(v -> postingProjek());
        }
    }

    private void postingProjek() {
        EditText etNamaProjek = findViewById(R.id.et_nama_projek);
        EditText etDeskripsi = findViewById(R.id.et_deskripsi);

        String nama = etNamaProjek.getText().toString().trim();
        String deskripsi = etDeskripsi.getText().toString().trim();

        if (nama.isEmpty()) {
            Toast.makeText(this, "Nama Projek tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (deskripsi.isEmpty()) {
            Toast.makeText(this, "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        int roleCount = containerRoles.getChildCount();
        if (roleCount == 0) {
            Toast.makeText(this, "Minimal tambahkan 1 Role", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kumpulkan data roles dari container
        List<Map<String, Object>> rolesList = new ArrayList<>();
        for (int i = 0; i < roleCount; i++) {
            View roleView = containerRoles.getChildAt(i);
            TextView tvRoleName = roleView.findViewById(R.id.tv_role_name);
            TextView tvRoleSlot = roleView.findViewById(R.id.tv_role_slot);
            
            if (tvRoleName != null && tvRoleSlot != null) {
                String roleName = tvRoleName.getText().toString();
                String roleSlotStr = tvRoleSlot.getText().toString().replace(" SLOT", "").trim();
                int slot = 1;
                try {
                    slot = Integer.parseInt(roleSlotStr);
                } catch (NumberFormatException ignored) {}

                Map<String, Object> roleMap = new HashMap<>();
                roleMap.put("roleName", roleName);
                roleMap.put("slot", slot);
                rolesList.add(roleMap);
            }
        }

        // Siapkan data untuk Firestore
        Map<String, Object> projectData = new HashMap<>();
        projectData.put("title", nama);
        projectData.put("description", deskripsi);
        projectData.put("type", selectedKompensasi);
        projectData.put("roles", rolesList);
        projectData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
        
        String currentUserId = FirebaseAuth.getInstance().getUid();
        projectData.put("ownerId", currentUserId);

        // Disable tombol biar gak ke-klik 2 kali
        View btnPosting = findViewById(R.id.btn_posting);
        if (btnPosting != null) btnPosting.setEnabled(false);

        if (selectedImageUri != null) {
            Toast.makeText(this, "Memproses dan menyimpan gambar...", Toast.LENGTH_SHORT).show();
            String base64Image = encodeImageToBase64(selectedImageUri);
            if (base64Image != null) {
                projectData.put("banner_base64", base64Image);
            }
        } else {
            Toast.makeText(this, "Menyimpan ke database...", Toast.LENGTH_SHORT).show();
        }
        
        saveToFirestore(projectData, btnPosting);
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
            if (selectedImage == null) return null;

            // Resize image to keep it small (e.g. max width 600) so it fits in Firestore's 1MB limit
            int maxWidth = 600;
            float scale = (float) maxWidth / selectedImage.getWidth();
            if (scale < 1.0f) {
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                selectedImage = Bitmap.createBitmap(selectedImage, 0, 0, selectedImage.getWidth(), selectedImage.getHeight(), matrix, true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.JPEG, 50, baos); // Compress to 50%
            byte[] b = baos.toByteArray();
            
            return "data:image/jpeg;base64," + Base64.encodeToString(b, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveToFirestore(Map<String, Object> projectData, View btnPosting) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("projects").add(projectData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Projek berhasil diposting!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memposting: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    if (btnPosting != null) btnPosting.setEnabled(true);
                });
    }

    private void setupCompensationButtons() {
        View.OnClickListener listener = v -> {
            // Reset all
            if (btnSukarela != null) btnSukarela.setBackgroundResource(R.drawable.bg_brutalist_unselected);
            if (btnBagiHasil != null) btnBagiHasil.setBackgroundResource(R.drawable.bg_brutalist_unselected);
            if (btnBerbayar != null) btnBerbayar.setBackgroundResource(R.drawable.bg_brutalist_unselected);

            // Set selected
            v.setBackgroundResource(R.drawable.bg_brutalist_selected);

            // Set kompensasi state
            if (v.getId() == R.id.btn_sukarela) {
                selectedKompensasi = "SUKARELA";
            } else if (v.getId() == R.id.btn_bagi_hasil) {
                selectedKompensasi = "BAGI HASIL";
            } else if (v.getId() == R.id.btn_berbayar) {
                selectedKompensasi = "BERBAYAR";
            }
        };

        if (btnSukarela != null) btnSukarela.setOnClickListener(listener);
        if (btnBagiHasil != null) btnBagiHasil.setOnClickListener(listener);
        if (btnBerbayar != null) btnBerbayar.setOnClickListener(listener);
    }

    private void addRole() {
        if (etPosisi == null || etSlot == null || containerRoles == null) return;
        
        String posisi = etPosisi.getText().toString().trim();
        String slot = etSlot.getText().toString().trim();

        if (posisi.isEmpty() || slot.isEmpty()) {
            Toast.makeText(this, "Posisi dan Slot harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        View roleView = LayoutInflater.from(this).inflate(R.layout.item_role_row, containerRoles, false);
        
        TextView tvRoleName = roleView.findViewById(R.id.tv_role_name);
        TextView tvRoleSlot = roleView.findViewById(R.id.tv_role_slot);
        View btnDeleteRole = roleView.findViewById(R.id.btn_delete_role);

        if (tvRoleName != null) tvRoleName.setText(posisi.toUpperCase());
        if (tvRoleSlot != null) tvRoleSlot.setText(slot + " SLOT");

        if (btnDeleteRole != null) {
            btnDeleteRole.setOnClickListener(v -> containerRoles.removeView(roleView));
        }

        containerRoles.addView(roleView);

        // Clear input
        etPosisi.setText("");
        etSlot.setText("");
    }
}
