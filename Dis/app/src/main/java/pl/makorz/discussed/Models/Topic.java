package pl.makorz.discussed.Models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Topic {

    private String topicTitle;
    private boolean isFavorite;


    public Topic() {
    }

    public String getTopicTitle() {
        return topicTitle;
    }

    public void setTopicTitle(String topicTitle) {
        this.topicTitle = topicTitle;
    }

    public Boolean getFavorite() {
        return isFavorite;
    }

    public void setFavorite(Boolean favorite) {
        isFavorite = favorite;
    }

//    public void checkIfItIsFavorite(FirebaseFirestore db, String userID, String topic) {
//
//        DocumentReference docRef = db.collection("users").document(userID);
//        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document != null) {
//                        ArrayList<String> topicList = (ArrayList<String>) document.get("chosenTopicsArray");
//                        Log.d("GGGG",topicList.toString());
//                        Log.d("GGGGG",topic);
//                        for(int i = 0; i < topicList.size(); i++) {
//                            if (topic.equals(topicList.get(i))) {
//                                Log.d("GGGGGG",topicList.get(i));
//                                isFavorite = true;
//
//                            } else {
//                                isFavorite = false;
//                            }
//                        }
//                    } else {
//                        Log.d("LOGGER", "No such document");
//                    }
//
//                } else {
//                    Log.d("LOGGER", "get failed with ", task.getException());
//                }
//            }
//        });
//    }


}
