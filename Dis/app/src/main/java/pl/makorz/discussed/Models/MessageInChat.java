package pl.makorz.discussed.Models;

import java.util.Date;

public class MessageInChat {

    public String textOfMessage, userID, userNameID, messageID;
    Date dateOfMessage;
    public boolean wasGraded;

    public MessageInChat() {
    }

    public MessageInChat(String textOfMessage, String userID, String userNameID, Date dateOfMessage, boolean wasGraded) {
        this.textOfMessage = textOfMessage;
        this.userID = userID;
        this.userNameID = userNameID;
        this.dateOfMessage = dateOfMessage;
        this.wasGraded = wasGraded;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public boolean wasGraded() {
        return wasGraded;
    }

    public void setWasGraded(boolean wasGraded) {
        this.wasGraded = wasGraded;
    }

    public String getTextOfMessage() {
        return textOfMessage;
    }

    public void setTextOfMessage(String textOfMessage) {
        this.textOfMessage = textOfMessage;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserNameID() {
        return userNameID;
    }

    public void setUserNameID(String userNameID) {
        this.userNameID = userNameID;
    }

    public Date getDateOfMessage() {
        return dateOfMessage;
    }

    public void setDateOfMessage(Date dateOfMessage) {
        this.dateOfMessage = dateOfMessage;
    }
}
