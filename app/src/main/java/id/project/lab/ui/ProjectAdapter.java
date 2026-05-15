package id.project.lab.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import id.project.lab.R;
import id.project.lab.model.Project;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projectList;
    private OnProjectClickListener listener;

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    public ProjectAdapter(List<Project> projectList, OnProjectClickListener listener) {
        this.projectList = projectList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.bind(project, listener);
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvType, tvTitle, tvDescription, tvApplicants;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.project_category);
            tvType = itemView.findViewById(R.id.project_type);
            tvTitle = itemView.findViewById(R.id.project_title);
            tvDescription = itemView.findViewById(R.id.project_description);
            tvApplicants = itemView.findViewById(R.id.project_applicants);
        }

        public void bind(Project project, OnProjectClickListener listener) {
            tvCategory.setText(project.getCategory());
            tvType.setText(project.getType());
            tvTitle.setText(project.getTitle());
            tvDescription.setText(project.getDescription());
            tvApplicants.setText(project.getApplicantsCount() + " PELAMAR");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectClick(project);
                }
            });
        }
    }
}
