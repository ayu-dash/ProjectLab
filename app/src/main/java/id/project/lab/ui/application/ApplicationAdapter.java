package id.project.lab.ui.application;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import id.project.lab.R;
import id.project.lab.model.Applicant;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private List<Applicant> list;

    public ApplicationAdapter(List<Applicant> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Applicant app = list.get(position);

        holder.tvTitle.setText(app.getProjectTitle() != null ? app.getProjectTitle().toUpperCase() : "PROJEK TANPA JUDUL");
        holder.tvRole.setText(app.getRole() != null ? app.getRole().toUpperCase() : "ROLE");
        
        String status = app.getStatus() != null ? app.getStatus() : "PENDING";
        
        // Translate status for UI
        String displayStatus = status;
        int statusColor = android.graphics.Color.parseColor("#FF1D1B20"); // Default Black

        if ("ACCEPTED".equals(status)) {
            displayStatus = "DITERIMA";
            statusColor = android.graphics.Color.parseColor("#FF4CAF50"); // Green
            holder.layoutContact.setVisibility(View.VISIBLE);
            if (app.getProjectId() != null) {
                fetchOwnerContact(app.getProjectId(), holder);
            }
        } else if ("REJECTED".equals(status)) {
            displayStatus = "DITOLAK";
            statusColor = android.graphics.Color.parseColor("#FFF44336"); // Red
            holder.layoutContact.setVisibility(View.GONE);
        } else {
            displayStatus = "MENUNGGU";
            holder.layoutContact.setVisibility(View.GONE);
        }

        holder.tvStatus.setText(displayStatus);
        holder.tvStatus.setBackgroundColor(statusColor);

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), DetailLamaranActivity.class);
            intent.putExtra("applicationId", app.getId());
            intent.putExtra("projectId", app.getProjectId());
            v.getContext().startActivity(intent);
        });

        if (app.getTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.tvDate.setText(sdf.format(new Date(app.getTimestamp())));
        } else {
            holder.tvDate.setText("-");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvRole, tvStatus, tvDate, btnDetail;
        TextView tvContactWA, tvContactDiscord, tvContactEmail;
        View layoutContact;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_project_title);
            tvRole = itemView.findViewById(R.id.tv_role);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvDate = itemView.findViewById(R.id.tv_date);
            btnDetail = itemView.findViewById(R.id.btn_detail);
            layoutContact = itemView.findViewById(R.id.layout_contact);
            tvContactWA = itemView.findViewById(R.id.tv_contact_wa);
            tvContactDiscord = itemView.findViewById(R.id.tv_contact_discord);
            tvContactEmail = itemView.findViewById(R.id.tv_contact_email);
        }
    }

    private void fetchOwnerContact(String projectId, ViewHolder holder) {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("projects").document(projectId).get().addOnSuccessListener(projectDoc -> {
            String ownerId = projectDoc.getString("ownerId");
            if (ownerId != null) {
                db.collection("users").document(ownerId).get().addOnSuccessListener(userDoc -> {
                    String wa = userDoc.getString("wa");
                    if (wa == null) wa = userDoc.getString("whatsapp"); // fallback
                    
                    String discord = userDoc.getString("discord");
                    String email = userDoc.getString("email");
                    
                    holder.tvContactWA.setText("WhatsApp: " + (wa != null && !wa.isEmpty() ? wa : "-"));
                    holder.tvContactDiscord.setText("Discord: " + (discord != null && !discord.isEmpty() ? discord : "-"));
                    holder.tvContactEmail.setText("Email: " + (email != null && !email.isEmpty() ? email : "-"));

                    if (wa != null && !wa.isEmpty()) {
                        String finalWa = wa;
                        holder.tvContactWA.setOnClickListener(v -> {
                            String url = "https://wa.me/" + finalWa.replace("+", "").replace(" ", "").replace("-", "");
                            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
                            v.getContext().startActivity(intent);
                        });
                    }

                    if (discord != null && !discord.isEmpty()) {
                        String finalDiscord = discord;
                        holder.tvContactDiscord.setOnClickListener(v -> {
                            String url = finalDiscord.trim();
                            if (!url.startsWith("http")) {
                                // If it's just a username, we can't easily link to PM, 
                                // but we'll try to treat it as a search or just open discord.com
                                if (url.contains("discord.gg") || url.contains("discord.com")) {
                                    url = "https://" + url;
                                } else {
                                    // Fallback to searching the user or just opening discord
                                    url = "https://discord.com/search?q=" + url;
                                }
                            }
                            try {
                                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
                                // Force browser to avoid app interception if desired, 
                                // but ACTION_VIEW is usually what users want
                                v.getContext().startActivity(intent);
                            } catch (Exception e) {
                                android.widget.Toast.makeText(v.getContext(), "Gagal membuka Discord", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    if (email != null && !email.isEmpty()) {
                        String finalEmail = email;
                        holder.tvContactEmail.setOnClickListener(v -> {
                            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
                            intent.setData(android.net.Uri.parse("mailto:" + finalEmail));
                            v.getContext().startActivity(intent);
                        });
                    }
                });
            }
        });
    }
}
