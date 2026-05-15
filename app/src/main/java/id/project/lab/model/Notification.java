package id.project.lab.model;

public class Notification {
    private String id;
    private String userId; // Recipient
    private String title;
    private String message;
    private String type; // "APPLICATION", "STATUS_CHANGE", etc.
    private long timestamp;
    private boolean read;
    private String referenceId; // e.g. applicationId or projectId

    public Notification() {}

    public Notification(String userId, String title, String message, String type, String referenceId) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
        this.timestamp = System.currentTimeMillis();
        this.read = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
}
