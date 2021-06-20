package pl.makorz.discussed.Controllers.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import pl.makorz.discussed.Controllers.ChatActivity;
import pl.makorz.discussed.Models.Topic;
import pl.makorz.discussed.Models.TopicViewHolder;
import pl.makorz.discussed.R;


public class MainFragment extends Fragment {

    private static final String TAG = "MainFragmentActivity";
    private static String chatID;
    private String nameOfUser;
    private String nameOfOtherUser;
    private String idOfSearchedUser;
    private View mainView;
    private RecyclerView topicsRecycler;
    private DocumentSnapshot userSnapshot;
    private DocumentSnapshot otherUserSnapshot;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.fragment_main,container,false);

//        topicsRecycler = mainView.findViewById(R.id.topics_recycler);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
//        topicsRecycler.setLayoutManager(layoutManager);

        ProgressBar waitUntilChatAppears = mainView.findViewById(R.id.progressBarOfSearch);
        RelativeLayout layoutToDimWhenSearching = mainView.findViewById(R.id.relativeLayoutOfSearch);
        Button buttonChat = (Button) mainView.findViewById(R.id.button_search_chatmate);
        Button buttonBlindDate = (Button) mainView.findViewById(R.id.button_start_blinddate);

        buttonChat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                buttonChat.setEnabled(false);
                buttonBlindDate.setEnabled(false);
                layoutToDimWhenSearching.setAlpha(0.4f);
                waitUntilChatAppears.setVisibility(View.VISIBLE);
                searchForUser();
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
                                idOfSearchedUser = document.get("idOfUser").toString();
                                Log.d(TAG, "onCompleteasfasf: " + idOfSearchedUser);
                                if (idOfSearchedUser.equals(user.getUid())){
                                    searchForUser();
                                } else {
                                    getUsersNames(idOfSearchedUser);
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
                        chat.put("usersParticipatingID", Arrays.asList(user.getUid(),idOfSearchedUser));
                        chat.put("usersNrOfPoints",Arrays.asList(0,0));

                        db.collection("chats").document(chatID).set(chat)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully written! " + chatID);
                                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                                        intent.putExtra("chatIdIntent", chatID);
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

    @Override
    public void onStart() {
        super.onStart();
//        // Initialize Cloud Firestore
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        // Ask the Cloud Firestore
//        Query queryListOfTopics = db.collection("topics").orderBy("position");
//        //Configuring adapter to populate recyclerview
//        FirestoreRecyclerOptions<Topic> options = new FirestoreRecyclerOptions.Builder<Topic>()
//                .setQuery(queryListOfTopics, Topic.class)
//                .setLifecycleOwner(this)
//                .build();
//
//        FirestoreRecyclerAdapter topicAdapter = new FirestoreRecyclerAdapter<Topic, TopicViewHolder>(options) {
//            @Override
//            public void onBindViewHolder(TopicViewHolder holder, int position, Topic model) {
//                // Bind the Ch at object to the ChatHolder
//                TextView topicTitle = holder.itemView.findViewById(android.R.id.text1);
//                topicTitle.setText(model.getTopicTitle());
//            }
//
//            @Override
//            public TopicViewHolder onCreateViewHolder(ViewGroup group, int i) {
//                // Create a new instance of the ViewHolder, in this case we are using a custom
//                // layout called R.layout.message for each item
//                mainView = LayoutInflater.from(group.getContext())
//                        .inflate(android.R.layout.simple_expandable_list_item_1, group, false);
//                return new TopicViewHolder(mainView);
//            }
//        };
//        topicsRecycler.setAdapter(topicAdapter);
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
                taskOtherUser.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                        otherUserSnapshot = task2.getResult();
                        nameOfOtherUser = otherUserSnapshot.getString("displayName");
                        Log.d(TAG, "onCompleteTASKSKAKSAKDK: " + nameOfOtherUser);
                        generateChatInFirestore();
                    }
                });

//                        .addOnCompleteListener(executor, new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
//                        otherUserSnapshot = task2.getResult();
//                        nameOfOtherUser = otherUserSnapshot.getString("displayName");
//                        Log.d(TAG, "onCompleteTASKSKAKSAKDK: " + nameOfOtherUser);
//                        generateChatInFirestore();
//                    }
//                });

            }
        });
    }
}



