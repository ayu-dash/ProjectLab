package id.project.lab.model;

import java.util.List;

public class Applicant {
    private String id;
    private String userId;
    private String name;
    private String role;
    private List<String> portfolioLinks;
    private String status; // "PENDING", "ACCEPTED", "REJECTED"
    private String projectId;
    private String projectTitle;
    private String profilePicture;
    private String message;
    private long timestamp;

    public Applicant() {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<String> getPortfolioLinks() { return portfolioLinks; }
    public void setPortfolioLinks(List<String> portfolioLinks) { this.portfolioLinks = portfolioLinks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
