package pl.makorz.discussed.Adpaters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import pl.makorz.discussed.Models.Conversation;
import pl.makorz.discussed.R;

public class ConversationsAdapter extends FirestoreRecyclerAdapter<Conversation, ConversationsAdapter.ViewHolder> {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private static OnItemClickListener listener;
    private static String chatID;

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Conversation model) {

        chatID = model.getChatID();
        model.checkLastMessage(chatID);
        holder.textOfMessage.setText(model.getLastMessage());
        List<String> listOfUsers = model.getUsersParticipatingID();
        int index = listOfUsers.indexOf(user.getUid());
        if (index == 0) {
            index++;
        } else {
            index--;
        }
        holder.userName.setText(model.getUsersParticipatingName().get(index));
    }

    public ConversationsAdapter(@NonNull FirestoreRecyclerOptions<Conversation> options) {
        super(options);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textOfMessage, userName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(android.R.id.text1);
            textOfMessage = itemView.findViewById(android.R.id.text2);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(chatID, position);
                    }

                }
            });

        }
    }

    // This methods below allow to send data from adapter to activity
    public interface OnItemClickListener {
        void onItemClick(String chatID, int position);
    }

    public void setOnItemCLickListener(OnItemClickListener listener) {
        ConversationsAdapter.listener = listener;
    }
}
