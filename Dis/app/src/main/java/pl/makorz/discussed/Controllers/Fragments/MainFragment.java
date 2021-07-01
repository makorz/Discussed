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
    public static final String CAN_USER_SEARCH = "canUserSearch";
    public static final String FIRST_PHOTO_UPLOAD_MADE = "firstPhotoUploadMade";
    public static final String SECOND_PHOTO_UPLOAD_MADE = "secondPhotoUploadMade";
    public static final String THIRD_PHOTO_UPLOAD_MADE = "thirdPhotoUploadMade";
    public static final String LOCATION_UPLOAD_MADE = "locationUploadMade";
    public static final String TOPICS_UPLOAD_MADE = "topicsUploadMade";
    public static final String AGE_UPLOAD_MADE = "ageUploadMade";
    public static final String DESCRIPTION_UPLOAD_MADE = "descriptionUploadMade";
    public static final String GENDER_UPLOAD_MADE = "genderUploadMade";
    public static final String NAME_UPLOAD_MADE = "nameUploadMade";
    private static String chatID;
    private String nameOfUser, nameOfOtherUser, firstPhotoUriOfUser, firstPhotoUriOfOtherUser, idOfOtherUser;
    private DocumentSnapshot userSnapshot, otherUserSnapshot;
    Button buttonChat, buttonBlindDate;
    ProgressBar waitUntilChatAppears;
    LinearLayout layoutToDimWhenSearching;
    private Boolean firstPhotoUploadMade, secondPhotoUploadMade, thirdPhotoUploadMade, locationUploadMade, descriptionUploadMade, ageUploadMade, topicsUploadMade,
            genderUploadMade, nameUploadMade, whatGender, canUserSearch;
    Map<String, Boolean> userSearchAvailability = new HashMap<>();


    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mainView = inflater.inflate(R.layout.fragment_main, container, false);
        waitUntilChatAppears = mainView.findViewById(R.id.progressBarOfSearch);
        layoutToDimWhenSearching = mainView.findViewById(R.id.linearLayoutOfSearch);
        buttonChat = (Button) mainView.findViewById(R.id.button_search_chatmate);
        buttonBlindDate = (Button) mainView.findViewById(R.id.button_start_blinddate);

        buttonChat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                buttonChat.setEnabled(false);
                buttonBlindDate.setEnabled(false);
                layoutToDimWhenSearching.setAlpha(0.4f);
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
        int randomNrOfUser = r.nextInt(7 - 1) + 1;


        Query queryUser = FirebaseFirestore.getInstance().collection("search/searchAll/searchNE").whereEqualTo ("randomNr",randomNrOfUser);
        queryUser.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                idOfOtherUser = document.get("idOfUser").toString();
                                Log.d(TAG, "onCompleteasfasf: " + idOfOtherUser);
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

        db.collection("chats").add(chat)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        chatID = documentReference.getId();
                        chat.put("chatID", chatID);
                        chat.put("dateOfChatCreation", new Date());
                        Log.d(TAG, "onCdgsdgsdg: " + nameOfOtherUser);
                        chat.put("usersParticipatingName", Arrays.asList(nameOfUser,nameOfOtherUser));
                        chat.put("lastMessage","");
                        chat.put("usersParticipatingFirstImageUri",Arrays.asList(firstPhotoUriOfUser,firstPhotoUriOfOtherUser));
                        chat.put("usersParticipatingID", Arrays.asList(user.getUid(),idOfOtherUser));
                        chat.put("usersNrOfPoints",Arrays.asList(0,0));
                        chat.put("isFirstPhotoOfUserUncovered",Arrays.asList(false,false));

                        db.collection("chats").document(chatID).set(chat)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully written! " + chatID);
                                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                                        intent.putExtra("chatIdIntent", chatID);
                                        intent.putExtra("otherUserName", nameOfOtherUser);
                                        intent.putExtra("idOfOtherUser",idOfOtherUser);
                                        Objects.requireNonNull(getActivity()).startActivity(intent);
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
        StringBuilder whatWasNotFilled = new StringBuilder("You have not fully filled the profile card. You still need to add: \n\n");
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



