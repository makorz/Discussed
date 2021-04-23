package pl.makorz.discussed.Fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import pl.makorz.discussed.ChatActivity;
import pl.makorz.discussed.Functions.SearchUserAsync;
import pl.makorz.discussed.Models.MessageInChat;
import pl.makorz.discussed.Models.Topic;
import pl.makorz.discussed.Models.TopicViewHolder;
import pl.makorz.discussed.R;


public class MainFragment extends Fragment {

    private static final String TAG = "MainFragmentActivity";
    private static String chatID = "";
    private String nameOfUser = "";
    private String nameOfOtherUser = "";
    private String idOfSearchedUser = "";
    private View mainView;
    private RecyclerView topicsRecycler;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.fragment_main,container,false);
        topicsRecycler = mainView.findViewById(R.id.topics_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        topicsRecycler.setLayoutManager(layoutManager);

        Button buttonChat = (Button) mainView.findViewById(R.id.button_search_chatmate);
        buttonChat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                //new SearchUserAsync().execute(user.getUid());
                searchForUser();

                generateChatInFirestore();
            }
        });

        return mainView;
    }

    private void searchForUser() {
        int numberOfUsers = 2;
        int randomIndex = (int) Math.floor(Math.random() * numberOfUsers);
        Query queryUser = FirebaseFirestore.getInstance().collection("search").whereNotEqualTo("idOfUser",user.getUid()).limit(1);
        queryUser.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                 idOfSearchedUser = document.get("idOfUser").toString();
                                Log.d(TAG, "onCompleteasfasf: " + idOfSearchedUser);

                            }

                        }
                    }
                });

    }


    // Start chat activity and get chatID, which is needed for chatActivity
    private void generateChatInFirestore() {


        Map<String, Object> chat = new HashMap<>();

        getUsersNames(idOfSearchedUser);

        db.collection("chats").add(chat)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        chatID = documentReference.getId();
                        chat.put("chatID", chatID);
                        chat.put("dateOfChatCreation", new Date());
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

//                        Map<String, Object> otherUserDataToSend = new HashMap<>();
//                        otherUserDataToSend.put("chatID", chatID);
//                        otherUserDataToSend.put("dateOfChatCreation", new Date());
//                        otherUserDataToSend.put("dateOfLastActivity", new Date());
//                        otherUserDataToSend.put("nameOfOtherUser", nameOfUser );
//
//                        db.collection("users").document("brOA3T81WcXdb6IDJX2negMapTx2").collection("userChats")
//                                .document("chat0").update(otherUserDataToSend);
//
//                        Map<String, Object> searchingUserDataToSend = new HashMap<>();
//                        searchingUserDataToSend.put("chatID", chatID);
//                        searchingUserDataToSend.put("dateOfChatCreation", new Date());
//                        searchingUserDataToSend.put("dateOfLastActivity", new Date());
//                        searchingUserDataToSend.put("nameOfOtherUser", nameOfOtherUser );
//
//                        db.collection("users").document(user.getUid()).collection("userChats").document("chat0")
//                                .update(searchingUserDataToSend);
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
        // Initialize Cloud Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Ask the Cloud Firestore
        Query queryListOfTopics = db.collection("topics").orderBy("position");
        //Configuring adapter to populate recyclerview
        FirestoreRecyclerOptions<Topic> options = new FirestoreRecyclerOptions.Builder<Topic>()
                .setQuery(queryListOfTopics, Topic.class)
                .setLifecycleOwner(this)
                .build();

        FirestoreRecyclerAdapter topicAdapter = new FirestoreRecyclerAdapter<Topic, TopicViewHolder>(options) {
            @Override
            public void onBindViewHolder(TopicViewHolder holder, int position, Topic model) {
                // Bind the Ch at object to the ChatHolder
                TextView topicTitle = holder.itemView.findViewById(android.R.id.text1);
                topicTitle.setText(model.getTopicTitle());
            }

            @Override
            public TopicViewHolder onCreateViewHolder(ViewGroup group, int i) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                mainView = LayoutInflater.from(group.getContext())
                        .inflate(android.R.layout.simple_expandable_list_item_1, group, false);
                return new TopicViewHolder(mainView);
            }
        };
        topicsRecycler.setAdapter(topicAdapter);
    }

    private void getUsersNames(String id) {

        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    nameOfUser = document.getString("displayName");
                    Log.d(TAG, "NameOfUser:" + nameOfUser);
               }
        });

        DocumentReference docRef2 = db.collection("users").document(id);
        docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document2 = task.getResult();
                nameOfOtherUser = document2.getString("displayName");
                Log.d(TAG, "NameOfOtherUser:" + nameOfOtherUser);
            }

        });
    }



}



