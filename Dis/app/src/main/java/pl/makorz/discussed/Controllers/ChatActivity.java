package pl.makorz.discussed.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.Date;
import pl.makorz.discussed.Models.Adapters.MessageInChatAdapter;
import pl.makorz.discussed.Models.MessageInChat;
import pl.makorz.discussed.R;


public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private String chatIdIntent;
   // public static final String EXTRA_CHAT_INFO = "chatNumber";
//    public static final String TEXT_OF_MESSAGE = "textOfMessage";
//    public static final String DATE_OF_MESSAGE = "dateOfMessage";
//    public static final String MESSAGE_NUMBER = "messageNumber";
//    public static final String USER_WHO_SEARCHED_ID = "userWhoSearchedID";
//    public static final String USER_WHO_WAS_PICKED_ID = "userWhoWasPickedID";
//    public static final String NR_OF_POINTS_FOR_USER_WHO_SEARCHED = "nrOfPointsUserWhoSearched";
//    public static final String NR_OF_POINTS_FOR_USER_WHO_WAS_PICKED = "nrOfPointsUserWhoWasPicked";

    private EditText messageText;
    private RecyclerView messagesRecycler;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    MessageInChatAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        chatIdIntent = intent.getStringExtra("chatIdIntent");
        messageText = findViewById(R.id.messageEditText);

        // What happens after send button click
        findViewById(R.id.sendMessageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageInChat messageChat = new MessageInChat(messageText.getText().toString(), user.getUid(), user.getDisplayName(), new Date());
                db.collection("chats").document(chatIdIntent).collection("messages").add(messageChat);
                messageText.setText("");


            }
        });

        messagesRecycler = findViewById(R.id.messagesRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecycler.setLayoutManager(layoutManager);

        //Ask Firebase what You want
        Query queryMessages = FirebaseFirestore.getInstance().collection("chats").document(chatIdIntent).collection("messages").orderBy("dateOfMessage", Query.Direction.ASCENDING);
        //Configuring adapter to populate recyclerview
        FirestoreRecyclerOptions<MessageInChat> options = new FirestoreRecyclerOptions.Builder<MessageInChat>()
                .setQuery(queryMessages, MessageInChat.class)
                .setLifecycleOwner(this)
                .build();

        //Populating recyclerview
        messagesAdapter = new MessageInChatAdapter(options);
        messagesAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                messagesRecycler.scrollToPosition(messagesAdapter.getItemCount() - 1);
            }
        });
        messagesRecycler.setAdapter(messagesAdapter);

    }

//    public void sendMessage(View v) {
//
//        String message = messageText.getText().toString();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmSS", Locale.getDefault());
//        String currentDateAndTime = sdf.format(new Date());
//        int messageNumber = 0;
//
//        Map<String, Object> messageSent = new HashMap<>();
//        messageSent.put(TEXT_OF_MESSAGE, message);
//        messageSent.put(DATE_OF_MESSAGE, currentDateAndTime);
//        messageSent.put(MESSAGE_NUMBER, messageNumber);
//
//        db.collection("chats").document("chatID").collection("messages").add(messageSent)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                    }
//                });
////                .addOnSuccessListener(new OnSuccessListener<Void>() {
////                    @Override
////                    public void onSuccess(Void aVoid) {
////                        Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
////                    }
////                })
////                .addOnFailureListener(new OnFailureListener() {
////                    @Override
////                    public void onFailure(@NonNull Exception e) {
////                        Toast.makeText(ChatActivity.this, "Error!", Toast.LENGTH_SHORT).show();
////                        Log.d(TAG, e.toString());
////                    }
////                });
//
//        if (messageNumber <=0) {
//
//            Map<String, Object> userWhoSearched = new HashMap<>();
//            userWhoSearched.put(USER_WHO_SEARCHED_ID, "MateuszID");
//            userWhoSearched.put(USER_WHO_WAS_PICKED_ID, "AlfredID");
//            userWhoSearched.put(NR_OF_POINTS_FOR_USER_WHO_SEARCHED, "0");
//            userWhoSearched.put(NR_OF_POINTS_FOR_USER_WHO_WAS_PICKED, "0");
//
//            db.collection("chats").document("chatID").set(userWhoSearched)
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Toast.makeText(ChatActivity.this, "Info sent", Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(ChatActivity.this, "Error!", Toast.LENGTH_SHORT).show();
//                            Log.d(TAG, e.toString());
//                        }
//                    });
//
//        }
//
//    }
//
//    public void loadMessages(View v) {
//        db.collection("chats").document("chatID").collection("messages").document("MessageID").get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        if (documentSnapshot.exists()){
//
//                        } else {
//                            Toast.makeText(ChatActivity.this, "Document not found!", Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(ChatActivity.this, "Error!", Toast.LENGTH_SHORT).show();
//                        Log.d(TAG, e.toString());
//                    }
//                });
//
//    }

    @Override
    protected void onStart() {
        super.onStart();
        messagesAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        messagesAdapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_alien_profile:
                Intent intent = new Intent(this, AlienProfileActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}