package id.project.lab.ui.myproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import id.project.lab.R;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.ViewHolder> {

    public static class TeamSlot {
        public String roleName;
        public String memberName;
        public String profileUrl;
        public boolean isFilled;
        
        public TeamSlot(String roleName, String memberName, String profileUrl, boolean isFilled) {
            this.roleName = roleName;
            this.memberName = memberName;
            this.profileUrl = profileUrl;
            this.isFilled = isFilled;
        }
    }

    private List<TeamSlot> slots;

    public TeamAdapter(List<TeamSlot> slots) {
        this.slots = slots;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeamSlot slot = slots.get(position);
        if (slot.isFilled) {
            holder.containerFilled.setVisibility(View.VISIBLE);
            holder.containerEmpty.setVisibility(View.GONE);
            holder.tvMemberName.setText(slot.memberName);
            holder.tvMemberRole.setText(slot.roleName);
            
            if (slot.profileUrl != null && !slot.profileUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(slot.profileUrl)
                        .placeholder(R.color.purple_light)
                        .error(R.color.purple_light)
                        .into(holder.ivMemberAvatar);
            } else {
                holder.ivMemberAvatar.setImageResource(0);
                holder.ivMemberAvatar.setBackgroundResource(R.color.purple_light);
            }
        } else {
            holder.containerFilled.setVisibility(View.GONE);
            holder.containerEmpty.setVisibility(View.VISIBLE);
            holder.tvEmptyRole.setText(slot.roleName);
        }
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View containerFilled, containerEmpty;
        TextView tvMemberName, tvMemberRole, tvEmptyRole;
        ImageView ivMemberAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            containerFilled = itemView.findViewById(R.id.container_filled);
            containerEmpty = itemView.findViewById(R.id.container_empty);
            tvMemberName = itemView.findViewById(R.id.tv_member_name);
            tvMemberRole = itemView.findViewById(R.id.tv_member_role);
            tvEmptyRole = itemView.findViewById(R.id.tv_empty_role);
            ivMemberAvatar = itemView.findViewById(R.id.iv_member_avatar);
        }
    }
}
