package pl.makorz.discussed.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import pl.makorz.discussed.Models.User;
import pl.makorz.discussed.Models.UserViewHolder;
import pl.makorz.discussed.R;

public class ConversationsFragment extends Fragment {

    private View userView;
    private RecyclerView usersRecycler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        userView = inflater.inflate(R.layout.fragment_conversations,container,false);
        usersRecycler = userView.findViewById(R.id.users_recycler);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        usersRecycler.setLayoutManager(layoutManager);

        return userView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Initialize Cloud Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Ask the Cloud Firestore
        Query queryListOfUsers = db.collection("users");
        //Configuring adapter to populate recyclerview
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(queryListOfUsers, User.class)
                .setLifecycleOwner(this)
                .build();

        FirestoreRecyclerAdapter userAdapter = new FirestoreRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            public void onBindViewHolder(UserViewHolder holder, int position, User model) {
                // Bind the Chat object to the ChatHolder
                TextView nameUser = holder.itemView.findViewById(android.R.id.text1);
                TextView emojiUser = holder.itemView.findViewById(android.R.id.text2);
                nameUser.setText(model.getDisplayName());
                emojiUser.setText(model.getDescription());
            }

            @Override
            public UserViewHolder onCreateViewHolder(ViewGroup group, int i) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                userView = LayoutInflater.from(group.getContext())
                        .inflate(android.R.layout.simple_list_item_2, group, false);
                return new UserViewHolder(userView);
            }
        };
        usersRecycler.setAdapter(userAdapter);
    }
}