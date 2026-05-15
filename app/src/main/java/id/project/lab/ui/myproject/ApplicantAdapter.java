package id.project.lab.ui.myproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.project.lab.R;
import id.project.lab.model.Applicant;

public class ApplicantAdapter extends RecyclerView.Adapter<ApplicantAdapter.ViewHolder> {

    private List<Applicant> applicants;
    private OnApplicantActionListener listener;

    public interface OnApplicantActionListener {
        void onAccept(Applicant applicant);
        void onReject(Applicant applicant);
    }

    public ApplicantAdapter(List<Applicant> applicants, OnApplicantActionListener listener) {
        this.applicants = applicants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pelamar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Applicant applicant = applicants.get(position);
        holder.tvName.setText(applicant.getName());
        holder.tvRole.setText(applicant.getRole());
        
        if (applicant.getMessage() != null && !applicant.getMessage().isEmpty()) {
            holder.tvMessage.setVisibility(View.VISIBLE);
            holder.tvMessage.setText(applicant.getMessage());
        } else {
            holder.tvMessage.setVisibility(View.GONE);
        }
        
        // Fetch user profile photo
        String photoUrlFromApp = applicant.getProfilePicture();
        if (photoUrlFromApp != null && !photoUrlFromApp.isEmpty()) {
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(photoUrlFromApp)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(holder.ivAvatar);
        } else {
            // Fallback: fetch from user collection
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(applicant.getUserId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Check multiple possible field names
                        String photo = documentSnapshot.getString("profilePicture");
                        if (photo == null) photo = documentSnapshot.getString("photoUrl");
                        if (photo == null) photo = documentSnapshot.getString("avatar");
                        
                        if (photo != null && !photo.isEmpty()) {
                            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                                .load(photo)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .into(holder.ivAvatar);
                        }
                    }
                });
        }
        
        // Populate Portfolio Links
        holder.containerLinks.removeAllViews();
        if (applicant.getPortfolioLinks() != null) {
            for (String link : applicant.getPortfolioLinks()) {
                TextView tvLink = new TextView(holder.itemView.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, (int) (4 * holder.itemView.getContext().getResources().getDisplayMetrics().density));
                tvLink.setLayoutParams(params);
                tvLink.setText(link);
                tvLink.setTextSize(12f);
                tvLink.setTextColor(android.graphics.Color.parseColor("#FF494551"));
                tvLink.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_open_in_new, 0, 0, 0);
                tvLink.setCompoundDrawablePadding((int) (8 * holder.itemView.getContext().getResources().getDisplayMetrics().density));
                
                // Click listener to open in browser
                tvLink.setOnClickListener(v -> {
                    String url = link;
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://" + url;
                    }
                    try {
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
                        holder.itemView.getContext().startActivity(intent);
                    } catch (Exception e) {
                        android.widget.Toast.makeText(holder.itemView.getContext(), "Gagal membuka link", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
                
                try {
                    android.graphics.Typeface font = androidx.core.content.res.ResourcesCompat.getFont(holder.itemView.getContext(), R.font.space_grotesk);
                    tvLink.setTypeface(font);
                } catch (Exception e) {}
                
                holder.containerLinks.addView(tvLink);
            }
        }

        holder.btnTerima.setOnClickListener(v -> listener.onAccept(applicant));
        holder.btnTolak.setOnClickListener(v -> listener.onReject(applicant));
    }

    @Override
    public int getItemCount() {
        return applicants.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvMessage, btnTerima;
        ImageView ivAvatar, btnTolak;
        android.widget.LinearLayout containerLinks;

        public ViewHolder(@androidx.annotation.NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_pelamar_name);
            tvRole = itemView.findViewById(R.id.tv_pelamar_role);
            tvMessage = itemView.findViewById(R.id.tv_pelamar_message);
            ivAvatar = itemView.findViewById(R.id.iv_pelamar_avatar);
            btnTerima = itemView.findViewById(R.id.btn_terima);
            btnTolak = itemView.findViewById(R.id.btn_tolak);
            containerLinks = itemView.findViewById(R.id.container_links);
        }
    }
}
