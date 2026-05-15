package id.project.lab.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import id.project.lab.R;
import id.project.lab.ui.auth.LoginActivity;
public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvRole, tvProjCount, tvAppCount;
    private TextView tvWhatsapp, tvDiscord, tvEmailContact;
    private LinearLayout layoutPortfolioList;
    private ImageView ivProfile;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvRole = view.findViewById(R.id.tv_role_badge);
        tvProjCount = view.findViewById(R.id.tv_count_projects);
        tvAppCount = view.findViewById(R.id.tv_count_applications);
        ivProfile = view.findViewById(R.id.iv_profile);

        tvWhatsapp = view.findViewById(R.id.tv_whatsapp);
        tvDiscord = view.findViewById(R.id.tv_discord);
        tvEmailContact = view.findViewById(R.id.tv_email_contact);
        layoutPortfolioList = view.findViewById(R.id.layout_portfolio_list);

        loadUserData();
        loadStats();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            Glide.with(this)
                .load(user.getPhotoUrl())
                .placeholder(R.drawable.ic_person_solid)
                .into(ivProfile);
        }

        view.findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
        loadStats();
    }

    private void loadUserData() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    String email = documentSnapshot.getString("email");
                    String role = documentSnapshot.getString("role");
                    
                    String wa = documentSnapshot.getString("wa");
                    String discord = documentSnapshot.getString("discord");
                    String portfolioStr = documentSnapshot.getString("portfolio");

                    tvName.setText(name != null ? name.toUpperCase() : "USER");
                    tvEmail.setText(email != null ? email : "");
                    tvRole.setText(role != null ? role.toUpperCase() : "MEMBER");

                    // Load Portfolios
                    if (portfolioStr != null && !portfolioStr.isEmpty()) {
                        layoutPortfolioList.removeAllViews();
                        String[] portfolios = portfolioStr.split(", ");
                        for (String link : portfolios) {
                            TextView tvLink = new TextView(getContext());
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    (int) (64 * getResources().getDisplayMetrics().density)
                            );
                            params.setMargins(0, 0, 0, (int) (12 * getResources().getDisplayMetrics().density));
                            tvLink.setLayoutParams(params);
                            tvLink.setBackgroundResource(R.drawable.bg_brutalist_shadow);
                            tvLink.setText(link);
                            tvLink.setGravity(android.view.Gravity.CENTER_VERTICAL);
                            tvLink.setPadding((int) (16 * getResources().getDisplayMetrics().density), 0, (int) (16 * getResources().getDisplayMetrics().density), 0);
                            tvLink.setTextColor(android.graphics.Color.parseColor("#FF1D1B20"));
                            tvLink.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                            tvLink.setOnClickListener(v -> openLink(link.startsWith("http") ? link : "https://" + link));
                            layoutPortfolioList.addView(tvLink);
                        }
                    }

                    if (wa != null && !wa.isEmpty()) {
                        tvWhatsapp.setText(wa);
                        getView().findViewById(R.id.layout_whatsapp).setOnClickListener(v -> openLink("https://wa.me/" + wa));
                        getView().findViewById(R.id.layout_whatsapp).setVisibility(android.view.View.VISIBLE);
                    } else {
                        getView().findViewById(R.id.layout_whatsapp).setVisibility(android.view.View.GONE);
                    }

                    if (discord != null && !discord.isEmpty()) {
                        tvDiscord.setText(discord);
                        getView().findViewById(R.id.layout_discord).setVisibility(android.view.View.VISIBLE);
                    } else {
                        getView().findViewById(R.id.layout_discord).setVisibility(android.view.View.GONE);
                    }

                    if (email != null && !email.isEmpty()) {
                        tvEmailContact.setText(email);
                        getView().findViewById(R.id.layout_email_contact).setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(android.net.Uri.parse("mailto:" + email));
                            startActivity(intent);
                        });
                        getView().findViewById(R.id.layout_email_contact).setVisibility(android.view.View.VISIBLE);
                    } else {
                        getView().findViewById(R.id.layout_email_contact).setVisibility(android.view.View.GONE);
                    }
                }
            });
    }

    private void openLink(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Gagal membuka link", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadStats() {
        String uid = mAuth.getCurrentUser().getUid();
        
        // Count user's projects
        db.collection("projects").whereEqualTo("ownerId", uid).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                tvProjCount.setText(String.valueOf(queryDocumentSnapshots.size()));
            });

        // Count user's applications
        db.collection("applications").whereEqualTo("userId", uid).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                tvAppCount.setText(String.valueOf(queryDocumentSnapshots.size()));
            });
    }
}
