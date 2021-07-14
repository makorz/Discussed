package pl.makorz.discussed.Controllers.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import pl.makorz.discussed.Controllers.ChatActivity;
import pl.makorz.discussed.R;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragmentActivity";

    private static final String CAN_USER_SEARCH = "canUserSearch";
    private static final String FIRST_PHOTO_UPLOAD_MADE = "firstPhotoUploadMade";
    private static final String SECOND_PHOTO_UPLOAD_MADE = "secondPhotoUploadMade";
    private static final String THIRD_PHOTO_UPLOAD_MADE = "thirdPhotoUploadMade";
    private static final String LOCATION_UPLOAD_MADE = "locationUploadMade";
    private static final String TOPICS_UPLOAD_MADE = "topicsUploadMade";
    private static final String AGE_UPLOAD_MADE = "ageUploadMade";
    private static final String DESCRIPTION_UPLOAD_MADE = "descriptionUploadMade";
    private static final String GENDER_UPLOAD_MADE = "genderUploadMade";
    private static final String NAME_UPLOAD_MADE = "nameUploadMade";

    private String chatID, nameOfUser, nameOfOtherUser, firstPhotoUriOfUser, firstPhotoUriOfOtherUser, idOfOtherUser;
    private Button buttonChat, buttonBlindDate;
    private ProgressBar waitUntilChatAppears;
    private LinearLayout layoutToDimWhenSearching;
    private Boolean firstPhotoUploadMade, secondPhotoUploadMade, thirdPhotoUploadMade, locationUploadMade, descriptionUploadMade, ageUploadMade, topicsUploadMade,
            genderUploadMade, nameUploadMade,canUserSearch;
    private final Map<String, Boolean> userSearchAvailability = new HashMap<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DocumentSnapshot userSnapshot, otherUserSnapshot;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View mainView = inflater.inflate(R.layout.fragment_main, container, false);
        waitUntilChatAppears = mainView.findViewById(R.id.progressBarOfSearch);
        layoutToDimWhenSearching = mainView.findViewById(R.id.linearLayoutOfSearch);
        buttonChat = (Button) mainView.findViewById(R.id.button_search_chatMate);
        buttonBlindDate = (Button) mainView.findViewById(R.id.button_start_blindDate);

        buttonChat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                buttonChat.setEnabled(false);
                buttonBlindDate.setEnabled(false);
                layoutToDimWhenSearching.setAlpha(0.3f);
                waitUntilChatAppears.setVisibility(View.VISIBLE);
                try {
                    checkIfUserCanSearch();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });


        return mainView;
    }

    // Searching user with some parameters
    private void searchForUser() {

        Random r = new Random();
        int randomNrOfUser = r.nextInt(4 - 1) + 1;

        Query queryUser = FirebaseFirestore.getInstance().collection("search/searchAll/searchNE").whereEqualTo("randomNr",randomNrOfUser).limit(1);
        queryUser.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                idOfOtherUser = document.get("idOfUser").toString();
                                if (idOfOtherUser.equals(user.getUid())){
                                    searchForUser();
                                } else {
                                    getUsersNames(idOfOtherUser);
                                }

                            }
                        }
                    }
                });

    }

    // Create Chat Activity
    private void generateChatInFirestore() {
        Map<String, Object> chat = new HashMap<>();
        Map<String, Object> user1 = new HashMap<>();
        Map<String, Object> user2 = new HashMap<>();

        db.collection("chats").add(chat)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        chatID = documentReference.getId();
                        chat.put("chatID", chatID);
                        chat.put("dateOfChatCreation", new Date());
                        chat.put("usersParticipatingName", Arrays.asList(nameOfUser,nameOfOtherUser));
                        chat.put("lastMessage","");
                        chat.put("lastMessageDate",new Date());
                        chat.put("usersParticipatingFirstImageUri",Arrays.asList(firstPhotoUriOfUser,firstPhotoUriOfOtherUser));
                        chat.put("usersParticipatingID", Arrays.asList(user.getUid(),idOfOtherUser));
                        chat.put("isFirstPhotoOfUserUncovered",Arrays.asList(false,false));

                        user1.put("userID", user.getUid());
                        user1.put("pointsFromOtherUser", (Integer) 0);
                        user1.put("userName", nameOfUser);
                        user1.put("uncoverStrangerFirstPhoto",false);
                        user1.put("uncoverStrangerSecondPhoto",false);
                        user1.put("uncoverStrangerThirdPhoto",false);
                        user1.put("uncoverStrangerDescription",false);
                        user1.put("uncoverStrangerAge",false);
                        user1.put("uncoverStrangerLocation",false);
                        user1.put("uncoverStrangerGender",false);
                        db.collection("chats").document(chatID).collection("chatUsers").document(user.getUid()).set(user1);

                        user2.put("userID", idOfOtherUser);
                        user2.put("pointsFromOtherUser", (Integer) 0);
                        user2.put("userName", nameOfOtherUser);
                        user2.put("uncoverStrangerFirstPhoto",false);
                        user2.put("uncoverStrangerSecondPhoto",false);
                        user2.put("uncoverStrangerThirdPhoto",false);
                        user2.put("uncoverStrangerDescription",false);
                        user2.put("uncoverStrangerAge",false);
                        user2.put("uncoverStrangerLocation",false);
                        user2.put("uncoverStrangerGender",false);
                        db.collection("chats").document(chatID).collection("chatUsers").document(idOfOtherUser).set(user2);

                        db.collection("chats").document(chatID).set(chat)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                                        intent.putExtra("chatIdIntent", chatID);
                                        intent.putExtra("otherUserName", nameOfOtherUser);
                                        intent.putExtra("idOfOtherUser",idOfOtherUser);
                                        waitUntilChatAppears.setVisibility(View.INVISIBLE);
                                        layoutToDimWhenSearching.setAlpha(1f);
                                        requireActivity().startActivity(intent);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });

    }

    // Get names of user that was searched
    private void getUsersNames(String id) {

        int numCores = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(numCores * 2, numCores * 2,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

        String str = id.replaceAll("\\s", "");

        DocumentReference docRef2 = db.collection("users").document(str);
        Task<DocumentSnapshot> taskOtherUser = docRef2.get();

        DocumentReference docRef = db.collection("users").document(user.getUid());
        Task<DocumentSnapshot> taskUser = docRef.get();

        taskUser.addOnCompleteListener(executor, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                userSnapshot = task.getResult();
                nameOfUser = userSnapshot.getString("displayName");
                firstPhotoUriOfUser = userSnapshot.getString("firstPhotoUri");
                taskOtherUser.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                        otherUserSnapshot = task2.getResult();
                        nameOfOtherUser = otherUserSnapshot.getString("displayName");
                        firstPhotoUriOfOtherUser = otherUserSnapshot.getString("firstPhotoUri");
                        Log.d(TAG, "onCompleteTASKSKAKSAKDK: " + nameOfOtherUser);
                        generateChatInFirestore();
                    }
                });
            }
        });
    }

    public void profileNotFiledAlertDialog() {

        layoutToDimWhenSearching.setAlpha(1f);
        waitUntilChatAppears.setVisibility(View.INVISIBLE);
        LayoutInflater inflaterDialog = LayoutInflater.from(getContext());
        View alertNotFiledProfileDialogView = inflaterDialog.inflate(R.layout.dialog_search_not_available, null);
        TextView notFiledProfileAlertTitle = alertNotFiledProfileDialogView.findViewById(R.id.title_of_search_availability_box);
        int i = 0;
        StringBuilder whatWasNotFilled = new StringBuilder(getString(R.string.title_not_filled_profile_to_search_alert_dialog));
        for (Map.Entry<String,Boolean> entry : userSearchAvailability.entrySet()) {
            String addTopic = "";
            if (!entry.getValue()) {
                i++;
                String nrOfTopic = String.valueOf(i);
                addTopic = nrOfTopic + ". " + entry.getKey() + "\n";
            }
            whatWasNotFilled.append(addTopic);
        }
        notFiledProfileAlertTitle.setText(whatWasNotFilled);
        userSearchAvailability.clear();

        AlertDialog alertDateDialog = new AlertDialog.Builder(getContext())
                .setView(alertNotFiledProfileDialogView)  // What to use in dialog box
                .setPositiveButton("OK", null)
                .show();

        alertDateDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonChat.setEnabled(true);
                buttonBlindDate.setEnabled(true);
                alertDateDialog.dismiss();
            }
        });

    }

    private void checkIfUserCanSearch() throws ExecutionException, InterruptedException {
        // Download document of current user, to retrieve actual info to profile view
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        canUserSearch = document.getBoolean(CAN_USER_SEARCH);

                        if (canUserSearch) {
                            searchForUser();
                        } else {
                            firstPhotoUploadMade = document.getBoolean(FIRST_PHOTO_UPLOAD_MADE);
                            secondPhotoUploadMade = document.getBoolean(SECOND_PHOTO_UPLOAD_MADE);
                            thirdPhotoUploadMade = document.getBoolean(THIRD_PHOTO_UPLOAD_MADE);
                            locationUploadMade = document.getBoolean(LOCATION_UPLOAD_MADE);
                            nameUploadMade = document.getBoolean(NAME_UPLOAD_MADE);
                            ageUploadMade = document.getBoolean(AGE_UPLOAD_MADE);
                            topicsUploadMade = document.getBoolean(TOPICS_UPLOAD_MADE);
                            descriptionUploadMade = document.getBoolean(DESCRIPTION_UPLOAD_MADE);
                            genderUploadMade = document.getBoolean(GENDER_UPLOAD_MADE);

                            userSearchAvailability.put("First Photo",firstPhotoUploadMade);
                            userSearchAvailability.put("Second Photo",secondPhotoUploadMade);
                            userSearchAvailability.put("Third Photo",thirdPhotoUploadMade);
                            userSearchAvailability.put("Location",locationUploadMade);
                            userSearchAvailability.put("Name",nameUploadMade);
                            userSearchAvailability.put("Age",ageUploadMade);
                            userSearchAvailability.put("Topics",topicsUploadMade);
                            userSearchAvailability.put("Description",descriptionUploadMade);
                            userSearchAvailability.put("Gender",genderUploadMade);

                            profileNotFiledAlertDialog();
                        }

                    } else {
                        Log.d("LOGGER", "No such document");
                    }

                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });

    }



}



