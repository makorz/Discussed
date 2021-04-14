package pl.makorz.discussed.Models;

import java.util.Date;

public class MessageInChat {

    public String textOfMessage, userID, userNameID;
    Date dateOfMessage;

    public MessageInChat() {

    }

    public MessageInChat(String textOfMessage, String userID, String userNameID, Date dateOfMessage) {
        this.textOfMessage = textOfMessage;
        this.userID = userID;
        this.userNameID = userNameID;
        this.dateOfMessage = dateOfMessage;
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
