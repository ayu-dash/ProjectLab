package id.project.lab.ui.myproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import id.project.lab.R;
import id.project.lab.model.Project;
import java.util.List;
import java.util.Map;

public class MyProjectAdapter extends RecyclerView.Adapter<MyProjectAdapter.ViewHolder> {

    private List<Project> projects;
    private OnProjectActionListener listener;

    public interface OnProjectActionListener {
        void onManage(Project project);
        void onEdit(Project project);
    }

    public MyProjectAdapter(List<Project> projects, OnProjectActionListener listener) {
        this.projects = projects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_project, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Project p = projects.get(position);
        holder.tvTitle.setText(p.getTitle().toUpperCase());
        holder.tvApplicants.setText(String.valueOf(p.getApplicantsCount()));
        
        // Calculate total slots from roles
        int totalSlots = 0;
        if (p.getRoles() != null) {
            for (Map<String, Object> role : p.getRoles()) {
                Object slotObj = role.get("slot");
                if (slotObj instanceof Long) {
                    totalSlots += ((Long) slotObj).intValue();
                } else if (slotObj instanceof Integer) {
                    totalSlots += (Integer) slotObj;
                }
            }
        }
        holder.tvTeamProgressTotal.setText("/ " + totalSlots);
        holder.tvTeamProgressCurrent.setText(String.valueOf(p.getAcceptedCount()));

        String status = p.getStatus() != null ? p.getStatus() : "BUKA";
        holder.tvStatusBadge.setText(status);
        if ("TUTUP".equals(status)) {
            holder.tvStatusBadge.setBackgroundColor(0xFF1D1B20); // Greyish for closed
            holder.tvStatusBadge.setTextColor(0xFFFFFFFF);
        } else {
            holder.tvStatusBadge.setBackgroundColor(0xFF1D1B20); // Black for open
            holder.tvStatusBadge.setTextColor(0xFFFFFFFF);
        }

        holder.btnKelola.setOnClickListener(v -> listener.onManage(p));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(p));
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvApplicants, tvTeamProgressCurrent, tvTeamProgressTotal, tvStatusBadge;
        View btnKelola, btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_project_title);
            tvApplicants = itemView.findViewById(R.id.tv_applicants_count);
            tvTeamProgressCurrent = itemView.findViewById(R.id.tv_team_progress_current);
            tvTeamProgressTotal = itemView.findViewById(R.id.tv_team_progress_total);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            btnKelola = itemView.findViewById(R.id.btn_kelola);
            btnEdit = itemView.findViewById(R.id.btn_edit);
        }
    }
}
