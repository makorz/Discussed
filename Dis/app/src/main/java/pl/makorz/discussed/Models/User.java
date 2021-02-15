package pl.makorz.discussed.Models;

public class User {

    private String displayName;
    private String description;
    public User() {
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
