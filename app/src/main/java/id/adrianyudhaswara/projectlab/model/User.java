package id.adrianyudhaswara.projectlab.model;

public class User {
    private String id;
    private String name;
    private String email;
    private String role;
    private String contact;

    public User(String id, String name, String email, String role, String contact) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.contact = contact;
    }

    public boolean isValid() {
        return name != null && !name.trim().isEmpty() && 
               email != null && !email.trim().isEmpty();
    }

    // Getters and Setters (optional but good for future use)
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getContact() { return contact; }
}
