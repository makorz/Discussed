package pl.makorz.discussed.Controllers.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import pl.makorz.discussed.Models.Adapters.ConversationsAdapter;
import pl.makorz.discussed.Controllers.ChatActivity;
import pl.makorz.discussed.Models.Conversation;
import pl.makorz.discussed.R;

public class ConversationsFragment extends Fragment {

    private static final String TAG = "ConversationsFragment";
    public static final String USERS_ID_ARRAY = "usersParticipatingID";
    public static final String NAME_FIELD = "displayName";

    private ConversationsAdapter adapter;
    private RecyclerView conversationRecycler;
    private String otherUserID, otherUserName;
    private ProgressBar waitUntilChatAppears;
    private RelativeLayout layoutToDimWhenSearching;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View conversationView = inflater.inflate(R.layout.fragment_conversations, container, false);
        waitUntilChatAppears = conversationView.findViewById(R.id.progressBarOfSearchConversation);
        layoutToDimWhenSearching = conversationView.findViewById(R.id.layout_fragment_conversations);
        conversationRecycler = conversationView.findViewById(R.id.users_recycler);
        setUpRecyclerView();
        setOnSingleClickAdapterListener(adapter);
        setOnLongClickAdapterListener(adapter);
        return conversationView;

    }

    public void setUpRecyclerView() {

        assert user != null;
        Query queryListOfConversations2 = db.collection("chats").whereArrayContains("usersThatHaveNotDeletedConversation", user.getUid());
        //Configuring adapter to populate recyclerview
        FirestoreRecyclerOptions<Conversation> options = new FirestoreRecyclerOptions.Builder<Conversation>()
                .setQuery(queryListOfConversations2, Conversation.class)
                .build();
        adapter = new ConversationsAdapter(options);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        conversationRecycler.setLayoutManager(layoutManager);
        conversationRecycler.setAdapter(adapter);

    }

    private void setOnSingleClickAdapterListener(ConversationsAdapter adapter) {

        adapter.setOnItemCLickListener(new ConversationsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String chatID, int position) {
                if (adapter.isClickable) {
                    adapter.isClickable = false;
                    layoutToDimWhenSearching.setAlpha(0.15f);
                    waitUntilChatAppears.setVisibility(View.VISIBLE);
                    DocumentReference docRef = db.collection("chats").document(chatID);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null) {

                                    List<String> listOfUsers = (List<String>) document.get(USERS_ID_ARRAY);
                                    int index = listOfUsers.indexOf(user.getUid());
                                    if (index == 0) {
                                        index++;
                                    } else {
                                        index--;
                                    }
                                    otherUserID = listOfUsers.get(index);

                                    DocumentReference docRef2 = db.collection("users").document(otherUserID);
                                    docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document2 = task.getResult();
                                                if (document2 != null) {
                                                    otherUserName = document2.getString(NAME_FIELD);
                                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                                    adapter.stopListening();
                                                    intent.putExtra("chatIdIntent", chatID);
                                                    intent.putExtra("otherUserName", otherUserName);
                                                    intent.putExtra("idOfOtherUser", otherUserID);
                                                    intent.putExtra("currentUserID", user.getUid());
                                                    startActivity(intent);
                                                    layoutToDimWhenSearching.setAlpha(1f);
                                                    waitUntilChatAppears.setVisibility(View.INVISIBLE);
                                                    adapter.isClickable = true;
                                                    Toast.makeText(getContext(), "Position: " + position + " ChatID: " + chatID, Toast.LENGTH_SHORT).show();


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
        });

    }

    private void setOnLongClickAdapterListener(ConversationsAdapter adapter) {

        adapter.setOnLongItemClickListener(new ConversationsAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(String chatID, String userName) {

                LayoutInflater inflaterDialog = LayoutInflater.from(getContext());
                View deleteChatAlertView = inflaterDialog.inflate(R.layout.dialog_uncover, null);
                TextView deleteChatText = deleteChatAlertView.findViewById(R.id.uncover_text);
                deleteChatText.setText("Are You sure You want to delete chat with " + userName);

                AlertDialog deleteDialog = new AlertDialog.Builder(getContext())
                        .setView(deleteChatAlertView)  // What to use in dialog box
                        .setPositiveButton(R.string.yes_text_dialog_boxes, null)
                        .setNegativeButton(R.string.no_text_dialog_boxes, null)
                        .show();

                deleteDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Query checkQueryIfSomeoneDeletedChat = FirebaseFirestore.getInstance().collection("chats").document(chatID).collection("chatUsers").limit(5);

                        checkQueryIfSomeoneDeletedChat.get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().isEmpty()) {

                                                Log.d(TAG, "NO ONE HERE");

                                                db.collection("chats").document(chatID).delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "AllChat successfully deleted!");
                                                                Fragment fragment = new ConversationsFragment();
                                                                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                                                ft.detach(ConversationsFragment.this);
                                                                ft.replace(R.id.content_frame, fragment, "visible_fragment");
                                                                ft.commit();
                                                                deleteDialog.dismiss();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w(TAG, "Error deleting document", e);
                                                            }
                                                        });

                                            } else {

                                                List<String> userIDList = new ArrayList<>();

                                                int nrOfChatMembers = 0;
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    nrOfChatMembers ++;
                                                    userIDList.add(document.getString("userID"));
                                                }

                                                if (nrOfChatMembers == 1) {

                                                    db.collection("chats").document(chatID).delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d(TAG, "AllChat successfully deleted!");
                                                                    Fragment fragment = new ConversationsFragment();
                                                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                                                    ft.detach(ConversationsFragment.this);
                                                                    ft.replace(R.id.content_frame, fragment, "visible_fragment");
                                                                    ft.commit();
                                                                    deleteDialog.dismiss();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.w(TAG, "Error deleting document", e);
                                                                }
                                                            });



                                                } else {
                                                    db.collection("chats").document(chatID).collection("chatUsers").document(user.getUid()).delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d(TAG, "Single ChatMemberDocument successfully deleted!");
                                                                    Fragment fragment = new ConversationsFragment();
                                                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                                                    ft.detach(ConversationsFragment.this);
                                                                    ft.replace(R.id.content_frame, fragment, "visible_fragment");
                                                                    ft.commit();
                                                                    deleteDialog.dismiss();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.w(TAG, "Error deleting document", e);
                                                                }
                                                            });

                                                    db.collection("users").document(userIDList.get(0)).update("otherUsersIDFromStartedChatsArray", FieldValue.arrayRemove(userIDList.get(1)));
                                                    db.collection("users").document(userIDList.get(1)).update("otherUsersIDFromStartedChatsArray", FieldValue.arrayRemove(userIDList.get(0)));
                                                    db.collection("chats").document(chatID).update("usersThatHaveNotDeletedConversation", FieldValue.arrayRemove(user.getUid()));

                                                }
                                            }
                                        }
                                    }
                                });

                    }
                });

                deleteDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Fragment fragment = new ConversationsFragment();
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.detach(ConversationsFragment.this);
                        ft.replace(R.id.content_frame, fragment, "visible_fragment");
                        ft.commit();
                        deleteDialog.dismiss();
                    }
                });

            }
        });
    }



    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


}