package pl.makorz.discussed.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Objects;

import pl.makorz.discussed.Adpaters.ConversationsAdapter;
import pl.makorz.discussed.ChatActivity;
import pl.makorz.discussed.Models.Conversation;
import pl.makorz.discussed.R;

public class ConversationsFragment extends Fragment  {

    private View conversationView;
    private RecyclerView conversationRecycler;
    private static final String TAG = "ConversationsFragment";
    private ConversationsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        conversationView = inflater.inflate(R.layout.fragment_conversations,container,false);
        conversationRecycler = conversationView.findViewById(R.id.users_recycler);
        setUpRecyclerView();

        adapter.setOnItemCLickListener(new ConversationsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String chatID, int position) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("chatIdIntent", chatID);
                Objects.requireNonNull(getActivity()).startActivity(intent);
                Toast.makeText(getContext(), "Position" + position + "ChatID" + chatID , Toast.LENGTH_SHORT).show();
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
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }


}