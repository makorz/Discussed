package pl.makorz.discussed.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import pl.makorz.discussed.Models.Adapters.MessageInChatAdapter;
import pl.makorz.discussed.Models.MessageInChat;
import pl.makorz.discussed.R;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    public static final String USERS_ID_ARRAY = "usersParticipatingID";
    public static final String WAS_GRADED = "wasGraded";
    public static final String NAME_FIELD = "displayName";
    public static final String USERS_NAME_ARRAY = "usersParticipatingName";
    public static final String USERS_PHOTO_URI_ARRAY = "usersParticipatingFirstImageUri";
    public static final String POINTS_FROM_OTHER_USER = "pointsFromOtherUser";

    private List<String> listOfUsers, listOfUserNames, listOfUserPhotoUri;
    private String chatIdIntent, idOfOtherUser, otherUserName, displayName;
    int index;
    private EditText messageText;
    private RecyclerView messagesRecycler;
    private MessageInChatAdapter messagesAdapter;
    private DocumentSnapshot documentOfUser;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

        Intent intent = getIntent();
        chatIdIntent = intent.getStringExtra("chatIdIntent");
        otherUserName = intent.getStringExtra("otherUserName");
        idOfOtherUser = intent.getStringExtra("idOfOtherUser");
        getSupportActionBar().setTitle(otherUserName);
        messageText = findViewById(R.id.messageEditText);

        DocumentReference docRefUser = db.collection("users").document(user.getUid());
        docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    documentOfUser = task.getResult();
                    if (documentOfUser != null) {
                    displayName = documentOfUser.getString(NAME_FIELD);
                    }else {
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }

            }
        });

        // What happens after send button click
        findViewById(R.id.sendMessageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputFilter lengthFilter = new InputFilter.LengthFilter(1250);
                messageText.setFilters(new InputFilter[]{lengthFilter});
                if (messageText.getText().toString().length() <= 1250) {
                    MessageInChat messageChat = new MessageInChat(messageText.getText().toString(), user.getUid(), displayName, new Date(), false);
                    db.collection("chats").document(chatIdIntent).collection("messages").add(messageChat).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            String messageID = documentReference.getId();
                            Map<String, Object> message = new HashMap<>();
                            message.put("messageID",messageID);
                            db.collection("chats").document(chatIdIntent).collection("messages").document(messageID).update(message);
                        }
                    });
                    messageText.setText("");
                } else {
                    Toast.makeText(ChatActivity.this, "Typed text is too long! Cut it to 1250 characters!", Toast.LENGTH_SHORT).show();
                }


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

        messagesAdapter.setOnLongItemCLickListener(new MessageInChatAdapter.onLongItemClickListener() {
            @Override
            public void onLongItemClick(int points, int position, String idMessage, boolean wasGraded) {

                if (!wasGraded) {
                    DocumentReference docRef = db.collection("chats").document(chatIdIntent);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentOfChat = task.getResult();
                                if (documentOfChat != null) {

                                    docRef.collection("chatUsers").document(idOfOtherUser).update(POINTS_FROM_OTHER_USER, FieldValue.increment(points));
                                    Toast.makeText(ChatActivity.this, "You awarded " + otherUserName + " with " + points + " points!", Toast.LENGTH_SHORT).show();

                                    DocumentReference docRef2 = db.collection("chats").document(chatIdIntent).
                                            collection("messages").document(idMessage);

                                    docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot documentOfMessage = task.getResult();
                                                if (documentOfMessage != null) {
                                                    db.collection("chats").document(chatIdIntent).collection("messages")
                                                            .document(idMessage).update(WAS_GRADED, true);
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

                } else {
                    Toast.makeText(ChatActivity.this, "Message was graded!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        messagesAdapter.startListening();
        checkUserNameAndPhotoUri(chatIdIntent,user.getUid());
        getSupportActionBar().setTitle(otherUserName);
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
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_alien_profile:
                Intent intent = new Intent(this, AlienProfileActivity.class);
                intent.putExtra("idOfOtherUser",idOfOtherUser);
                intent.putExtra("chatIdIntent",chatIdIntent);
                intent.putExtra("currentUserID",user.getUid());
                intent.putExtra("otherUserName",otherUserName);
                startActivity(intent);
                onStop();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void checkUserNameAndPhotoUri(final String chatID, String currentUserID) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("chats").document(chatID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentOfChat = task.getResult();
                    if (documentOfChat != null) {
                        listOfUsers = (List<String>) documentOfChat.get(USERS_ID_ARRAY);
                        listOfUserNames = (List<String>) documentOfChat.get(USERS_NAME_ARRAY);
                        listOfUserPhotoUri = (List<String>) documentOfChat.get(USERS_PHOTO_URI_ARRAY);
                        DocumentReference docRef2 = db.collection("users").document(idOfOtherUser);

                        docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentOfUser = task.getResult();
                                    if (documentOfUser != null) {
                                        index = listOfUsers.indexOf(user.getUid());
                                        if (!listOfUserNames.contains(documentOfUser.getString("displayName"))) {

                                            String a = documentOfUser.getString("displayName");
                                            otherUserName = a;
                                            String b = listOfUserNames.get(index);

                                            db.collection("chats").document(chatID)
                                                    .update(USERS_NAME_ARRAY, FieldValue.delete());
                                            if (index == 0) {
                                                docRef.update(USERS_NAME_ARRAY, FieldValue.arrayUnion(b,a));
                                            } else {
                                                docRef.update(USERS_NAME_ARRAY, FieldValue.arrayUnion(a,b));
                                            }
                                        }
                                        if (!listOfUserPhotoUri.contains(documentOfUser.getString("firstPhotoUri"))) {

                                            String a = documentOfUser.getString("firstPhotoUri");
                                            String b = listOfUserNames.get(index);

                                            db.collection("chats").document(chatID)
                                                    .update(USERS_PHOTO_URI_ARRAY, FieldValue.delete());
                                            if (index == 0) {
                                                docRef.update(USERS_PHOTO_URI_ARRAY, FieldValue.arrayUnion(b,a));
                                            } else {
                                                docRef.update(USERS_PHOTO_URI_ARRAY, FieldValue.arrayUnion(a,b));
                                            }
                                        }

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

}