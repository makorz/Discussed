package pl.makorz.discussed.Models.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import pl.makorz.discussed.Models.Conversation;
import pl.makorz.discussed.R;


public class ConversationsAdapter extends FirestoreRecyclerAdapter<Conversation, ConversationsAdapter.ViewHolder> {

    private static OnItemClickListener listener;
    private Context context;
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Conversation model) {

        String chatID = model.getChatID();
        model.checkLastMessage(chatID);

        holder.chatIDtext.setText(model.getChatID());
        holder.textOfMessage.setText(model.getLastMessage());
        List<String> listOfUsers = model.getUsersParticipatingID();
        int index = listOfUsers.indexOf(currentUser.getUid());
        if (index == 0) {
            index++;
        } else {
            index--;
        }
        Boolean isFirstPhotoUncovered = model.getIsFirstPhotoOfUserUncovered().get(index);
        if (isFirstPhotoUncovered) {
            Glide.with(context).load(model.getUsersParticipatingFirstImageUri().get(index)).into(holder.imageOfUser);
        } else {
            holder.imageOfUser.setImageResource(R.drawable.main_logo_icon_transparent2);
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
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_conversation_view, parent, false);
        ViewHolder vh = new ViewHolder(view);
        context = parent.getContext();
        return vh;

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textOfMessage, userName, chatIDtext;
        ImageView imageOfUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.info_text);
            textOfMessage = itemView.findViewById(R.id.message_text);
            imageOfUser = itemView.findViewById(R.id.info_image);
            chatIDtext = itemView.findViewById(R.id.chatID_text);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        String chatIDofPosition = chatIDtext.getText().toString();
                        listener.onItemClick(chatIDofPosition, position);
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
