package pl.makorz.discussed.Controllers.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

import pl.makorz.discussed.Models.Adapters.ConversationsAdapter;
import pl.makorz.discussed.Controllers.ChatActivity;
import pl.makorz.discussed.Models.Conversation;
import pl.makorz.discussed.R;

public class ConversationsFragment extends Fragment  {

    private RecyclerView conversationRecycler;
    private static final String TAG = "ConversationsFragment";
    private ConversationsAdapter adapter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    String otherUserID;
    private String otherUserName;
    public static final String USERS_ID_ARRAY = "usersParticipatingID";
    public static final String NAME_FIELD = "displayName";
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View conversationView = inflater.inflate(R.layout.fragment_conversations, container, false);
        conversationRecycler = conversationView.findViewById(R.id.users_recycler);
        setUpRecyclerView();

        adapter.setOnItemCLickListener(new ConversationsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String chatID, int position) {
                DocumentReference docRef = db.collection("chats").document(chatID);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {

                                List<String> listOfUsers = (List<String>) document.get(USERS_ID_ARRAY);
                                int index = listOfUsers.indexOf(user.getUid());
                                if (index == 0) {
                                    index++;
                                } else {
                                    index--;
                                }
                                otherUserID = listOfUsers.get(index);

                                DocumentReference docRef2 = db.collection("users").document(otherUserID);
                                docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document2 = task.getResult();
                                            if (document2 != null) {
                                                otherUserName = document2.getString(NAME_FIELD);
                                                Intent intent = new Intent(getActivity(), ChatActivity.class);
                                                adapter.stopListening();
                                                intent.putExtra("chatIdIntent", chatID);
                                                intent.putExtra("otherUserName",otherUserName);
                                                intent.putExtra("idOfOtherUser",otherUserID);
                                                intent.putExtra("currentUserID",user.getUid());
                                                startActivity(intent);
                                                Toast.makeText(getContext(), "Position: " + position + " ChatID: " + chatID , Toast.LENGTH_SHORT).show();


                                            } else {
                                                Log.d("LOGGER", "No such document");
                                            }
                                        } else {
                                            Log.d("LOGGER", "get failed with ", task.getException());
                                        }
                                    }
                                });
                            } else {
                                Log.d("LOGGER", "No such document");
                            }
                        } else {
                            Log.d("LOGGER", "get failed with ", task.getException());
                        }
                    }
                });
            }
        });
        return conversationView;

    }

    public void setUpRecyclerView () {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Initialize Cloud Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Ask the Cloud Firestore
        assert user != null;
        Query queryListOfConversations2 = db.collection("chats").whereArrayContains("usersParticipatingID", user.getUid());
        //Configuring adapter to populate recyclerview
        FirestoreRecyclerOptions<Conversation> options = new FirestoreRecyclerOptions.Builder<Conversation>()
                .setQuery(queryListOfConversations2, Conversation.class)
                .build();
        adapter = new ConversationsAdapter(options);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        conversationRecycler.setLayoutManager(layoutManager);
        conversationRecycler.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }
    @Override
    public void onResume() {
        super.onResume();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();

    }


}