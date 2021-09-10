package pl.makorz.discussed.Models;

import java.util.Date;
import java.util.List;

public class BlindDate {

    private String userNameNr0, userNameNr1, userNameNr2, userNameNr3, blindDateID, userIDNr0, userIDNr1, userIDNr2, userIDNr3;
    private List<String> usersParticipatingThatHaveNotDeletedBlindDates;
    private boolean wasUserInActivityNr0, wasUserInActivityNr1, wasUserInActivityNr2, wasUserInActivityNr3, wasUserRejectedNr1, wasUserRejectedNr2,
            wasUserRejectedNr3;
    private Date lastMessageDate, dateOfBlindDateCreation;
    private int numberOfRoundInBlindDate;

    public BlindDate() {
    }

    public String getUserNameNr0() {
        return userNameNr0;
    }

    public void setUserNameNr0(String userNameNr0) {
        this.userNameNr0 = userNameNr0;
    }

    public String getUserNameNr1() {
        return userNameNr1;
    }

    public void setUserNameNr1(String userNameNr1) {
        this.userNameNr1 = userNameNr1;
    }

    public String getUserNameNr2() {
        return userNameNr2;
    }

    public void setUserNameNr2(String userNameNr2) {
        this.userNameNr2 = userNameNr2;
    }

    public String getUserNameNr3() {
        return userNameNr3;
    }

    public void setUserNameNr3(String userNameNr3) {
        this.userNameNr3 = userNameNr3;
    }

    public String getBlindDateID() {
        return blindDateID;
    }

    public void setBlindDateID(String blindDateID) {
        this.blindDateID = blindDateID;
    }

    public List<String> getUsersParticipatingThatHaveNotDeletedBlindDates() {
        return usersParticipatingThatHaveNotDeletedBlindDates;
    }

    public void setUsersParticipatingThatHaveNotDeletedBlindDates(List<String> usersParticipatingThatHaveNotDeletedBlindDates) {
        this.usersParticipatingThatHaveNotDeletedBlindDates = usersParticipatingThatHaveNotDeletedBlindDates;
    }

    public boolean getWasUserInActivityNr0() {
        return wasUserInActivityNr0;
    }

    public void setWasUserInActivityNr0(boolean wasUserInActivityNr0) {
        this.wasUserInActivityNr0 = wasUserInActivityNr0;
    }

    public boolean getWasUserInActivityNr1() {
        return wasUserInActivityNr1;
    }

    public void setWasUserInActivityNr1(boolean wasUserInActivityNr1) {
        this.wasUserInActivityNr1 = wasUserInActivityNr1;
    }

    public boolean getWasUserInActivityNr2() {
        return wasUserInActivityNr2;
    }

    public void setWasUserInActivityNr2(boolean wasUserInActivityNr2) {
        this.wasUserInActivityNr2 = wasUserInActivityNr2;
    }

    public boolean getWasUserInActivityNr3() {
        return wasUserInActivityNr3;
    }

    public void setWasUserInActivityNr3(boolean wasUserInActivityNr3) {
        this.wasUserInActivityNr3 = wasUserInActivityNr3;
    }

    public Date getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(Date lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public String getUserIDNr0() {
        return userIDNr0;
    }

    public void setUserIDNr0(String userIDNr0) {
        this.userIDNr0 = userIDNr0;
    }

    public String getUserIDNr1() {
        return userIDNr1;
    }

    public void setUserIDNr1(String userIDNr1) {
        this.userIDNr1 = userIDNr1;
    }

    public String getUserIDNr2() {
        return userIDNr2;
    }

    public void setUserIDNr2(String userIDNr2) {
        this.userIDNr2 = userIDNr2;
    }

    public String getUserIDNr3() {
        return userIDNr3;
    }

    public void setUserIDNr3(String userIDNr3) {
        this.userIDNr3 = userIDNr3;
    }

    public boolean getWasUserRejectedNr1() {
        return wasUserRejectedNr1;
    }

    public void setWasUserRejectedNr1(boolean wasUserRejectedNr1) {
        this.wasUserRejectedNr1 = wasUserRejectedNr1;
    }

    public boolean getWasUserRejectedNr2() {
        return wasUserRejectedNr2;
    }

    public void setWasUserRejectedNr2(boolean wasUserRejectedNr2) {
        this.wasUserRejectedNr2 = wasUserRejectedNr2;
    }

    public boolean getWasUserRejectedNr3() {
        return wasUserRejectedNr3;
    }

    public void setWasUserRejectedNr3(boolean wasUserRejectedNr3) {
        this.wasUserRejectedNr3 = wasUserRejectedNr3;
    }

    public Date getDateOfBlindDateCreation() {
        return dateOfBlindDateCreation;
    }

    public void setDateOfBlindDateCreation(Date dateOfBlindDateCreation) {
        this.dateOfBlindDateCreation = dateOfBlindDateCreation;
    }

    public int getNumberOfRoundInBlindDate() {
        return numberOfRoundInBlindDate;
    }

    public void setNumberOfRoundInBlindDate(int numberOfRoundInBlindDate) {
        this.numberOfRoundInBlindDate = numberOfRoundInBlindDate;
    }
}