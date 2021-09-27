package pl.makorz.discussed.Controllers.Notifications;

public class DataB {

    private String userTargetedID;
    private int icon, roundNo, typeData;
    private String body;
    private String title;
    private String whoSendID;
    private String whoSendName;
    private String blindDateID;
//    private String userNameNr0, userNameNr1, userNameNr2, userNameNr3;
//    private String userIDNr0, userIDNr1, userIDNr2, userIDNr3;
//    private boolean wasUserNr1Rejected, wasUserNr2Rejected, wasUserNr3Rejected;
//    private Date blindDateCreationDate, dateOfStartRound2;

//    public DataB(String userTargetedID, int icon, int roundNo, int typeData, String body, String title, String whoSendID, String whoSendName,
//                 String blindID, String userNameNr0, String userNameNr1, String userNameNr2, String userNameNr3, String userIDNr0, String userIDNr1,
//                 String userIDNr2, String userIDNr3, boolean wasUserNr1Rejected, boolean wasUserNr2Rejected, boolean wasUserNr3Rejected, Date blindDateCreationDate,
//                 Date dateOfStartRound2) {
//
//        this.userTargetedID = userTargetedID;
//        this.icon = icon;
//        this.roundNo = roundNo;
//        this.typeData = typeData;
//        this.body = body;
//        this.title = title;
//        this.whoSendID = whoSendID;
//        this.whoSendName = whoSendName;
//        this.blindID = blindID;
//        this.userNameNr0 = userNameNr0;
//        this.userNameNr1 = userNameNr1;
//        this.userNameNr2 = userNameNr2;
//        this.userNameNr3 = userNameNr3;
//        this.userIDNr0 = userIDNr0;
//        this.userIDNr1 = userIDNr1;
//        this.userIDNr2 = userIDNr2;
//        this.userIDNr3 = userIDNr3;
//        this.wasUserNr1Rejected = wasUserNr1Rejected;
//        this.wasUserNr2Rejected = wasUserNr2Rejected;
//        this.wasUserNr3Rejected = wasUserNr3Rejected;
//        this.blindDateCreationDate = blindDateCreationDate;
//        this.dateOfStartRound2 = dateOfStartRound2;
//    }


        public DataB(String userTargetedID, int icon, String body, String title, String whoSendID, String whoSendName, String blindDateID,
                 int typeData) {

        this.userTargetedID = userTargetedID;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.whoSendID = whoSendID;
        this.whoSendName = whoSendName;
        this.blindDateID = blindDateID;
        this.typeData = typeData;

    }
}
