package pl.makorz.discussed.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import pl.makorz.discussed.ChatActivity;
import pl.makorz.discussed.Models.Topic;
import pl.makorz.discussed.Models.TopicViewHolder;
import pl.makorz.discussed.R;


public class MainFragment extends Fragment {

    private View mainView;
    private RecyclerView topicsRecycler;
    private static final String TAG = "MainActivity";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.fragment_main,container,false);
        topicsRecycler = mainView.findViewById(R.id.topics_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        topicsRecycler.setLayoutManager(layoutManager);

        Button button = (Button) mainView.findViewById(R.id.button_search_chatmate);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Intent intent = new Intent(getActivity(), ChatActivity.class);
                //intent.putExtra(ChatActivity.EXTRA_CHAT_INFO, 1);
                getActivity().startActivity(intent);

            }
        });





        return mainView;
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
                // Bind the Chat object to the ChatHolder
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
}