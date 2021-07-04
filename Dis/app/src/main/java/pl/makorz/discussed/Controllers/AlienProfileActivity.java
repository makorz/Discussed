package pl.makorz.discussed.Controllers;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import pl.makorz.discussed.R;

import static com.google.firebase.firestore.FieldValue.arrayUnion;


public class AlienProfileActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "AlienProfileActivity";

    public static final String AGE_FIELD = "ageOfUser";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String TOPICS_ARRAY = "chosenTopicsArray";
    public static final String FIRST_PHOTO_URI = "firstPhotoUri";
    public static final String SECOND_PHOTO_URI = "secondPhotoUri";
    public static final String THIRD_PHOTO_URI = "thirdPhotoUri";
    public static final String COUNTRY_NAME_FIELD = "locationCountryName";
    public static final String PLACE_NAME_FIELD = "placeName";
    public static final String NAME_FIELD = "displayName";

    public static final String UNCOVER_DESCRIPTION_MADE = "uncoverStrangerDescription";
    public static final String UNCOVER_LOCATION_MADE = "uncoverStrangerLocation";
    public static final String UNCOVER_AGE_MADE = "uncoverStrangerAge";
    public static final String UNCOVER_FIRST_PHOTO_MADE = "uncoverStrangerFirstPhoto";
    public static final String UNCOVER_SECOND_PHOTO_MADE = "uncoverStrangerSecondPhoto";
    public static final String UNCOVER_THIRD_PHOTO_MADE = "uncoverStrangerThirdPhoto";
    public static final String POINTS_TO_USE = "pointsFromOtherUser";

    private String otherUserName, chatIdIntent, idOfOtherUser, currentUserID;
    private String firstPhotoUri, secondPhotoUri, thirdPhotoUri;
    private TextView profileDescriptionText, ageText, locationText, titleText, topicsText, nrOfPointsText, messageAlertView;
    private ImageView firstImageView, secondImageView, thirdImageView;
    public Button buttonDescriptionUncover, buttonAgeUncover, buttonLocationUncover, buttonFirstImageUncover,
            buttonSecondImageUncover, buttonThirdImageUncover;
    private Boolean firstPhotoUncovered, secondPhotoUncovered, thirdPhotoUncovered, locationUncovered, descriptionUncovered, ageUncovered;
    private AlertDialog dialog;
    public static int whatButtonPressed = -1;
    private ConstraintLayout layoutToDimWhenSearching;
    private ArrayList<String> topicList;
    private long pointsToUse;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alien_profile);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

        Intent intent = getIntent();
        chatIdIntent = intent.getStringExtra("chatIdIntent");
        otherUserName = intent.getStringExtra("otherUserName");
        idOfOtherUser = intent.getStringExtra("idOfOtherUser");
        currentUserID = intent.getStringExtra("currentUserID");
        getSupportActionBar().setTitle(otherUserName + " Profile");

        // Alert of loading data of profile from server
        loadingAlertDialog();
        layoutToDimWhenSearching = findViewById(R.id.layout_Constraint_Alien_Profile);
        layoutToDimWhenSearching.setAlpha(0.0f);
        initView();

        // Loading data from server in second thread
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            updateAlienUserCollection();
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                });
            }
        }.start();

    }
    // This function updates profile information when activity is started
    private void updateAlienUserCollection() throws ExecutionException, InterruptedException {
        // Download document of current user, to retrieve actual info to profile view
        DocumentReference docRef = db.collection("users").document(idOfOtherUser);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentOfOtherUser = task.getResult();
                    if (documentOfOtherUser != null) {

                        DocumentReference docRef2 = db.collection("chats").document(chatIdIntent).collection("chatUsers").document(currentUserID);
                                docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot documentOfCurrentUserFromChat = task.getResult();
                                            if (documentOfCurrentUserFromChat != null) {

                                                firstPhotoUncovered = documentOfCurrentUserFromChat.getBoolean(UNCOVER_FIRST_PHOTO_MADE);
                                                secondPhotoUncovered = documentOfCurrentUserFromChat.getBoolean(UNCOVER_SECOND_PHOTO_MADE);
                                                thirdPhotoUncovered = documentOfCurrentUserFromChat.getBoolean(UNCOVER_THIRD_PHOTO_MADE);
                                                locationUncovered = documentOfCurrentUserFromChat.getBoolean(UNCOVER_LOCATION_MADE);
                                                ageUncovered = documentOfCurrentUserFromChat.getBoolean(UNCOVER_AGE_MADE);
                                                descriptionUncovered = documentOfCurrentUserFromChat.getBoolean(UNCOVER_DESCRIPTION_MADE);

                                                pointsToUse = documentOfCurrentUserFromChat.getLong(POINTS_TO_USE);

                                                firstPhotoUri = documentOfOtherUser.getString(FIRST_PHOTO_URI);
                                                secondPhotoUri = documentOfOtherUser.getString(SECOND_PHOTO_URI);
                                                thirdPhotoUri = documentOfOtherUser.getString(THIRD_PHOTO_URI);

                                                String title = "Welcome to profile of " + documentOfOtherUser.getString(NAME_FIELD) + "!";
                                                titleText.setText(title);
                                                String pointBegin = "Nr of points left to use:  ";
                                                String pointNr = String.valueOf(pointsToUse);
                                                SpannableString pointsText = new SpannableString(pointBegin + pointNr);
                                                pointsText.setSpan(new StyleSpan(Typeface.BOLD), pointBegin.length(), pointBegin.length() + pointNr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                nrOfPointsText.setText(pointsText);

                                                if (descriptionUncovered) {
                                                    profileDescriptionText.setText(documentOfOtherUser.getString(DESCRIPTION_FIELD));
                                                }

                                                if (ageUncovered) {
                                                    String age = Integer.toString(documentOfOtherUser.getDouble(AGE_FIELD).intValue());
                                                    ageText.setText(age);
                                                }

                                                if (locationUncovered) {
                                                    String placeName = documentOfOtherUser.getString(PLACE_NAME_FIELD);
                                                    String countryName = documentOfOtherUser.getString(COUNTRY_NAME_FIELD);
                                                    String localisation = placeName + ", " + countryName + ".";
                                                    locationText.setText(localisation);
                                                }

                                                topicList = (ArrayList<String>) documentOfOtherUser.get(TOPICS_ARRAY);
                                                StringBuilder topicsListInTextView = new StringBuilder(otherUserName + "'s favorite topics: \n\n");
                                                for (int i = 0; i < topicList.size(); i++) {
                                                        String addTopic;
                                                        String nrOfTopic = String.valueOf(i + 1);
                                                        if (i == topicList.size() - 1) {
                                                            addTopic = nrOfTopic + ". " + topicList.get(i);
                                                        } else {
                                                            addTopic = nrOfTopic + ". " + topicList.get(i) + "\n";
                                                        }
                                                        topicsListInTextView.append(addTopic);
                                                    }
                                                topicsText.setText(topicsListInTextView);

                                                if (firstPhotoUncovered) {
                                                    Glide.with(getApplicationContext()).load(firstPhotoUri).into(firstImageView);
                                                }
                                                if (secondPhotoUncovered) {
                                                    Glide.with(getApplicationContext()).load(secondPhotoUri).into(secondImageView);
                                                }
                                                if (thirdPhotoUncovered) {
                                                    Glide.with(getApplicationContext()).load(thirdPhotoUri).into(thirdImageView);
                                                }

                                                layoutToDimWhenSearching.setAlpha(1.0f);
                                                dialog.dismiss();

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

    // this function initialises buttons, text views, etc.
    public void initView() {

        profileDescriptionText = findViewById(R.id.alien_description_profile_textview);
        ageText = findViewById(R.id.alien_age_profile_textview);
        nrOfPointsText = findViewById(R.id.points_alien_profile_textview);
        titleText = findViewById(R.id.title_alien_profile_textview);
        locationText = findViewById(R.id.alien_location_profile_textview);
        topicsText = findViewById(R.id.topics_alien_profile_textview);

        firstImageView = findViewById(R.id.imageview_first_image_alien_profile);
        secondImageView = findViewById(R.id.imageview_second_image_alien_profile);
        thirdImageView = findViewById(R.id.imageview_third_image_alien_profile);

        buttonDescriptionUncover = findViewById(R.id.description_alien_uncover_button);
        buttonLocationUncover = findViewById(R.id.location_alien_uncover_button);
        buttonAgeUncover = findViewById(R.id.age_alien_uncover_button);
        buttonFirstImageUncover = findViewById(R.id.first_image_uncover);
        buttonSecondImageUncover = findViewById(R.id.second_image_uncover);
        buttonThirdImageUncover = findViewById(R.id.third_image_uncover);

        buttonThirdImageUncover.setOnClickListener(this);
        buttonSecondImageUncover.setOnClickListener(this);
        buttonFirstImageUncover.setOnClickListener(this);
        buttonLocationUncover.setOnClickListener(this);
        buttonDescriptionUncover.setOnClickListener(this);

        firstImageView.setOnClickListener(this);
        secondImageView.setOnClickListener(this);
        thirdImageView.setOnClickListener(this);

    }

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

    @Override
    public void onClick(View view) {
        int photoCost = 10;
        int descriptionCost = 8;
        int locationCost = 5;
        int ageCost = 5;

        switch (view.getId()) {
            case R.id.description_alien_uncover_button:
                spendPointsAlertDialog("Do You want to spend " + descriptionCost + " points to uncover description?",
                        descriptionCost, UNCOVER_DESCRIPTION_MADE);
                whatButtonPressed = 1;
                break;
            case R.id.age_alien_uncover_button:
                spendPointsAlertDialog("Do You want to spend " + ageCost + " points to uncover age?",
                        ageCost, UNCOVER_AGE_MADE);
                whatButtonPressed = 2;
                break;
            case R.id.location_alien_uncover_button:
                spendPointsAlertDialog("Do You want to spend " + locationCost + " points to uncover location?",
                        locationCost, UNCOVER_LOCATION_MADE);
                whatButtonPressed = 3;
                break;
            case R.id.first_image_uncover:
                spendPointsAlertDialog("Do You want to spend " + photoCost + " points to uncover this photo?",
                        photoCost, UNCOVER_FIRST_PHOTO_MADE);
                whatButtonPressed = 4;
                break;
            case R.id.second_image_uncover:
                spendPointsAlertDialog("Do You want to spend " + photoCost + " points to uncover this photo?",
                        photoCost, UNCOVER_SECOND_PHOTO_MADE);
                whatButtonPressed = 5;
                break;
            case R.id.third_image_uncover:
                spendPointsAlertDialog("Do You want to spend " + photoCost + " points to uncover this photo?",
                        photoCost, UNCOVER_THIRD_PHOTO_MADE);
                whatButtonPressed = 6;
                break;
            case R.id.imageview_first_image_alien_profile:
                photoShowDialog(firstPhotoUri, firstPhotoUncovered);
                whatButtonPressed = 7;
                break;
            case R.id.imageview_second_image_alien_profile:
                photoShowDialog(secondPhotoUri,secondPhotoUncovered);
                whatButtonPressed = 8;
                break;
            case R.id.imageview_third_image_alien_profile:
                photoShowDialog(thirdPhotoUri, thirdPhotoUncovered);
                whatButtonPressed = 9;
                break;
            default:
                break;
        }
    }

    // This function shows images in bigger format
    public void photoShowDialog(String imagePath, Boolean wasPhotoUncovered) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogAlertView = inflater.inflate(R.layout.photo_full_screen, null);
        ImageView imageFullView = dialogAlertView.findViewById(R.id.full_screen_photo_show);
        imageFullView.setImageResource(R.drawable.question_icon);
        if (wasPhotoUncovered) {
            Glide.with(getApplicationContext()).load(imagePath).into(imageFullView);
        }
        builder.setView(dialogAlertView);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    public void spendPointsAlertDialog(String title, int pointsCost, String whatUncovered) {
        LayoutInflater inflater = getLayoutInflater();
        View uncoverAlertView = inflater.inflate(R.layout.dialog_uncover, null);
        TextView uncoverText = uncoverAlertView.findViewById(R.id.uncover_text);
        uncoverText.setText(title);

        AlertDialog uncoverDialog = new AlertDialog.Builder(this)
                .setView(uncoverAlertView)  // What to use in dialog box
                .setNegativeButton("Abort!", null)
                .setPositiveButton("YES", null)
                .show();

        uncoverDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pointsToUse >= pointsCost) {
                    pointsToUse -= pointsCost;

                    db.collection("chats").document(chatIdIntent).collection("chatUsers").document(currentUserID)
                            .update(whatUncovered, true);
                    db.collection("chats").document(chatIdIntent).collection("chatUsers").document(currentUserID)
                            .update(POINTS_TO_USE, FieldValue.increment(-pointsCost));

                    if (whatUncovered.equals(UNCOVER_FIRST_PHOTO_MADE)) {

                    }


                    try {
                        updateAlienUserCollection();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    View uncoverDialogView2 = inflater.inflate(R.layout.dialog_uncover, null);
                    TextView uncoverText = uncoverDialogView2.findViewById(R.id.uncover_text);
                    uncoverText.setText("You don't have enough points to uncover what You want. \n Try harder!");

                    AlertDialog uncoverDialog2 = new AlertDialog.Builder(AlienProfileActivity.this)
                            .setView(uncoverDialogView2)
                            .setPositiveButton("OK, I GET IT!", null)
                            .show();

                    uncoverDialog2.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            uncoverDialog2.dismiss();

                        }
                    });
                    uncoverDialog.dismiss();

                }
                uncoverDialog.dismiss();
            }

        });

    }

}
