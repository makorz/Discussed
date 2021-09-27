package pl.makorz.discussed.Models;

import java.util.Date;
import java.util.List;

public class Conversation {

    private String lastMessage, chatID;
    private List<String> usersParticipatingName, usersParticipatingID, usersParticipatingFirstImageUri, usersThatHaveNotDeletedConversation;
    private List<Boolean> isFirstPhotoOfUserUncovered, wasUserInActivity;
    private boolean wasUserInActivityNr0, wasUserInActivityNr1;
    private Date dateOfChatCreation, lastMessageDate;

    public Conversation() {
    }

    public List<String> getUsersThatHaveNotDeletedConversation() {
        return usersThatHaveNotDeletedConversation;
    }

    public void setUsersThatHaveNotDeletedConversation(List<String> usersThatHaveNotDeletedConversation) {
        this.usersThatHaveNotDeletedConversation = usersThatHaveNotDeletedConversation;
    }

    public boolean getWasUserInActivityNr0() {
        return wasUserInActivityNr0;
    }

    public void setWasUserInActivityNr0(Boolean wasUserInActivityNr0) {
        this.wasUserInActivityNr0 = wasUserInActivityNr0;
    }

    public boolean getWasUserInActivityNr1() {
        return wasUserInActivityNr1;
    }

    public void setWasUserInActivityNr1(Boolean getWasViewedByUser1) {
        this.wasUserInActivityNr1 = getWasViewedByUser1;
    }

    public List<Boolean> getWasUserInActivity() {
        return wasUserInActivity;
    }

    public void setWasUserInActivity(List<Boolean> wasUserInActivity) {
        this.wasUserInActivity = wasUserInActivity;
    }

    public Date getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(Date lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public List<String> getUsersParticipatingName() {
        return usersParticipatingName;
    }

    public List<String> getUsersParticipatingID() {
        return usersParticipatingID;
    }

    public List<Boolean> getIsFirstPhotoOfUserUncovered() {
        return isFirstPhotoOfUserUncovered;
    }

    public void setIsFirstPhotoOfUserUncovered(List<Boolean> isFirstPhotoOfUserUncovered) {
        this.isFirstPhotoOfUserUncovered = isFirstPhotoOfUserUncovered;
    }

    public List<String> getUsersParticipatingFirstImageUri() {
        return usersParticipatingFirstImageUri;
    }

    public void setUsersParticipatingFirstImageUri(List<String> usersParticipatingFirstImageUri) {
        this.usersParticipatingFirstImageUri = usersParticipatingFirstImageUri;
    }

    public void setUsersParticipatingName(List<String> usersParticipatingName) {
        this.usersParticipatingName = usersParticipatingName;
    }

    public void setUsersParticipatingID(List<String> usersParticipatingID) {
        this.usersParticipatingID = usersParticipatingID;
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public Date getDateOfChatCreation() {
        return dateOfChatCreation;
    }

    public void setDateOfChatCreation(Date dateOfChatCreation) {
        this.dateOfChatCreation = dateOfChatCreation;
    }

}



