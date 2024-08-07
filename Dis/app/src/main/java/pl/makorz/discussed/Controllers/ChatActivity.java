package pl.makorz.discussed.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import com.google.gson.Gson;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import pl.makorz.discussed.Controllers.Functions.APIService;
import pl.makorz.discussed.Controllers.Notifications.Client;
import pl.makorz.discussed.Controllers.Notifications.Data;
import pl.makorz.discussed.Controllers.Notifications.Response;
import pl.makorz.discussed.Controllers.Notifications.Sender;
import pl.makorz.discussed.Models.Adapters.MessageInChatAdapter;
import pl.makorz.discussed.Models.MessageInChat;
import pl.makorz.discussed.R;
import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    public static final String USERS_ID_ARRAY = "usersParticipatingID";
    public static final String WAS_GRADED = "wasGraded";
    public static final String NAME_FIELD = "displayName";
    public static final String USERS_NAME_ARRAY = "usersParticipatingName";
    public static final String USERS_PHOTO_URI_ARRAY = "usersParticipatingFirstImageUri";
    public static final String POINTS_FROM_OTHER_USER = "pointsFromOtherUser";
    public static final String PREMIUM_ACCOUNT = "premium";
    public static final String USER_THAT_HAVE_NOT_DELETED_CHAT = "usersThatHaveNotDeletedConversation";

    private List<String> listOfUsers, listOfUserNames, listOfUserPhotoUri, listOfUsersWhoDidNotDeletedChat;
    private String chatIdIntent, idOfOtherUser, otherUserName, displayName;
    private int index;
    private EditText messageText;
    private ImageButton sendButton;
    private RecyclerView messagesRecycler;
    private TextView infoText;
    private MessageInChatAdapter messagesAdapter;
    private DocumentSnapshot documentOfUser;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //no dark theme

        Intent intent = getIntent();
        chatIdIntent = intent.getStringExtra("chatIdIntent");
        otherUserName = intent.getStringExtra("otherUserName");
        idOfOtherUser = intent.getStringExtra("idOfOtherUser");
        getSupportActionBar().setTitle(otherUserName);

        messageText = findViewById(R.id.messageEditText);
        messagesRecycler = findViewById(R.id.messagesRecyclerView);
        sendButton = findViewById(R.id.sendMessageButton);
        infoText = findViewById(R.id.textview_chat_info);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        getCurrentUserNecessaryData();

        db.collection("chats").document(chatIdIntent).collection("chatUsers").document(user.getUid())
                .update("lastTimeInChatActivity",new Date());

        setSendButtonClickListener();

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

        setGradeButtonLongClickListener();
    }

    // Send notification from Chat
    private void sendNotification(String otherUserID, final String userID, final String message){
        DocumentReference docRefUser = db.collection("users").document(otherUserID);
        docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    documentOfUser = task.getResult();
                    if (documentOfUser != null) {

                        String token = documentOfUser.getString("fcmRegistrationToken");
                        Data data = new Data(otherUserID, R.drawable.notification_icon_white, message,
                                displayName, userID, displayName, chatIdIntent,0);
                        Sender sender = new Sender(data, token);

                        Gson gson = new Gson();
                        String json = gson.toJson(sender);

                        Log.d(TAG, displayName + message);

                        apiService.sendNotification(sender).enqueue(new Callback<Response>() {
                            @Override
                            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                                Log.d(TAG, String.valueOf(response.code()));
                                if (response.code() == 200) {
                                    assert response.body() != null;
                                    if (response.body().success != 1) {
                                        Log.d(TAG, "Failure while sending notification.");
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<Response> call, Throwable t) {

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
    // Check if another user change his/her name and first photo
    private void checkUserNameAndPhotoUri(final String chatID, String currentUserID) {

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
                        listOfUsersWhoDidNotDeletedChat = (List<String>) documentOfChat.get(USER_THAT_HAVE_NOT_DELETED_CHAT);

                        if (listOfUsersWhoDidNotDeletedChat.size() < 2) {
                            infoText.setText("Other user got rid off this chat, You can't write nothing more.");
                            infoText.setVisibility(View.VISIBLE);
                            messageText.setVisibility(View.GONE);
                            sendButton.setVisibility(View.GONE);
                        }

                        DocumentReference docRef2 = db.collection("users").document(idOfOtherUser);

                        docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentOfUser = task.getResult();
                                    if (documentOfUser != null) {
                                        index = listOfUsers.indexOf(currentUserID);
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
    // Get user data tha will be used in activity
    private void getCurrentUserNecessaryData() {

        DocumentReference docRefUser = db.collection("users").document(user.getUid());
        docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    documentOfUser = task.getResult();
                    if (documentOfUser != null) {
                        displayName = documentOfUser.getString(NAME_FIELD);
                    } else {
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }

            }
        });

    }
    // Set the sending message function on image button next to edit text field
    private void setSendButtonClickListener() {

        // What happens after send button click
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputFilter lengthFilter = new InputFilter.LengthFilter(1250);
                messageText.setFilters(new InputFilter[]{lengthFilter});
                if (messageText.getText().toString().length() <= 1250) {
                    if (messageText.getText().toString().length() >= 1) {
                        MessageInChat messageChat = new MessageInChat(messageText.getText().toString(), user.getUid(), displayName, new Date(), false);
                        Log.d("MessageInChat", String.valueOf(messageChat.wasGraded()));
                        db.collection("chats").document(chatIdIntent).collection("messages").add(messageChat).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                String messageID = documentReference.getId();
                                Map<String, Object> message = new HashMap<>();
                                message.put("messageID", messageID);
                                message.put("wasGraded", false);
                                db.collection("chats").document(chatIdIntent).collection("messages").document(messageID).update(message);

                            }
                        });
                        sendNotification(idOfOtherUser, user.getUid(), messageText.getText().toString());
                        messageText.setText("");
                    } else
                        Toast.makeText(ChatActivity.this, "Type something!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChatActivity.this, R.string.too_long_message_chat_activity_toast, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }
    // Set the long click listener on chat bubbles to grade them
    private void setGradeButtonLongClickListener() {

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
                                    db.collection("users").document(idOfOtherUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot documentOfUser = task.getResult();
                                                if (documentOfUser != null) {
                                                    boolean premium = documentOfUser.getBoolean(PREMIUM_ACCOUNT);
                                                    if (premium) {
                                                        docRef.collection("chatUsers").document(idOfOtherUser).update(POINTS_FROM_OTHER_USER, FieldValue.increment(points * 2L));
                                                        Toast.makeText(ChatActivity.this, getString(R.string.award_info_text_1_chat_activity_toast) + otherUserName
                                                                + getString(R.string.award_info_text_2_chat_activity_toast) + points * 2
                                                                + getString(R.string.award_info_text_3_chat_activity_toast), Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        docRef.collection("chatUsers").document(idOfOtherUser).update(POINTS_FROM_OTHER_USER, FieldValue.increment(points));
                                                        Toast.makeText(ChatActivity.this, getString(R.string.award_info_text_1_chat_activity_toast) + otherUserName
                                                                + getString(R.string.award_info_text_2_chat_activity_toast) + points
                                                                + getString(R.string.award_info_text_3_chat_activity_toast), Toast.LENGTH_SHORT).show();
                                                    }
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

                                                }
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
                    Toast.makeText(ChatActivity.this, R.string.graded_message_chat_activity_toast, Toast.LENGTH_SHORT).show();
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
        db.collection("chats").document(chatIdIntent).collection("chatUsers").document(user.getUid())
                .update("lastTimeInChatActivity",new Date());
    }

    @Override
    public void onBackPressed() {
        //If Chat starts from notification and it's root start MainActivity instead of completely closing app
        if (isTaskRoot()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        super.onBackPressed();
        finish();


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



}