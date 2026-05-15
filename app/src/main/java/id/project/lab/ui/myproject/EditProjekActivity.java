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

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.project.lab.R;
import id.project.lab.model.Project;

public class EditProjekActivity extends AppCompatActivity {

    private String projectId;
    private Project project;

    private EditText etNamaProjek, etDeskripsi;
    private ImageView ivBanner;
    private View btnPilihGambar;

    private View scrollContent, bottomActionBar, loadingState;

    private TextView btnSukarela, btnBagiHasil, btnBerbayar;
    private EditText etPosisi, etSlot;
    private LinearLayout containerRoles;

    private String selectedKompensasi = "SUKARELA";
    private Uri selectedImageUri = null;
    private String currentBannerBase64 = null;

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
        setContentView(R.layout.activity_edit_projek);

        projectId = getIntent().getStringExtra("PROJECT_ID");
        if (projectId == null) {
            Toast.makeText(this, "ID Projek tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadProjectData();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        etNamaProjek = findViewById(R.id.et_nama_projek);
        etDeskripsi = findViewById(R.id.et_deskripsi);
        ivBanner = findViewById(R.id.iv_banner);
        btnPilihGambar = findViewById(R.id.btn_pilih_gambar);
        
        scrollContent = findViewById(R.id.scroll_content);
        bottomActionBar = findViewById(R.id.bottom_action_bar);
        loadingState = findViewById(R.id.loading_state);
        
        findViewById(R.id.container_banner).setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSukarela = findViewById(R.id.btn_sukarela);
        btnBagiHasil = findViewById(R.id.btn_bagi_hasil);
        btnBerbayar = findViewById(R.id.btn_berbayar);

        setupCompensationButtons();

        etPosisi = findViewById(R.id.et_posisi);
        etSlot = findViewById(R.id.et_slot);
        containerRoles = findViewById(R.id.container_roles);
        
        findViewById(R.id.btn_add_role).setOnClickListener(v -> addRole());
        findViewById(R.id.btn_simpan).setOnClickListener(v -> updateProjek());
    }

    private void loadProjectData() {
        FirebaseFirestore.getInstance().collection("projects").document(projectId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    project = documentSnapshot.toObject(Project.class);
                    if (project != null) {
                        fillForm();
                        
                        // Hide loading, show content
                        loadingState.setVisibility(View.GONE);
                        scrollContent.setVisibility(View.VISIBLE);
                        bottomActionBar.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    loadingState.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void fillForm() {
        etNamaProjek.setText(project.getTitle());
        etDeskripsi.setText(project.getDescription());
        
        if (project.getType() != null) {
            selectedKompensasi = project.getType();
        }
        updateCompensationUI();

        if (project.getBanner_base64() != null && !project.getBanner_base64().isEmpty()) {
            currentBannerBase64 = project.getBanner_base64();
            try {
                String pureBase64 = currentBannerBase64;
                if (pureBase64.contains(",")) {
                    pureBase64 = pureBase64.substring(pureBase64.indexOf(",") + 1);
                }
                byte[] decodedString = Base64.decode(pureBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedByte != null) {
                    ivBanner.setImageBitmap(decodedByte);
                    btnPilihGambar.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (project.getRoles() != null) {
            containerRoles.removeAllViews();
            for (Map<String, Object> role : project.getRoles()) {
                String name = (String) role.get("roleName");
                int slot = 0;
                Object slotObj = role.get("slot");
                if (slotObj instanceof Long) {
                    slot = ((Long) slotObj).intValue();
                } else if (slotObj instanceof Integer) {
                    slot = (Integer) slotObj;
                }
                if (name != null) {
                    addRoleToContainer(name, slot);
                }
            }
        }
    }

    private void updateCompensationUI() {
        btnSukarela.setBackgroundResource(R.id.btn_sukarela == getResId(selectedKompensasi) ? R.drawable.bg_brutalist_selected : R.drawable.bg_brutalist_unselected);
        
        // Simplified reset and set
        btnSukarela.setBackgroundResource(R.drawable.bg_brutalist_unselected);
        btnBagiHasil.setBackgroundResource(R.drawable.bg_brutalist_unselected);
        btnBerbayar.setBackgroundResource(R.drawable.bg_brutalist_unselected);

        if ("SUKARELA".equals(selectedKompensasi)) {
            btnSukarela.setBackgroundResource(R.drawable.bg_brutalist_selected);
        } else if ("BAGI HASIL".equals(selectedKompensasi)) {
            btnBagiHasil.setBackgroundResource(R.drawable.bg_brutalist_selected);
        } else if ("BERBAYAR".equals(selectedKompensasi)) {
            btnBerbayar.setBackgroundResource(R.drawable.bg_brutalist_selected);
        }
    }

    private int getResId(String type) {
        if ("SUKARELA".equals(type)) return R.id.btn_sukarela;
        if ("BAGI HASIL".equals(type)) return R.id.btn_bagi_hasil;
        if ("BERBAYAR".equals(type)) return R.id.btn_berbayar;
        return R.id.btn_sukarela;
    }

    private void setupCompensationButtons() {
        View.OnClickListener listener = v -> {
            if (v.getId() == R.id.btn_sukarela) selectedKompensasi = "SUKARELA";
            else if (v.getId() == R.id.btn_bagi_hasil) selectedKompensasi = "BAGI HASIL";
            else if (v.getId() == R.id.btn_berbayar) selectedKompensasi = "BERBAYAR";
            updateCompensationUI();
        };

        btnSukarela.setOnClickListener(listener);
        btnBagiHasil.setOnClickListener(listener);
        btnBerbayar.setOnClickListener(listener);
    }

    private void addRole() {
        String posisi = etPosisi.getText().toString().trim();
        String slotStr = etSlot.getText().toString().trim();

        if (posisi.isEmpty() || slotStr.isEmpty()) {
            Toast.makeText(this, "Posisi dan Slot harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        int slot = Integer.parseInt(slotStr);
        addRoleToContainer(posisi, slot);

        etPosisi.setText("");
        etSlot.setText("");
    }

    private void addRoleToContainer(String name, int slot) {
        View roleView = LayoutInflater.from(this).inflate(R.layout.item_role_row, containerRoles, false);
        TextView tvRoleName = roleView.findViewById(R.id.tv_role_name);
        TextView tvRoleSlot = roleView.findViewById(R.id.tv_role_slot);
        View btnDeleteRole = roleView.findViewById(R.id.btn_delete_role);

        tvRoleName.setText(name.toUpperCase());
        tvRoleSlot.setText(slot + " SLOT");
        btnDeleteRole.setOnClickListener(v -> containerRoles.removeView(roleView));

        containerRoles.addView(roleView);
    }

    private void updateProjek() {
        String nama = etNamaProjek.getText().toString().trim();
        String deskripsi = etDeskripsi.getText().toString().trim();

        if (nama.isEmpty() || deskripsi.isEmpty()) {
            Toast.makeText(this, "Nama dan Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Map<String, Object>> rolesList = new ArrayList<>();
        for (int i = 0; i < containerRoles.getChildCount(); i++) {
            View roleView = containerRoles.getChildAt(i);
            TextView tvRoleName = roleView.findViewById(R.id.tv_role_name);
            TextView tvRoleSlot = roleView.findViewById(R.id.tv_role_slot);
            
            String roleName = tvRoleName.getText().toString();
            int slot = Integer.parseInt(tvRoleSlot.getText().toString().replace(" SLOT", ""));

            Map<String, Object> roleMap = new HashMap<>();
            roleMap.put("roleName", roleName);
            roleMap.put("slot", slot);
            rolesList.add(roleMap);
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", nama);
        updates.put("description", deskripsi);
        updates.put("type", selectedKompensasi);
        updates.put("roles", rolesList);

        if (selectedImageUri != null) {
            String base64Image = encodeImageToBase64(selectedImageUri);
            if (base64Image != null) {
                updates.put("banner_base64", base64Image);
            }
        }

        FirebaseFirestore.getInstance().collection("projects").document(projectId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Perubahan disimpan!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal menyimpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
            if (selectedImage == null) return null;

            int maxWidth = 600;
            float scale = (float) maxWidth / selectedImage.getWidth();
            if (scale < 1.0f) {
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                selectedImage = Bitmap.createBitmap(selectedImage, 0, 0, selectedImage.getWidth(), selectedImage.getHeight(), matrix, true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] b = baos.toByteArray();
            return "data:image/jpeg;base64," + Base64.encodeToString(b, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
