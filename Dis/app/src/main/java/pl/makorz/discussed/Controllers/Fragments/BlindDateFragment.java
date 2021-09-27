package pl.makorz.discussed.Controllers.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.makorz.discussed.Controllers.ChatActivity;
import pl.makorz.discussed.Models.Adapters.BlindDatesAdapter;
import pl.makorz.discussed.Controllers.BlindDateActivity;
import pl.makorz.discussed.Models.Adapters.ConversationsAdapter;
import pl.makorz.discussed.Models.BlindDate;
import pl.makorz.discussed.Models.Conversation;
import pl.makorz.discussed.R;

public class BlindDateFragment extends Fragment {

    private static final String TAG = "BlindDatesFragment";

    public static final String USER_NAME_NR_0 = "userNameNr0";
    public static final String USER_NAME_NR_1 = "userNameNr1";
    public static final String USER_NAME_NR_2 = "userNameNr2";
    public static final String USER_NAME_NR_3 = "userNameNr3";
    public static final String USER_ID_NR_0 = "userIDNr0";
    public static final String USER_ID_NR_1 = "userIDNr1";
    public static final String USER_ID_NR_2 = "userIDNr2";
    public static final String USER_ID_NR_3 = "userIDNr3";
    public static final String USER_NR_1_WAS_REJECTED = "wasUserRejectedNr1";
    public static final String USER_NR_2_WAS_REJECTED = "wasUserRejectedNr2";
    public static final String USER_NR_3_WAS_REJECTED = "wasUserRejectedNr3";
    public static final String BLIND_DATE_ID = "blindDateID";
    public static final String DATE_OF_CREATION_BLIND_DATE = "dateOfBlindDateCreation";
    public static final String ROUND_NO = "numberOfRoundInBlindDate";
    public static final String DATE_OF_ROUND_2_STARTED = "dateOfRound2Started";

    private BlindDatesAdapter adapter;
    private RecyclerView blindDatesRecycler;
    private ProgressBar waitUntilChatAppears;
    private RelativeLayout layoutToDimWhenSearching;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View blindDateView = inflater.inflate(R.layout.fragment_bilnddates, container, false);
        blindDatesRecycler = blindDateView.findViewById(R.id.blindDates_recycler);
        waitUntilChatAppears = blindDateView.findViewById(R.id.progressBarOfSearchBlindDates);
        layoutToDimWhenSearching = blindDateView.findViewById(R.id.layout_fragment_blindDates);
        setUpRecyclerView();
        setOnSingleClickAdapterListener(adapter);
        setOnLongClickAdapterListener(adapter);
        return blindDateView;

    }

    public void setUpRecyclerView() {

        assert user != null;
        Query queryListOfBlindDates = db.collection("blindDates").whereArrayContains("usersParticipatingThatHaveNotDeletedBlindDates", user.getUid());
        //Configuring adapter to populate recyclerview
        FirestoreRecyclerOptions<BlindDate> options = new FirestoreRecyclerOptions.Builder<BlindDate>()
                .setQuery(queryListOfBlindDates, BlindDate.class)
                .build();
        adapter = new BlindDatesAdapter(options);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        blindDatesRecycler.setLayoutManager(layoutManager);
        blindDatesRecycler.setAdapter(adapter);

    }

    public void setOnSingleClickAdapterListener(BlindDatesAdapter adapter) {

        adapter.setOnItemCLickListener(new BlindDatesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String blindDateID, int position) {
                if (adapter.isClickable) {
                    adapter.isClickable = false;
                    layoutToDimWhenSearching.setAlpha(0.15f);
                    waitUntilChatAppears.setVisibility(View.VISIBLE);
                    DocumentReference docRef = db.collection("blindDates").document(blindDateID);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null) {

                                    Map<String, Object> blindDate = new HashMap<>();

                                    String userNameNr0 = document.getString(USER_NAME_NR_0);
                                    String userNameNr1 = document.getString(USER_NAME_NR_1);
                                    String userNameNr2 = document.getString(USER_NAME_NR_2);
                                    String userNameNr3 = document.getString(USER_NAME_NR_3);

                                    String userIDNr0 = document.getString(USER_ID_NR_0);
                                    String userIDNr1 = document.getString(USER_ID_NR_1);
                                    String userIDNr2 = document.getString(USER_ID_NR_2);
                                    String userIDNr3 = document.getString(USER_ID_NR_3);

                                    boolean wasUserNr1Rejected = document.getBoolean(USER_NR_1_WAS_REJECTED);
                                    boolean wasUserNr2Rejected = document.getBoolean(USER_NR_2_WAS_REJECTED);
                                    boolean wasUserNr3Rejected = document.getBoolean(USER_NR_3_WAS_REJECTED);

                                    Date blindDateCreationDate = document.getDate(DATE_OF_CREATION_BLIND_DATE);
                                    Date dateOfStartRound2 = document.getDate(DATE_OF_ROUND_2_STARTED);

                                    int roundNumber = document.getDouble(ROUND_NO).intValue();

                                    Intent intent = new Intent(getActivity(), BlindDateActivity.class);
                                    adapter.stopListening();

                                    blindDate.put("blindDateID", blindDateID);
                                    blindDate.put("dateOfBlindDateCreation", blindDateCreationDate);
                                    blindDate.put("dateOfRound2Started", dateOfStartRound2);
                                    blindDate.put("userNameNr0", userNameNr0);
                                    blindDate.put("userNameNr1", userNameNr1);
                                    blindDate.put("userNameNr2", userNameNr2);
                                    blindDate.put("userNameNr3", userNameNr3);
                                    blindDate.put("userIDNr0", userIDNr0);
                                    blindDate.put("userIDNr1", userIDNr1);
                                    blindDate.put("userIDNr2", userIDNr2);
                                    blindDate.put("userIDNr3", userIDNr3);
                                    blindDate.put("wasUserRejectedNr1", wasUserNr1Rejected);
                                    blindDate.put("wasUserRejectedNr2", wasUserNr2Rejected);
                                    blindDate.put("wasUserRejectedNr3", wasUserNr3Rejected);
                                    blindDate.put("numberOfRoundInBlindDate",roundNumber);
                                    blindDate.put("fromNotification",String.valueOf(false));

//                                    intent.putExtra(USER_NAME_NR_0, userNameNr0);
//                                    intent.putExtra(USER_NAME_NR_1, userNameNr1);
//                                    intent.putExtra(USER_NAME_NR_2, userNameNr2);
//                                    intent.putExtra(USER_NAME_NR_3, userNameNr3);
//                                    intent.putExtra(USER_ID_NR_0, userIDNr0);
//                                    intent.putExtra(USER_ID_NR_1, userIDNr1);
//                                    intent.putExtra(USER_ID_NR_2, userIDNr2);
//                                    intent.putExtra(USER_ID_NR_3, userIDNr3);
//                                    intent.putExtra(USER_NR_1_WAS_REJECTED, wasUserNr1Rejected);
//                                    intent.putExtra(USER_NR_2_WAS_REJECTED, wasUserNr2Rejected);
//                                    intent.putExtra(USER_NR_3_WAS_REJECTED, wasUserNr3Rejected);
//                                    intent.putExtra(DATE_OF_CREATION_BLIND_DATE, blindDateCreationDate);
//                                    intent.putExtra(ROUND_NO, roundNumber);
//                                    intent.putExtra(BLIND_DATE_ID, blindDateID);
//                                    intent.putExtra(DATE_OF_ROUND_2_STARTED,dateOfStartRound2);

                                    intent.putExtra("blindDateMap", (Serializable) blindDate);
                                    startActivity(intent);
                                    layoutToDimWhenSearching.setAlpha(1f);
                                    waitUntilChatAppears.setVisibility(View.INVISIBLE);
                                    adapter.isClickable = true;
                                    Toast.makeText(getContext(), "Position: " + position + " BlindDateID: " + blindDateID, Toast.LENGTH_SHORT).show();

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

    public void setOnLongClickAdapterListener(BlindDatesAdapter adapter) {

        adapter.setOnLongItemClickListener(new BlindDatesAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(String blindDateID, String usersNames) {

                LayoutInflater inflaterDialog = LayoutInflater.from(getContext());
                View deleteBlindDateAlertView = inflaterDialog.inflate(R.layout.dialog_uncover, null);
                TextView deleteBlindDateText = deleteBlindDateAlertView.findViewById(R.id.uncover_text);
                deleteBlindDateText.setText("Are You sure You want to delete chat with " + usersNames);

                AlertDialog deleteDialog = new AlertDialog.Builder(getContext())
                        .setView(deleteBlindDateAlertView)  // What to use in dialog box
                        .setPositiveButton(R.string.yes_text_dialog_boxes, null)
                        .setNegativeButton(R.string.no_text_dialog_boxes, null)
                        .show();

                deleteDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Query checkIfSomeoneDeletedBlindDate = FirebaseFirestore.getInstance().collection("blindDates").document(blindDateID).collection("blindDateUsers").limit(5);

                        checkIfSomeoneDeletedBlindDate.get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().isEmpty()) {

                                                Log.d(TAG, "NO ONE HERE");

                                                db.collection("blindDates").document(blindDateID).delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "BlindDate completely and successfully deleted!");
                                                                Fragment fragment = new BlindDateFragment();
                                                                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                                                ft.detach(BlindDateFragment.this);
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

                                                int nrOfChatMembers = 0;
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    nrOfChatMembers++;
                                                }

                                                if (nrOfChatMembers == 1) {

                                                    db.collection("blindDates").document(blindDateID).delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d(TAG, "BlindDate completely and successfully deleted!");
                                                                    Fragment fragment = new BlindDateFragment();
                                                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                                                    ft.detach(BlindDateFragment.this);
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
                                                    db.collection("blindDates").document(blindDateID).collection("blindDateUsers").document(user.getUid()).delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d(TAG, "Single BlindDateMemberDocument successfully deleted!");
                                                                    Fragment fragment = new BlindDateFragment();
                                                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                                                    ft.detach(BlindDateFragment.this);
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

                                                    db.collection("blindDates").document(blindDateID).update("usersParticipatingThatHaveNotDeletedBlindDates", FieldValue.arrayRemove(user.getUid()));

                                                }
                                            }
                                            //db.collection("users").document(user.getUid()).update(NR_OF_USER_CHATS, FieldValue.increment(-1)); ---> still to think about nr of chats max
                                        }
                                    }
                                });



                    }
                });

                deleteDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Fragment fragment = new BlindDateFragment();
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.detach(BlindDateFragment.this);
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