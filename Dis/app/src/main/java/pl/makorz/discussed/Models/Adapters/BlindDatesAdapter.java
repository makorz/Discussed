package pl.makorz.discussed.Models.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.Date;
import java.util.List;

import pl.makorz.discussed.Models.BlindDate;
import pl.makorz.discussed.R;

public class BlindDatesAdapter extends FirestoreRecyclerAdapter<BlindDate, BlindDatesAdapter.ViewHolder> {

    private static final String TAG = "BlindDatesAdapter";

    private static OnItemClickListener listener;
    private static OnLongItemClickListener longListener;
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    public boolean isClickable = true; //Prevent from multiple clicks on recyclerview items, preventing from launching multiple ChatActivities


    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull BlindDate model) {

        String blindDateID = model.getBlindDateID();
        checkIfChatWasViewed(blindDateID, currentUser.getUid());
        boolean wasUserInActivity = false;
        int currentUserNumberInBlindDate;
        if (currentUser.getUid().equals(model.getUserIDNr0())){
            currentUserNumberInBlindDate = 0;
            wasUserInActivity = model.getWasUserInActivityNr0();
          //  holder.hostName.setTextColor(Color.parseColor("#99aa00"));
        } else if (currentUser.getUid().equals(model.getUserIDNr1())) {
            currentUserNumberInBlindDate = 1;
            wasUserInActivity = model.getWasUserInActivityNr1();
          //  holder.userNameNr1.setTextColor(Color.parseColor("#99aa00"));
        } else if (currentUser.getUid().equals(model.getUserIDNr2())) {
            currentUserNumberInBlindDate = 2;
            wasUserInActivity = model.getWasUserInActivityNr2();
         //  holder.userNameNr2.setTextColor(Color.parseColor("#99aa00"));
        } else {
            currentUserNumberInBlindDate = 3;
            wasUserInActivity = model.getWasUserInActivityNr3();
          //  holder.userNameNr3.setTextColor(Color.parseColor("#99aa00"));
        }

        holder.blindDatesIDtext.setText(model.getBlindDateID());

        int roundNo = model.getNumberOfRoundInBlindDate();
        switch (roundNo) {
            case 2:
            case 3:
                holder.imageOfRound.setImageResource(R.drawable.blind_date_logo_round2);
                break;
            case 4:
                holder.imageOfRound.setImageResource(R.drawable.blind_date_logo_ended);
                break;
            default:
                holder.imageOfRound.setImageResource(R.drawable.blind_date_logo_round1);
                break;
        }

        String userNameNr1text = model.getUserNameNr1();
        String userNameNr2text = model.getUserNameNr2();
        String userNameNr3text = model.getUserNameNr3();

        if (wasUserInActivity) {
            holder.userNameNr1.setTypeface(Typeface.DEFAULT);
            holder.userNameNr2.setTypeface(Typeface.DEFAULT);
            holder.userNameNr3.setTypeface(Typeface.DEFAULT);
            holder.hostName.setTypeface(Typeface.DEFAULT);
            holder.hostName.setText(model.getUserNameNr0());

            holder.userNameNr1.setText(userNameNr1text);
            if (model.getWasUserRejectedNr1()) {
                holder.userNameNr1.setPaintFlags( holder.userNameNr1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            holder.userNameNr2.setText(userNameNr2text);
            if (model.getWasUserRejectedNr2()) {
                holder.userNameNr2.setPaintFlags( holder.userNameNr2.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            holder.userNameNr3.setText(userNameNr3text);
            if (model.getWasUserRejectedNr3()) {
                holder.userNameNr3.setPaintFlags( holder.userNameNr3.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

        } else {
            holder.userNameNr1.setTypeface(Typeface.DEFAULT_BOLD);
            holder.userNameNr2.setTypeface(Typeface.DEFAULT_BOLD);
            holder.userNameNr3.setTypeface(Typeface.DEFAULT_BOLD);
            holder.hostName.setTypeface(Typeface.DEFAULT_BOLD);
            holder.hostName.setText(model.getUserNameNr0());
            holder.userNameNr1.setText(userNameNr1text);
            if (model.getWasUserRejectedNr1()) {
                holder.userNameNr1.setPaintFlags( holder.userNameNr1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            holder.userNameNr2.setText(userNameNr2text);
            if (model.getWasUserRejectedNr2()) {
                holder.userNameNr2.setPaintFlags( holder.userNameNr2.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            holder.userNameNr3.setText(userNameNr3text);
            if (model.getWasUserRejectedNr3()) {
                holder.userNameNr3.setPaintFlags( holder.userNameNr3.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }

    }

    public BlindDatesAdapter(@NonNull FirestoreRecyclerOptions<BlindDate> options) {
        super(options);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_blind_dates_view, parent, false);
        ViewHolder vh = new ViewHolder(view);
        Context context = parent.getContext();
        return vh;

    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView userNameNr1, userNameNr2, userNameNr3, blindDatesIDtext, hostName;
        ImageView imageOfRound;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameNr1 = itemView.findViewById(R.id.user1_name_blind_dates_text);
            userNameNr2 = itemView.findViewById(R.id.user2_name_blind_dates_text);
            userNameNr3 = itemView.findViewById(R.id.user3_name_blind_dates_text);
            hostName = itemView.findViewById(R.id.host_name_blind_dates_text);
            imageOfRound = itemView.findViewById(R.id.info_image_blindDates);
            blindDatesIDtext = itemView.findViewById(R.id.blindDatesID_text);
            cardView = itemView.findViewById(R.id.card_view_blindDates);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        String blindDatesIDofPosition = blindDatesIDtext.getText().toString();
                        listener.onItemClick(blindDatesIDofPosition, position);
                    }

                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    cardView.setCardBackgroundColor(Color.RED);
                    imageOfRound.setImageResource(R.drawable.delete_icon);
                    String userNamesToDialog = hostName.getText().toString() + ", " + userNameNr1.getText().toString() + ", " + userNameNr2.getText().toString()
                            + ", " + userNameNr3.getText().toString();
                    userNameNr1.setText("");
                    userNameNr2.setText("");
                    userNameNr3.setText("");
                    hostName.setText("");
                    String blindDatesIDofPosition = blindDatesIDtext.getText().toString();
                    longListener.onLongItemClick(blindDatesIDofPosition, userNamesToDialog);
                    return true;

                }
            });

        }
    }

    // This methods below allow to send data from adapter to activity
    public interface OnItemClickListener {
        void onItemClick(String blindDateID, int position);
    }

    // This methods below allow to send data from adapter to activity
    public interface OnLongItemClickListener {
        void onLongItemClick(String blindDateID, String usersNames);
    }

    public void setOnLongItemClickListener(OnLongItemClickListener longListener) {
        BlindDatesAdapter.longListener = longListener;
    }

    public void setOnItemCLickListener(OnItemClickListener listener) {
        BlindDatesAdapter.listener = listener;
    }

    public void checkIfChatWasViewed(final String blindDatesID, final String userID) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("blindDates").document(blindDatesID);

        // Check if user Viewed a Message
        Query queryUsers = FirebaseFirestore.getInstance().collection("blindDates").document(blindDatesID)
                .collection("blindDateUsers").whereEqualTo("userID", userID);

        queryUsers.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Date lastTimeInChatActivity = document.getDate("lastTimeInChatActivity");
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentOfBlindDate = task.getResult();
                                    if (documentOfBlindDate != null) {
                                        Date lastMessageDate = documentOfBlindDate.getDate("lastMessageDate");
                                        Log.d(TAG, lastMessageDate.toString() + lastTimeInChatActivity.toString());
                                        if (lastTimeInChatActivity.after(lastMessageDate)) {
                                            if (userID.equals(documentOfBlindDate.getString("userIDNr1"))){
                                                db.collection("blindDates").document(blindDatesID)
                                                        .update("wasUserInActivityNr1", true);
                                            } else if (userID.equals(documentOfBlindDate.getString("userIDNr2"))) {
                                                db.collection("blindDates").document(blindDatesID)
                                                        .update("wasUserInActivityNr2", true);
                                            } else if (userID.equals(documentOfBlindDate.getString("userIDNr3"))) {
                                                db.collection("blindDates").document(blindDatesID)
                                                        .update("wasUserInActivityNr3", true);
                                            } else {
                                                db.collection("blindDates").document(blindDatesID)
                                                        .update("wasUserInActivityNr0", true);
                                            }
                                        } else {
                                            if (userID.equals(documentOfBlindDate.getString("userIDNr1"))){
                                                db.collection("blindDates").document(blindDatesID)
                                                        .update("wasUserInActivityNr1", false);
                                            } else if (userID.equals(documentOfBlindDate.getString("userIDNr2"))) {
                                                db.collection("blindDates").document(blindDatesID)
                                                        .update("wasUserInActivityNr2", false);
                                            } else if (userID.equals(documentOfBlindDate.getString("userIDNr3"))) {
                                                db.collection("blindDates").document(blindDatesID)
                                                        .update("wasUserInActivityNr3", false);
                                            } else {
                                                db.collection("blindDates").document(blindDatesID)
                                                        .update("wasUserInActivityNr0", false);
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


                    }
                }
            }
        });
    }

}
