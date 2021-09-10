package pl.makorz.discussed.Models.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
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
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.makorz.discussed.Models.Conversation;
import pl.makorz.discussed.R;

public class ConversationsAdapter extends FirestoreRecyclerAdapter<Conversation, ConversationsAdapter.ViewHolder> {

    private static final String TAG = "ConversationAdapter";

    private static OnItemClickListener listener;
    private static OnLongItemClickListener longListener;
    private Context context;
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    public boolean isClickable = true; //Prevent from multiple clicks on recyclerview items, preventing from launching multiple ChatActivities

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Conversation model) {

        String chatID = model.getChatID();
        checkLastMessage(chatID);
        checkIfChatWasViewed(chatID, currentUser.getUid());
        holder.chatIDtext.setText(model.getChatID());

        // Finding otherUserIndex
        List<String> listOfUsers = model.getUsersParticipatingID();
        int index = listOfUsers.indexOf(currentUser.getUid());
        if (index == 0) {
            index++;
        } else {
            index--;
        }

        boolean wasUserInActivity;
        if (listOfUsers.indexOf(currentUser.getUid()) == 0) {
            wasUserInActivity = model.getWasUserInActivityNr0();
        } else {
            wasUserInActivity = model.getWasUserInActivityNr1();
        }
        Log.d("BINDING", String.valueOf(wasUserInActivity));

        Boolean isFirstPhotoUncovered = model.getIsFirstPhotoOfUserUncovered().get(index);
        if (isFirstPhotoUncovered) {
            Glide.with(context).load(model.getUsersParticipatingFirstImageUri().get(index)).into(holder.imageOfUser);
        } else {
            holder.imageOfUser.setImageResource(R.drawable.main_logo_icon_transparent2);
        }

        if (wasUserInActivity) {
            holder.textOfMessage.setTypeface(Typeface.DEFAULT);
            holder.userName.setTypeface(Typeface.DEFAULT);
            holder.textOfMessage.setText(model.getLastMessage());
            holder.userName.setText(model.getUsersParticipatingName().get(index));
        } else {
            holder.textOfMessage.setTypeface(Typeface.DEFAULT_BOLD);
            holder.userName.setTypeface(Typeface.DEFAULT_BOLD);
            holder.textOfMessage.setText(model.getLastMessage());
            holder.userName.setText(model.getUsersParticipatingName().get(index));
        }

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

    public void checkLastMessage(final String chatID) {
        Query queryMessages = FirebaseFirestore.getInstance().collection("chats").document(chatID)
                .collection("messages").orderBy("dateOfMessage", Query.Direction.DESCENDING).limit(1);

        queryMessages.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> chat = new HashMap<>();
                                String textShort = document.get("textOfMessage").toString();
                                Date lastMessageDate = document.getDate("dateOfMessage");
                                if (textShort.length() > 40) {
                                    textShort = textShort.substring(0, 25) + "...";
                                }
                                chat.put("lastMessage", textShort);
                                chat.put("lastMessageDate", lastMessageDate);
                                FirebaseFirestore.getInstance().collection("chats").document(chatID).set(chat, SetOptions.merge());
                            }
                        }
                    }
                });
    }

    public void checkIfChatWasViewed(final String chatID, final String userID) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("chats").document(chatID);

        // Check if user Viewed a Message
        Query queryUsers = FirebaseFirestore.getInstance().collection("chats").document(chatID)
                .collection("chatUsers").whereEqualTo("userID", userID);

        queryUsers.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Date lastTimeInChatActivity = document.getDate("lastTimeInChatActivity");
                                Log.d("ASASFAF LATE", lastTimeInChatActivity.toString());

                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot documentOfChat = task.getResult();
                                            if (documentOfChat != null) {
                                                List<String> listOfUsers = (List<String>) documentOfChat.get("usersParticipatingID");
                                                Date lastMessageDate = documentOfChat.getDate("lastMessageDate");
                                                int index = listOfUsers.indexOf(userID);

                                                if (lastTimeInChatActivity.after(lastMessageDate)) {
                                                    db.collection("chats").document(chatID)
                                                            .update("wasUserInActivityNr" + index, true);
                                                } else {
                                                    db.collection("chats").document(chatID)
                                                            .update("wasUserInActivityNr" + index, false);
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
                    }
                });
    }


}
