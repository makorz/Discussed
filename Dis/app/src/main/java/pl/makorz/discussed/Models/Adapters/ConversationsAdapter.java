package pl.makorz.discussed.Models.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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

    public static final int CONVERSATION_NORMAL = 0;
    public static final int CONVERSATION_DELETE = 1;

    private static OnItemClickListener listener;
    private static OnLongItemClickListener longListener;
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
//        if(viewType == CONVERSATION_DELETE) {
//            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_conversation_view_delete, parent, false);
//        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_conversation_view, parent, false);
//        }

        ViewHolder vh = new ViewHolder(view);
        context = parent.getContext();
        return vh;

    }



    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textOfMessage, userName, chatIDtext;
        ImageView imageOfUser;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.info_text);
            textOfMessage = itemView.findViewById(R.id.message_text);
            imageOfUser = itemView.findViewById(R.id.info_image);
            chatIDtext = itemView.findViewById(R.id.chatID_text);
            cardView = itemView.findViewById(R.id.card_view);

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

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    cardView.setCardBackgroundColor(Color.RED);
                    imageOfUser.setImageResource(R.drawable.delete_icon);
                    String userName2 = userName.getText().toString();
                    userName.setText("");
                    textOfMessage.setText("");
                    String chatIDofPosition = chatIDtext.getText().toString();
                    longListener.onLongItemClick(chatIDofPosition, userName2);
                    return true;

                }
            });

        }
    }

    // This methods below allow to send data from adapter to activity
    public interface OnItemClickListener {
        void onItemClick(String chatID, int position);
    }

    // This methods below allow to send data from adapter to activity
    public interface OnLongItemClickListener {
        void onLongItemClick(String chatID, String userName);
    }

    public void setOnLongItemClickListener(OnLongItemClickListener longListener) {
        ConversationsAdapter.longListener = longListener;
    }

    public void setOnItemCLickListener(OnItemClickListener listener) {
        ConversationsAdapter.listener = listener;
    }


}
