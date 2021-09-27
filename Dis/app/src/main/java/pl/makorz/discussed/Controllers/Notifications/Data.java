package pl.makorz.discussed.Controllers.Notifications;

public class Data {

    private String userTargetedID;
    private int icon;
    private String body;
    private String title;
    private String whoSendID;
    private String whoSendName;
    private String chatID;
    private int typeData;

    public Data(String userTargetedID, int icon, String body, String title, String whoSendID, String whoSendName, String chatID, int typeData) {
        this.userTargetedID = userTargetedID;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.whoSendID = whoSendID;
        this.whoSendName = whoSendName;
        this.chatID = chatID;
        this.typeData = typeData;

    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public String getWhoSendName() {
        return whoSendName;
    }

    public void setWhoSendName(String whoSendName) {
        this.whoSendName = whoSendName;
    }

    public Data() {

    }

    public String getUserTargetedID() {
        return userTargetedID;
    }

    public void setUserTargetedID(String userTargetedID) {
        this.userTargetedID = userTargetedID;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWhoSendID() {
        return whoSendID;
    }

    public void setWhoSendID(String whoSendID) {
        this.whoSendID = whoSendID;
    }
}
