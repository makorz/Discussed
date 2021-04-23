package pl.makorz.discussed.Models;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Conversation {
    private String lastMessage;
    private String chatID;
    private List<String> usersParticipatingName, usersParticipatingID;
    private Date dateOfChatCreation;
    private final String TAG = "Conversation";

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    private int imageResourceId;

    public List<String> getUsersParticipatingName() {

        return usersParticipatingName;
    }

    public List<String> getUsersParticipatingID() {
        return usersParticipatingID;
    }


    public Conversation() {
    }

    public void getTextOfMessage(String chatID) {
        checkLastMessage(chatID);
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

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    public void checkLastMessage(final String chatID) {
        Query queryMessages = FirebaseFirestore.getInstance().collection("chats").document(chatID)
                .collection("messages").orderBy("dateOfMessage", Query.Direction.DESCENDING).limit(1);
        queryMessages.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> chat = new HashMap<>();
                                String textShort = document.get("textOfMessage").toString();
                                if (textShort.length() > 40) {
                                    textShort = textShort.substring(0,35) + "...";
                                }
                                chat.put("lastMessage", textShort);
                                FirebaseFirestore.getInstance().collection("chats").document(chatID).set(chat, SetOptions.merge());
                            }

                        }
                    }
                });


    }

}



