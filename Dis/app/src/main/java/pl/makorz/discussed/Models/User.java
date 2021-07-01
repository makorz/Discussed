package pl.makorz.discussed.Models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class User {

    private String displayName, description, firstPhotoUri, secondPhotoUri, thirdPhotoUri;
    private boolean premium, isActive, blindDateParticipationWill, filledNecessaryInfo, firstPhotoUploadMade, secondPhotoUploadMade, thirdPhotoUploadMade,
            topicsUploadMade, searchID, locationUploadMade, genderFemale, descriptionUploadMade;
    private int age, ageOfUser;
    private GeoPoint location;

    public User() {
    }

    public User(String displayName, String description, String firstPhotoUri, String secondPhotoUri, String thirdPhotoUri, boolean premium,
                boolean isActive, boolean blindDateParticipationWill, boolean filledNecessaryInfo, boolean firstPhotoUploadMade, boolean secondPhotoUploadMade,
                boolean thirdPhotoUploadMade, boolean topicsUploadMade, boolean searchID, boolean locationUploadMade, boolean genderFemale,
                boolean descriptionUploadMade, int age, int ageOfUser, GeoPoint location) {
        this.displayName = displayName;
        this.description = description;
        this.firstPhotoUri = firstPhotoUri;
        this.secondPhotoUri = secondPhotoUri;
        this.thirdPhotoUri = thirdPhotoUri;
        this.premium = premium;
        this.isActive = isActive;
        this.blindDateParticipationWill = blindDateParticipationWill;
        this.filledNecessaryInfo = filledNecessaryInfo;
        this.firstPhotoUploadMade = firstPhotoUploadMade;
        this.secondPhotoUploadMade = secondPhotoUploadMade;
        this.thirdPhotoUploadMade = thirdPhotoUploadMade;
        this.topicsUploadMade = topicsUploadMade;
        this.searchID = searchID;
        this.locationUploadMade = locationUploadMade;
        this.genderFemale = genderFemale;
        this.descriptionUploadMade = descriptionUploadMade;
        this.age = age;
        this.ageOfUser = ageOfUser;
        this.location = location;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFirstPhotoUri() {
        return firstPhotoUri;
    }

    public Bitmap getFirstPhotoBitmap() {
        try {
            URL url = new URL(getFirstPhotoUri());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }


    public void setFirstPhotoUri(String firstPhotoUri) {
        this.firstPhotoUri = firstPhotoUri;
    }

    public String getSecondPhotoUri() {
        return secondPhotoUri;
    }

    public void setSecondPhotoUri(String secondPhotoUri) {
        this.secondPhotoUri = secondPhotoUri;
    }

    public String getThirdPhotoUri() {
        return thirdPhotoUri;
    }

    public void setThirdPhotoUri(String thirdPhotoUri) {
        this.thirdPhotoUri = thirdPhotoUri;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isBlindDateParticipationWill() {
        return blindDateParticipationWill;
    }

    public void setBlindDateParticipationWill(boolean blindDateParticipationWill) {
        this.blindDateParticipationWill = blindDateParticipationWill;
    }

    public boolean isFilledNecessaryInfo() {
        return filledNecessaryInfo;
    }

    public void setFilledNecessaryInfo(boolean filledNecessaryInfo) {
        this.filledNecessaryInfo = filledNecessaryInfo;
    }

    public boolean isFirstPhotoUploadMade() {
        return firstPhotoUploadMade;
    }

    public void setFirstPhotoUploadMade(boolean firstPhotoUploadMade) {
        this.firstPhotoUploadMade = firstPhotoUploadMade;
    }

    public boolean isSecondPhotoUploadMade() {
        return secondPhotoUploadMade;
    }

    public void setSecondPhotoUploadMade(boolean secondPhotoUploadMade) {
        this.secondPhotoUploadMade = secondPhotoUploadMade;
    }

    public boolean isThirdPhotoUploadMade() {
        return thirdPhotoUploadMade;
    }

    public void setThirdPhotoUploadMade(boolean thirdPhotoUploadMade) {
        this.thirdPhotoUploadMade = thirdPhotoUploadMade;
    }

    public boolean isTopicsUploadMade() {
        return topicsUploadMade;
    }

    public void setTopicsUploadMade(boolean topicsUploadMade) {
        this.topicsUploadMade = topicsUploadMade;
    }

    public boolean isSearchID() {
        return searchID;
    }

    public void setSearchID(boolean searchID) {
        this.searchID = searchID;
    }

    public boolean isLocationUploadMade() {
        return locationUploadMade;
    }

    public void setLocationUploadMade(boolean locationUploadMade) {
        this.locationUploadMade = locationUploadMade;
    }

    public boolean isGenderFemale() {
        return genderFemale;
    }

    public void setGenderFemale(boolean genderFemale) {
        this.genderFemale = genderFemale;
    }

    public boolean isDescriptionUploadMade() {
        return descriptionUploadMade;
    }

    public void setDescriptionUploadMade(boolean descriptionUploadMade) {
        this.descriptionUploadMade = descriptionUploadMade;
    }

    public int getAge() {
        return age;
    }

    public String getAgeString() {
        return String.valueOf(age);
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getAgeOfUser() {
        return ageOfUser;
    }

    public void setAgeOfUser(int ageOfUser) {
        this.ageOfUser = ageOfUser;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public String getLocationString() {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        return "[ " + latitude + ", " + longitude + " ]";
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }
}
