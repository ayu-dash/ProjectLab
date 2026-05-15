package id.project.lab.model;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.Map;

public class Project {
    private String id;
    private String ownerId;
    private String category;
    private String title;
    private String description;
    private String type;
    private List<Map<String, Object>> roles;
    private Timestamp timestamp;
    private String banner_base64;
    private int applicantsCount;
    private int acceptedCount;
    private String status; // "BUKA" or "TUTUP"

    // Required empty constructor for Firestore
    public Project() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<Map<String, Object>> getRoles() { return roles; }
    public void setRoles(List<Map<String, Object>> roles) { this.roles = roles; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getBanner_base64() { return banner_base64; }
    public void setBanner_base64(String banner_base64) { this.banner_base64 = banner_base64; }

    public int getApplicantsCount() { return applicantsCount; }
    public void setApplicantsCount(int applicantsCount) { this.applicantsCount = applicantsCount; }

    public int getAcceptedCount() { return acceptedCount; }
    public void setAcceptedCount(int acceptedCount) { this.acceptedCount = acceptedCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
