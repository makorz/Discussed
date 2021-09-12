package pl.makorz.discussed.Models;

import java.util.Date;

public class MessageInBlindDate {

    private String textOfMessage, userID, userName, messageID;
    private Date dateOfMessage;
    private int typeOfMessage, whatRoundSend;

    public MessageInBlindDate() {
    }

    public MessageInBlindDate(String textOfMessage, String userID, String userName, Date dateOfMessage, int whatRoundSend) {
        this.textOfMessage = textOfMessage;
        this.userID = userID;
        this.userName = userName;
        this.dateOfMessage = dateOfMessage;
        this.whatRoundSend = whatRoundSend;
    }

    public int getWhatRoundSend() {
        return whatRoundSend;
    }

    public void setWhatRoundSend(int whatRoundSend) {
        this.whatRoundSend = whatRoundSend;
    }

    public int getTypeOfMessage() {
        return typeOfMessage;
    }

    public void setTypeOfMessage(int typeOfMessage) {
        this.typeOfMessage = typeOfMessage;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getDateOfMessage() {
        return dateOfMessage;
    }

    public void setDateOfMessage(Date dateOfMessage) {
        this.dateOfMessage = dateOfMessage;
    }

}
