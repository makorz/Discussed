package pl.makorz.discussed.Models;

public class User {

    private String displayName, description, ageOfUser, location;
    private String photo1URL, photo2URL, photo3URL;
    private boolean premium, isActive, blindDateParticipationWill, filledNecessaryInfo;

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
