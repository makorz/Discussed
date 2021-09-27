package pl.makorz.discussed.Controllers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import pl.makorz.discussed.Controllers.Functions.APIService;
import pl.makorz.discussed.Controllers.Functions.APIServiceB;
import pl.makorz.discussed.Controllers.Notifications.Client;
import pl.makorz.discussed.Controllers.Notifications.Data;
import pl.makorz.discussed.Controllers.Notifications.DataB;
import pl.makorz.discussed.Controllers.Notifications.Response;
import pl.makorz.discussed.Controllers.Notifications.Sender;
import pl.makorz.discussed.Controllers.Notifications.SenderB;
import pl.makorz.discussed.Models.Adapters.MessageInBlindDateAdapter;
import pl.makorz.discussed.Models.MessageInBlindDate;
import pl.makorz.discussed.R;
import retrofit2.Call;
import retrofit2.Callback;

public class BlindDateActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "BlindDateActivity";

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
    public static final String DATE_OF_CREATION_BLIND_DATE = "dateOfBlindDateCreation";
    public static final String ROUND_NO = "numberOfRoundInBlindDate";
    public static final String BLIND_DATE_ID = "blindDateID";
    public static final String DATE_OF_ROUND_2_STARTED = "dateOfRound2Started";

    private ImageView firstImageView, secondImageView, thirdImageView;
    private Button buttonKickOffDateNr1, buttonKickOffDateNr2, buttonKickOffDateNr3;
    private ImageButton buttonSendMessage;
    private EditText messageText;
    private TextView dateNr1Name, dateNr2Name, dateNr3Name, timer, infoText, messageAlertView;
    private RecyclerView messagesRecycler;
    private MessageInBlindDateAdapter messagesAdapter;
    private AlertDialog dialog, kickOutDialog, lastQuestionDialog;
    private LinearLayout wholeLayout, linear_space_dates1, linear_dates_buttons;
    private CountDownTimer timerCount;

    private int roundNumber, currentUserNr;
    private String blindDateID, userNameNr0, userNameNr1, userNameNr2, userNameNr3, userIDNr0, userIDNr1, userIDNr2, userIDNr3, currentUserName;
    private boolean wasUserNr1Rejected, wasUserNr2Rejected, wasUserNr3Rejected;
    private Date blindDateCreationDate, dateOfStartRound2;
    private List<String> activeChatUsersID = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private APIServiceB apiServiceB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blind_date);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //no dark theme

        loadingAlertDialog();
        messageAlertView.setText("Loading blind date...");
        wholeLayout = findViewById(R.id.linearBlindDateWholeLayout);
        wholeLayout.setAlpha(0.0f);

        apiServiceB = Client.getClient("https://fcm.googleapis.com/").create(APIServiceB.class);

        Map intentMap = new HashMap<>();
        intentMap = (Map) getIntent().getSerializableExtra("blindDateMap");
        blindDateID = (String) intentMap.get(BLIND_DATE_ID);
        String fromNotification = (String) intentMap.get("fromNotification");

        if (fromNotification.equals("false")) {

            userNameNr0 = (String) intentMap.get(USER_NAME_NR_0);
            userNameNr1 = (String) intentMap.get(USER_NAME_NR_1);
            userNameNr2 = (String) intentMap.get(USER_NAME_NR_2);
            userNameNr3 = (String) intentMap.get(USER_NAME_NR_3);
            userIDNr0 = (String) intentMap.get(USER_ID_NR_0);
            userIDNr1 = (String) intentMap.get(USER_ID_NR_1);
            userIDNr2 = (String) intentMap.get(USER_ID_NR_2);
            userIDNr3 = (String) intentMap.get(USER_ID_NR_3);
            blindDateCreationDate = (Date) intentMap.get(DATE_OF_CREATION_BLIND_DATE);
            dateOfStartRound2 = (Date) intentMap.get(DATE_OF_ROUND_2_STARTED);
            wasUserNr1Rejected = (Boolean) intentMap.get(USER_NR_1_WAS_REJECTED);
            wasUserNr2Rejected = (Boolean) intentMap.get(USER_NR_2_WAS_REJECTED);
            wasUserNr3Rejected = (Boolean) intentMap.get(USER_NR_3_WAS_REJECTED);
            roundNumber = (int) intentMap.get(ROUND_NO);

        }

        linear_dates_buttons = findViewById(R.id.linear_dates_buttons);
        linear_space_dates1 = findViewById(R.id.linear_space_dates1);
        dateNr1Name = findViewById(R.id.textview_date_nr_1);
        dateNr2Name = findViewById(R.id.textview_date_nr_2);
        dateNr3Name = findViewById(R.id.textview_date_nr_3);
        infoText = findViewById(R.id.textview_round_info);
        timer = findViewById(R.id.textview_timer);
        messageText = findViewById(R.id.messageBlindEditText);
        buttonSendMessage = findViewById(R.id.sendMessageBlindDateButton);
        firstImageView = findViewById(R.id.imageview_date_nr_1);
        secondImageView = findViewById(R.id.imageview_date_nr_2);
        thirdImageView = findViewById(R.id.imageview_date_nr_3);
        buttonKickOffDateNr1 = findViewById(R.id.button_first_date_delete);
        buttonKickOffDateNr2 = findViewById(R.id.button_second_date_delete);
        buttonKickOffDateNr3 = findViewById(R.id.button_third_date_delete);

        buttonSendMessage.setOnClickListener(this);
        buttonKickOffDateNr1.setOnClickListener(this);
        buttonKickOffDateNr2.setOnClickListener(this);
        buttonKickOffDateNr3.setOnClickListener(this);

        try {
            updateBlindDateStatus();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        db.collection("blindDates").document(blindDateID).collection("blindDateUsers").document(user.getUid())
                .update("lastTimeInChatActivity", new Date());

        // RecyclerView initialize
        messagesRecycler = findViewById(R.id.messagesBlindDateRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecycler.setLayoutManager(layoutManager);

        //Ask Firebase what You want
        Query queryMessages = FirebaseFirestore.getInstance().collection("blindDates").document(blindDateID).collection("messages")
                .orderBy("dateOfMessage", Query.Direction.ASCENDING);
        //Configuring adapter to populate recyclerview
        FirestoreRecyclerOptions<MessageInBlindDate> options = new FirestoreRecyclerOptions.Builder<MessageInBlindDate>()
                .setQuery(queryMessages, MessageInBlindDate.class)
                .setLifecycleOwner(this)
                .build();

        //Populating recyclerview
        messagesAdapter = new MessageInBlindDateAdapter(options);
        messagesAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                messagesRecycler.scrollToPosition(messagesAdapter.getItemCount() - 1);
            }
        });
        messagesRecycler.setAdapter(messagesAdapter);

    }

    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    public void myCountDownTimer(long dateDifference, long countDownInterval, int roundNo) {

        Log.d(TAG, "COUNTDOWN" + roundNo);

        timerCount = new CountDownTimer(dateDifference, countDownInterval) {

            @Override
            public void onTick(long l) {
                if (roundNo == 1) {
                    db.collection("blindDates").document(blindDateID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null) {
                                    Log.d(TAG, "DBCHECK" + roundNo);
                                    roundNumber = document.getDouble(ROUND_NO).intValue();
                                    dateOfStartRound2 = document.getDate(DATE_OF_ROUND_2_STARTED);
                                    wasUserNr1Rejected = document.getBoolean(USER_NR_1_WAS_REJECTED);
                                    wasUserNr2Rejected = document.getBoolean(USER_NR_2_WAS_REJECTED);
                                    wasUserNr3Rejected = document.getBoolean(USER_NR_3_WAS_REJECTED);
                                    if (roundNumber == 2) {
                                        timerCount.cancel();
                                        initView(2);
                                    }
                                } else {
                                    Log.d("LOGGER", "No such document");
                                }
                            } else {
                                Log.d("LOGGER", "get failed with ", task.getException());
                            }

                        }
                    });
                } else if (roundNo == 3) {
                    db.collection("blindDates").document(blindDateID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null) {
                                    roundNumber = document.getDouble(ROUND_NO).intValue();
                                    wasUserNr1Rejected = document.getBoolean(USER_NR_1_WAS_REJECTED);
                                    wasUserNr2Rejected = document.getBoolean(USER_NR_2_WAS_REJECTED);
                                    wasUserNr3Rejected = document.getBoolean(USER_NR_3_WAS_REJECTED);
                                    if (roundNumber == 4) {
                                        timerCount.cancel();
                                        initView(4);
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
                    String time = (l / (1000 * 60)) + ":" + checkDigit((int) ((l % (1000 * 60) / 1000)));
                    timer.setText(time);
                }
            }

            @Override
            public void onFinish() {

                timer.setText("Round is over!");
//                db.collection("blindDates").document(blindDateID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            DocumentSnapshot document = task.getResult();
//                            if (document != null) {
//                                roundNumber = document.getDouble(ROUND_NO).intValue();
//                                wasUserNr1Rejected = document.getBoolean(USER_NR_1_WAS_REJECTED);
//                                wasUserNr2Rejected = document.getBoolean(USER_NR_2_WAS_REJECTED);
//                                wasUserNr3Rejected = document.getBoolean(USER_NR_3_WAS_REJECTED);
//
//
//                                Log.d(TAG, "HOW OFTEN HERE I AM INSIDE " + roundNo);
//
//                             } else {
//                                Log.d("LOGGER", "No such document");
//                            }
//                        } else {
//                            Log.d("LOGGER", "get failed with ", task.getException());
//                        }
//
//                    }
//                });
                roundNumber = roundNumber + 1;
                Log.d(TAG, "HOW OFTEN HERE I AM ON FINISH" + roundNo);
                initView(roundNumber);
            }
        }.start();

    }

    // This function initialises buttons, text views, etc.
    public void initView(int round) {

        Log.d(TAG, "INIT : " + round);

        dateNr1Name.setText(userNameNr1);
        dateNr2Name.setText(userNameNr2);
        dateNr3Name.setText(userNameNr3);
        Date currentDate = new Date(new Date().getTime());
        Date timeRound1 = new Date(blindDateCreationDate.getTime() + 30000); //1800000
        Date timeRound2 = new Date(dateOfStartRound2.getTime() + 30000); //1200000
        long dateDiffSinceRound1Start = timeRound1.getTime() - currentDate.getTime();
        long dateDiffSinceRound2Start = timeRound2.getTime() - currentDate.getTime();

        Log.d(TAG, timeRound1.getTime() + "    " + timeRound2.getTime());

        if (user.getUid().equals(userIDNr0)) {
            currentUserName = userNameNr0;
            currentUserNr = 0;
            linear_dates_buttons.setVisibility(View.VISIBLE);
            linear_space_dates1.setVisibility(View.VISIBLE);
        } else if (user.getUid().equals(userIDNr1)) {
            currentUserName = userNameNr1;
            currentUserNr = 1;
            linear_dates_buttons.setVisibility(View.GONE);
            linear_space_dates1.setVisibility(View.GONE);
        } else if (user.getUid().equals(userIDNr2)) {
            currentUserName = userNameNr2;
            currentUserNr = 2;
            linear_dates_buttons.setVisibility(View.GONE);
            linear_space_dates1.setVisibility(View.GONE);
        } else if (user.getUid().equals(userIDNr3)) {
            currentUserName = userNameNr3;
            currentUserNr = 3;
            linear_dates_buttons.setVisibility(View.GONE);
            linear_space_dates1.setVisibility(View.GONE);
        }

        if (wasUserNr1Rejected) {
            firstImageView.setImageResource(R.drawable.date_nr_1_deleted);
        } else if (wasUserNr2Rejected && wasUserNr3Rejected) {
            firstImageView.setImageResource(R.drawable.date_nr_1_win);
        }

        if (wasUserNr2Rejected) {
            secondImageView.setImageResource(R.drawable.date_nr_2_deleted);
        } else if (wasUserNr1Rejected && wasUserNr3Rejected) {
            secondImageView.setImageResource(R.drawable.date_nr_2_win);
        }

        if (wasUserNr3Rejected) {
            thirdImageView.setImageResource(R.drawable.date_nr_3_deleted);
        } else if (wasUserNr1Rejected && wasUserNr2Rejected) {
            thirdImageView.setImageResource(R.drawable.date_nr_3_win);
        }

        switch (round) {
            case 1:
                if (user.getUid().equals(userIDNr0)) {
                    infoText.setText("Choose who will go out!");
                    messageText.setVisibility(View.GONE);
                    buttonSendMessage.setVisibility(View.GONE);
                    timer.setVisibility(View.GONE);
                    buttonKickOffDateNr1.setEnabled(true);
                    buttonKickOffDateNr2.setEnabled(true);
                    buttonKickOffDateNr3.setEnabled(true);
                } else {
                    infoText.setText("Wait for kick out!");
                    messageText.setVisibility(View.GONE);
                    buttonSendMessage.setVisibility(View.GONE);
                    timer.setVisibility(View.GONE);
                    myCountDownTimer(1800000, 2500, round);
                }
                break;
            case 2:
                if (user.getUid().equals(userIDNr0)) {
                    infoText.setText("Let's go and join discussion with chosen blind dates!");
                    messageText.setVisibility(View.VISIBLE);
                    buttonSendMessage.setVisibility(View.VISIBLE);
                    buttonKickOffDateNr1.setEnabled(false);
                    buttonKickOffDateNr2.setEnabled(false);
                    buttonKickOffDateNr3.setEnabled(false);
                    timer.setVisibility(View.VISIBLE);
                    myCountDownTimer(dateDiffSinceRound2Start, 1000, round);
                } else {
                    infoText.setText("Discuss last question with " + userNameNr0 + "!");
                    messageText.setVisibility(View.VISIBLE);
                    buttonSendMessage.setVisibility(View.VISIBLE);
                    timer.setVisibility(View.VISIBLE);
                    if (wasUserNr1Rejected && userIDNr1.equals(user.getUid())) {
                        messageText.setVisibility(View.GONE);
                        buttonSendMessage.setVisibility(View.GONE);
                    } else if (wasUserNr2Rejected && userIDNr2.equals(user.getUid())) {
                        messageText.setVisibility(View.GONE);
                        buttonSendMessage.setVisibility(View.GONE);
                    } else if (wasUserNr3Rejected && userIDNr3.equals(user.getUid())) {
                        messageText.setVisibility(View.GONE);
                        buttonSendMessage.setVisibility(View.GONE);
                    }
                    myCountDownTimer(dateDiffSinceRound2Start, 1000, round);
                }
                break;
            case 3:
                if (user.getUid().equals(userIDNr0)) {
                    infoText.setText("Choose who will go out!");
                    messageText.setVisibility(View.GONE);
                    buttonSendMessage.setVisibility(View.GONE);
                    if (wasUserNr1Rejected) {
                        buttonKickOffDateNr1.setEnabled(false);
                        buttonKickOffDateNr2.setEnabled(true);
                        buttonKickOffDateNr3.setEnabled(true);
                    } else if (wasUserNr2Rejected) {
                        buttonKickOffDateNr1.setEnabled(true);
                        buttonKickOffDateNr2.setEnabled(false);
                        buttonKickOffDateNr3.setEnabled(true);
                    } else if (wasUserNr3Rejected) {
                        buttonKickOffDateNr1.setEnabled(true);
                        buttonKickOffDateNr2.setEnabled(true);
                        buttonKickOffDateNr3.setEnabled(false);
                    }
                    timer.setVisibility(View.GONE);
                } else {
                    infoText.setText("Wait for final kick out!");
                    messageText.setVisibility(View.GONE);
                    buttonSendMessage.setVisibility(View.GONE);
                    timer.setVisibility(View.GONE);
                    myCountDownTimer(1800000, 2500, round);
                }
                break;
            case 4:
                if (user.getUid().equals(userIDNr0)) {
                    infoText.setText("You've chosen Your Date, private chat have been initialised. All the best for both of You!");
                    messageText.setVisibility(View.GONE);
                    buttonSendMessage.setVisibility(View.GONE);
                    linear_dates_buttons.setVisibility(View.GONE);
                    linear_space_dates1.setVisibility(View.GONE);
                    timer.setVisibility(View.GONE);
                } else {
                    infoText.setText("Final decision have been made! Private chat have been initialised. All the best for both of You!");
                    messageText.setVisibility(View.GONE);
                    buttonSendMessage.setVisibility(View.GONE);
                    timer.setVisibility(View.GONE);
                }
                break;
            default:
                if (user.getUid().equals(userIDNr0)) {
                    infoText.setText("Sit and watch what They have to say about Your questions!");
                    messageText.setVisibility(View.GONE);
                    buttonSendMessage.setVisibility(View.GONE);
                    buttonKickOffDateNr1.setEnabled(false);
                    buttonKickOffDateNr2.setEnabled(false);
                    buttonKickOffDateNr3.setEnabled(false);
                    timer.setVisibility(View.VISIBLE);
                    myCountDownTimer(dateDiffSinceRound1Start, 1000, round);
                } else {
                    infoText.setText("Discuss questions made by " + userNameNr0 + "!");
                    messageText.setVisibility(View.VISIBLE);
                    buttonSendMessage.setVisibility(View.VISIBLE);
                    timer.setVisibility(View.VISIBLE);
                    myCountDownTimer(dateDiffSinceRound1Start, 1000, round);
                }
                break;
        }

    }

    // This function starts functions correlated with certain button
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.button_first_date_delete:
                kickOutAlertDialog(1);
                break;
            case R.id.button_second_date_delete:
                kickOutAlertDialog(2);
                break;
            case R.id.button_third_date_delete:
                kickOutAlertDialog(3);
                break;
            case R.id.sendMessageBlindDateButton:
                sendButtonAction();
                break;
            default:
                break;
        }
    }

    public void kickOutAlertDialog(int whatUser) {

        LayoutInflater inflater = getLayoutInflater();
        View kickOutView = inflater.inflate(R.layout.dialog_uncover, null);
        TextView kickOutTextView = kickOutView.findViewById(R.id.uncover_text);

        if (whatUser == 1) {
            kickOutTextView.setText("Are You sure You want to kick out " + userNameNr1 + " (nr 1)?");
        } else if (whatUser == 2) {
            kickOutTextView.setText("Are You sure You want to kick out " + userNameNr2 + " (nr 2)?");
        } else {
            kickOutTextView.setText("Are You sure You want to kick out " + userNameNr3 + " (nr 3)?");
        }

        kickOutDialog = new AlertDialog.Builder(this)
                .setView(kickOutView)  // What to use in dialog box
                .setNegativeButton(R.string.no_text_dialog_boxes, null)
                .setPositiveButton(R.string.yes_text_dialog_boxes, null)
                .show();

        kickOutDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonKickOffDateNr1.setEnabled(false);
                buttonKickOffDateNr2.setEnabled(false);
                buttonKickOffDateNr3.setEnabled(false);

                if (roundNumber == 1) {

                    lastQuestionAlertDialog(whatUser);
                } else if (roundNumber == 3) {

                    String messageNotificationReject = "Host has made a decision. Sorry, You were not chosen.";
                    db.collection("blindDates").document(blindDateID).update(ROUND_NO, 4);
                    if (whatUser == 1) {
                        db.collection("blindDates").document(blindDateID).update(USER_NR_1_WAS_REJECTED, true);
                        wasUserNr1Rejected = true;
                        sendNotification(userIDNr1, user.getUid(), userIDNr1, messageNotificationReject);
                    } else if (whatUser == 2) {
                        db.collection("blindDates").document(blindDateID).update(USER_NR_2_WAS_REJECTED, true);
                        wasUserNr2Rejected = true;
                        sendNotification(userIDNr2, user.getUid(), userIDNr2, messageNotificationReject);
                    } else {
                        db.collection("blindDates").document(blindDateID).update(USER_NR_3_WAS_REJECTED, true);
                        wasUserNr3Rejected = true;
                        sendNotification(userIDNr3, user.getUid(), userIDNr3, messageNotificationReject);
                    }

                    String messageNotificationWin = " has chosen You. Congratulations!";
                    if (wasUserNr1Rejected && wasUserNr2Rejected) {
                        createChat(userIDNr3, userNameNr3);
                        sendNotification(userIDNr3, user.getUid(), userIDNr3, userNameNr0 + messageNotificationWin);
                    } else if (wasUserNr2Rejected && wasUserNr3Rejected) {
                        createChat(userIDNr1, userNameNr1);
                        sendNotification(userIDNr1, user.getUid(), userIDNr1, userNameNr0 + messageNotificationWin);
                    } else if (wasUserNr1Rejected && wasUserNr3Rejected) {
                        createChat(userIDNr2, userNameNr2);
                        sendNotification(userIDNr2, user.getUid(), userIDNr2, userNameNr0 + messageNotificationWin);
                    }

                    roundNumber = roundNumber + 1;
                    Log.d(TAG, "HALLLLOOOOOOO" + roundNumber);
                    initView(roundNumber);
                }

                kickOutDialog.dismiss();

            }
        });

        kickOutDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonKickOffDateNr1.setEnabled(true);
                buttonKickOffDateNr2.setEnabled(true);
                buttonKickOffDateNr3.setEnabled(true);
                kickOutDialog.dismiss();

            }
        });

    }

    public void lastQuestionAlertDialog(int whatUser) {

        kickOutDialog.dismiss();
        LayoutInflater inflater = getLayoutInflater();
        View lastQuestionView = inflater.inflate(R.layout.dialog_blind_date_last_question, null);
        EditText lastEditText = lastQuestionView.findViewById(R.id.editText_questionLast_blind_date);

        InputFilter lengthFilter = new InputFilter.LengthFilter(500);
        lastEditText.setHint("Last question (20-500 letters)");
        lastEditText.setFilters(new InputFilter[]{lengthFilter});

        lastQuestionDialog = new AlertDialog.Builder(this)
                .setView(lastQuestionView)  // What to use in dialog box
                .setNegativeButton(R.string.no_text_dialog_boxes, null)
                .setPositiveButton(R.string.yes_text_dialog_boxes, null)
                .setTitle("Prepare last question:")
                .show();

        lastQuestionDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (lastEditText.getText().toString().length() >= 1) {

                    db.collection("blindDates").document(blindDateID).update(ROUND_NO, 2);
                    db.collection("blindDates").document(blindDateID).update(DATE_OF_ROUND_2_STARTED, new Date());
                    dateOfStartRound2 = new Date();

                    if (whatUser == 1) {
                        db.collection("blindDates").document(blindDateID).update(USER_NR_1_WAS_REJECTED, true);
                        wasUserNr1Rejected = true;
                    } else if (whatUser == 2) {
                        db.collection("blindDates").document(blindDateID).update(USER_NR_2_WAS_REJECTED, true);
                        wasUserNr2Rejected = true;
                    } else {
                        db.collection("blindDates").document(blindDateID).update(USER_NR_3_WAS_REJECTED, true);
                        wasUserNr3Rejected = true;
                    }
                    roundNumber = roundNumber + 1;

                    MessageInBlindDate messageBlindDate = new MessageInBlindDate("4. " + lastEditText.getText().toString(), "HOST", currentUserName, new Date(), roundNumber, 10);
                    db.collection("blindDates").document(blindDateID).collection("messages").add(messageBlindDate).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            String messageID = documentReference.getId();
                            Map<String, Object> message = new HashMap<>();
                            message.put("messageID", messageID);
                            db.collection("blindDates").document(blindDateID).collection("messages").document(messageID).update(message);

                            String messageNotification = "Round 2 has started! Host has made a decision";
                            if (whatUser == 1) {
                                sendNotification(userIDNr1, user.getUid(), userIDNr1, messageNotification);
                                sendNotification(userIDNr2, user.getUid(), userIDNr2, messageNotification);
                                sendNotification(userIDNr3, user.getUid(), userIDNr3, messageNotification);
                            } else if (whatUser == 2) {
                                sendNotification(userIDNr1, user.getUid(), userIDNr1, messageNotification);
                                sendNotification(userIDNr2, user.getUid(), userIDNr2, messageNotification);
                                sendNotification(userIDNr3, user.getUid(), userIDNr3, messageNotification);
                            } else {
                                sendNotification(userIDNr1, user.getUid(), userIDNr1, messageNotification);
                                sendNotification(userIDNr2, user.getUid(), userIDNr2, messageNotification);
                                sendNotification(userIDNr3, user.getUid(), userIDNr3, messageNotification);
                            }

                            initView(roundNumber);
                            lastQuestionDialog.dismiss();

                        }
                    });

                } else {
                    Toast.makeText(BlindDateActivity.this, "Type something!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        lastQuestionDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonKickOffDateNr1.setEnabled(true);
                buttonKickOffDateNr2.setEnabled(true);
                buttonKickOffDateNr3.setEnabled(true);
                lastQuestionDialog.dismiss();
            }
        });
    }

    // This function updates profile information when activity is started
    private void updateBlindDateStatus() throws ExecutionException, InterruptedException {
        // Download document of current user, to retrieve actual info to profile view
        DocumentReference docRef = db.collection("blindDates").document(blindDateID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {

                        userIDNr0 = document.getString(USER_ID_NR_0);
                        userIDNr1 = document.getString(USER_ID_NR_1);
                        userIDNr2 = document.getString(USER_ID_NR_2);
                        userIDNr3 = document.getString(USER_ID_NR_3);
                        userNameNr0 = document.getString(USER_NAME_NR_0);
                        userNameNr1 = document.getString(USER_NAME_NR_1);
                        userNameNr2 = document.getString(USER_NAME_NR_2);
                        userNameNr3 = document.getString(USER_NAME_NR_3);
                        roundNumber = document.getDouble(ROUND_NO).intValue();
                        wasUserNr1Rejected = document.getBoolean(USER_NR_1_WAS_REJECTED);
                        wasUserNr2Rejected = document.getBoolean(USER_NR_2_WAS_REJECTED);
                        wasUserNr3Rejected = document.getBoolean(USER_NR_3_WAS_REJECTED);
                        blindDateCreationDate = document.getDate(DATE_OF_CREATION_BLIND_DATE);
                        dateOfStartRound2 = document.getDate(DATE_OF_ROUND_2_STARTED);

                        int whatToDo = 0;
                        if (wasUserNr1Rejected) {
                            whatToDo++;
                        } else if (wasUserNr2Rejected) {
                            whatToDo++;
                        } else if (wasUserNr3Rejected) {
                            whatToDo++;
                        }

                        Date currentDate = new Date(new Date().getTime());
                        Date timeRound1 = new Date(blindDateCreationDate.getTime() + 30000); //1800000
                        Date timeRound2 = new Date(dateOfStartRound2.getTime() + 30000); //1200000
                        long dateDiffSinceRound1Start = timeRound1.getTime() - currentDate.getTime();
                        long dateDiffSinceRound2Start = timeRound2.getTime() - currentDate.getTime();

                        if (dateDiffSinceRound1Start <= 0 && roundNumber == 0) {
                            db.collection("blindDates").document(blindDateID).update(ROUND_NO, 1);
                            wholeLayout.setAlpha(1.0f);
                            dialog.dismiss();
                            initView(1);
                        } else if (dateDiffSinceRound2Start <= 0 && roundNumber == 2){
                            db.collection("blindDates").document(blindDateID).update(ROUND_NO, 3);
                            wholeLayout.setAlpha(1.0f);
                            dialog.dismiss();
                            initView(3);
                        } else {
                            wholeLayout.setAlpha(1.0f);
                            dialog.dismiss();
                            initView(roundNumber);
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


    private void sendNotification(String otherUserID, final String userID, final String otherUserName, final String body) {
        DocumentReference docRefUser = db.collection("users").document(otherUserID);
        docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentOfUser = task.getResult();
                    if (documentOfUser != null) {

                        String token = documentOfUser.getString("fcmRegistrationToken");
                        DataB data = new DataB(otherUserID, R.drawable.notification_icon_white, body,
                                "Blind date with: " + userNameNr0, userID, otherUserName, blindDateID,1);
                        SenderB sender = new SenderB(data, token);

                        apiServiceB.sendNotification(sender).enqueue(new Callback<Response>() {
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

    @Override
    protected void onStart() {
        super.onStart();
        messagesAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        messagesAdapter.stopListening();
        db.collection("chats").document(blindDateID).collection("blindDateUsers").document(user.getUid())
                .update("lastTimeInChatActivity", new Date());
    }

    @Override
    public void onBackPressed() {
        //If Chat starts from notification and it's root start MainActivity instead of completely closing app
        if (isTaskRoot()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            super.onBackPressed();
            finish();
        } else {
            super.onBackPressed();
            finish();
        }
    }

    public void sendButtonAction() {

        InputFilter lengthFilter = new InputFilter.LengthFilter(1250);
        messageText.setFilters(new InputFilter[]{lengthFilter});
        if (messageText.getText().toString().length() <= 1250) {
            if (messageText.getText().toString().length() >= 1) {

                MessageInBlindDate messageBlindDate = new MessageInBlindDate(messageText.getText().toString(), user.getUid(), currentUserName, new Date(), roundNumber, currentUserNr);
                db.collection("blindDates").document(blindDateID).collection("messages").add(messageBlindDate).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String messageID = documentReference.getId();
                        Map<String, Object> message = new HashMap<>();
                        message.put("messageID", messageID);
                        db.collection("blindDates").document(blindDateID).collection("messages").document(messageID).update(message);
                    }
                });
                //sendNotification(idOfOtherUser, user.getUid(), messageText.getText().toString());
                messageText.setText("");
            } else
                Toast.makeText(BlindDateActivity.this, "Type something!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(BlindDateActivity.this, R.string.too_long_message_chat_activity_toast, Toast.LENGTH_SHORT).show();
        }

    }

    // This function shows AlertDialog
    public void loadingAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false); // if you want user to wait for some process to finish
        LayoutInflater inflater = getLayoutInflater();
        View dialogAlertView = inflater.inflate(R.layout.progress_bar, null);
        messageAlertView = dialogAlertView.findViewById(R.id.loading_msg);
        builder.setView(dialogAlertView);
        messageAlertView.setText(R.string.alert_dialog_loading_profile);
        dialog = builder.create();
        dialog.show();
    }

    // Create Chat Activity
    private void createChat(String idOfWinner, String nameOfWinner) {
        Map<String, Object> chat = new HashMap<>();
        Map<String, Object> user1 = new HashMap<>();
        Map<String, Object> user2 = new HashMap<>();

        db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document2 = task.getResult();
                    if (document2 != null) {
                        activeChatUsersID = (ArrayList<String>) document2.get("otherUsersIDFromStartedChatsArray");
                        db.collection("users").document(idOfWinner).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document != null) {
                                        if (activeChatUsersID.contains(idOfWinner)) {
                                            Toast.makeText(getApplicationContext(), "You already have active chat with that person!", Toast.LENGTH_SHORT).show();
                                        } else {

                                            db.collection("chats").add(chat)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            String chatID = documentReference.getId();
                                                            chat.put("chatID", chatID);
                                                            chat.put("dateOfChatCreation", new Date());
                                                            chat.put("usersParticipatingName", Arrays.asList(currentUserName, nameOfWinner));
                                                            chat.put("usersThatHaveNotDeletedConversation", Arrays.asList(user.getUid(), idOfWinner));
                                                            chat.put("lastMessage", "");
                                                            chat.put("lastMessageDate", new Date());
                                                            String firstPhotoUriOfOtherUser = document.getString("firstPhotoUri");
                                                            String firstPhotoUriOfUser = document2.getString("firstPhotoUri");
                                                            chat.put("usersParticipatingFirstImageUri", Arrays.asList(firstPhotoUriOfUser, firstPhotoUriOfOtherUser));
                                                            chat.put("usersParticipatingID", Arrays.asList(user.getUid(), idOfWinner));
                                                            chat.put("isFirstPhotoOfUserUncovered", Arrays.asList(true, true));
                                                            chat.put("wasUserInActivityNr0", true);
                                                            chat.put("wasUserInActivityNr1", false);

                                                            user1.put("userID", user.getUid());
                                                            user1.put("pointsFromOtherUser", (Integer) 0);
                                                            user1.put("userName", currentUserName);
                                                            user1.put("uncoverStrangerFirstPhoto", true);
                                                            user1.put("uncoverStrangerSecondPhoto", false);
                                                            user1.put("uncoverStrangerThirdPhoto", false);
                                                            user1.put("uncoverStrangerDescription", false);
                                                            user1.put("uncoverStrangerAge", false);
                                                            user1.put("uncoverStrangerLocation", false);
                                                            user1.put("uncoverStrangerGender", false);
                                                            user1.put("lastTimeInChatActivity", new Date());
                                                            Log.d(TAG, user1.toString());
                                                            db.collection("chats").document(chatID).collection("chatUsers").document(user.getUid()).set(user1);

                                                            user2.put("userID", idOfWinner);
                                                            user2.put("pointsFromOtherUser", (Integer) 0);
                                                            user2.put("userName", nameOfWinner);
                                                            user2.put("uncoverStrangerFirstPhoto", true);
                                                            user2.put("uncoverStrangerSecondPhoto", false);
                                                            user2.put("uncoverStrangerThirdPhoto", false);
                                                            user2.put("uncoverStrangerDescription", false);
                                                            user2.put("uncoverStrangerAge", false);
                                                            user2.put("uncoverStrangerLocation", false);
                                                            user2.put("uncoverStrangerGender", false);
                                                            user2.put("lastTimeInChatActivity", new Date());
                                                            Log.d(TAG, user2.toString());
                                                            db.collection("chats").document(chatID).collection("chatUsers").document(idOfWinner).set(user2);

                                                            db.collection("users").document(user.getUid()).update("otherUsersIDFromStartedChatsArray", FieldValue.arrayUnion(idOfWinner));

                                                            db.collection("users").document(idOfWinner).update("otherUsersIDFromStartedChatsArray", FieldValue.arrayUnion(user.getUid()));

                                                            db.collection("chats").document(chatID).set(chat)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

//                                        sendNotificationAboutChat(idOfOtherUser, user.getUid(),"New conversation just started with " + nameOfUser
//                                                ,nameOfOtherUser, nameOfUser, chatID);

                                                                            Intent intent2 = new Intent(getApplicationContext(), ChatActivity.class);
                                                                            intent2.putExtra("chatIdIntent", chatID);
                                                                            intent2.putExtra("otherUserName", nameOfWinner);
                                                                            intent2.putExtra("idOfOtherUser", idOfWinner);
                                                                            startActivity(intent2);


                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.w(TAG, "Error writing document", e);
                                                                        }
                                                                    });
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d(TAG, e.toString());
                                                        }
                                                    });

                                        }

                                    }
                                }
                            }
                        });

                    }
                }
            }
        });
    }

}