package pl.makorz.discussed.Controllers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import pl.makorz.discussed.R;


public class AlienProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AlienProfileActivity";

    private static final String AGE_FIELD = "ageOfUser";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String TOPICS_ARRAY = "chosenTopicsArray";
    private static final String FIRST_PHOTO_URI = "firstPhotoUri";
    private static final String SECOND_PHOTO_URI = "secondPhotoUri";
    private static final String THIRD_PHOTO_URI = "thirdPhotoUri";
    private static final String COUNTRY_NAME_FIELD = "locationCountryName";
    private static final String PLACE_NAME_FIELD = "placeName";
    private static final String NAME_FIELD = "displayName";

    private static final String UNCOVER_DESCRIPTION_MADE = "uncoverStrangerDescription";
    private static final String UNCOVER_LOCATION_MADE = "uncoverStrangerLocation";
    private static final String UNCOVER_AGE_MADE = "uncoverStrangerAge";
    private static final String UNCOVER_FIRST_PHOTO_MADE = "uncoverStrangerFirstPhoto";
    private static final String UNCOVER_SECOND_PHOTO_MADE = "uncoverStrangerSecondPhoto";
    private static final String UNCOVER_THIRD_PHOTO_MADE = "uncoverStrangerThirdPhoto";
    private static final String POINTS_TO_USE = "pointsFromOtherUser";

    private static final String USERS_ID_ARRAY = "usersParticipatingID";
    private static final String USERS_FIRST_PHOTO_UNCOVERED = "isFirstPhotoOfUserUncovered";


    private String otherUserName, chatIdIntent, idOfOtherUser, currentUserID, firstPhotoUri, secondPhotoUri, thirdPhotoUri;
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
        getSupportActionBar().hide();

        Intent intent = getIntent();
        chatIdIntent = intent.getStringExtra("chatIdIntent");
        otherUserName = intent.getStringExtra("otherUserName");
        idOfOtherUser = intent.getStringExtra("idOfOtherUser");
        currentUserID = intent.getStringExtra("currentUserID");

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

                                                String title1 = getString(R.string.title_text_1_alien_profile_activity);
                                                String title2 = documentOfOtherUser.getString(NAME_FIELD);
                                                String title3 = getString(R.string.title_text_2_alien_profile_activity);
                                                SpannableString titleTextWords = new SpannableString(title1 + title2 + title3);
                                                //Length of italic text if language set on device is polish (different style of title)
                                                int endPoint = title1.length() + title2.length();
                                                if (title3.length() > 1) {
                                                    endPoint = title1.length() + title2.length() + 2;
                                                }
                                                titleTextWords.setSpan(new StyleSpan(Typeface.ITALIC), title1.length(), endPoint, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                titleText.setText(titleTextWords);

                                                String pointBegin = getString(R.string.nr_of_points_text_alien_profile_activity);
                                                String pointNr = String.valueOf(pointsToUse);
                                                SpannableString pointsText = new SpannableString(pointBegin + pointNr);
                                                pointsText.setSpan(new StyleSpan(Typeface.BOLD), pointBegin.length(), pointBegin.length() + pointNr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                nrOfPointsText.setText(pointsText);

                                                if (descriptionUncovered) {
                                                    buttonDescriptionUncover.setEnabled(false);
                                                    profileDescriptionText.setText(documentOfOtherUser.getString(DESCRIPTION_FIELD));

                                                }

                                                if (ageUncovered) {
                                                    buttonAgeUncover.setEnabled(false);
                                                    String age = Integer.toString(documentOfOtherUser.getDouble(AGE_FIELD).intValue());
                                                    ageText.setText(age);

                                                }

                                                if (locationUncovered) {
                                                    buttonLocationUncover.setEnabled(false);
                                                    String placeName = documentOfOtherUser.getString(PLACE_NAME_FIELD);
                                                    String countryName = documentOfOtherUser.getString(COUNTRY_NAME_FIELD);
                                                    String localisation = placeName + ", " + countryName + ".";
                                                    locationText.setText(localisation);

                                                }

                                                topicList = (ArrayList<String>) documentOfOtherUser.get(TOPICS_ARRAY);
                                                StringBuilder topicsListInTextView = new StringBuilder(getString(R.string.favorite_topics_text_alien_profile_activity));
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
                                                    buttonFirstImageUncover.setEnabled(false);
                                                    Glide.with(getApplicationContext()).load(firstPhotoUri).into(firstImageView);
                                                }
                                                if (secondPhotoUncovered) {
                                                    buttonSecondImageUncover.setEnabled(false);
                                                    Glide.with(getApplicationContext()).load(secondPhotoUri).into(secondImageView);
                                                }
                                                if (thirdPhotoUncovered) {
                                                    buttonThirdImageUncover.setEnabled(false);
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
                    dialog.dismiss();
                    notExistingUserAlertDialog();
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
        buttonAgeUncover.setOnClickListener(this);

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

    // PopUp with info about not existing user.
    private void notExistingUserAlertDialog() {

        LayoutInflater inflaterDialog = LayoutInflater.from(this);
        View noUserAlertView = inflaterDialog.inflate(R.layout.dialog_uncover, null);
        TextView uncoverText = noUserAlertView.findViewById(R.id.uncover_text);
        uncoverText.setText(R.string.user_not_exist_dialog_box);

        AlertDialog infoDialog = new AlertDialog.Builder(this)
                .setView(noUserAlertView)  // What to use in dialog box
                .setPositiveButton("OK", null)
                .show();

        infoDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    infoDialog.dismiss();
                    finish();
                }
        });
    }


    @Override
    public void onClick(View view) {
        int photoCost = 15;
        int descriptionCost = 10;
        int locationCost = 5;
        int ageCost = 5;

        switch (view.getId()) {
            case R.id.description_alien_uncover_button:
                spendPointsAlertDialog(getString(R.string.spend_points_text_1_dialog_alien_profile_activity) + descriptionCost
                                + getString(R.string.spend_points_text_2_description_dialog_alien_profile_activity), descriptionCost, UNCOVER_DESCRIPTION_MADE);
                whatButtonPressed = 1;
                break;
            case R.id.age_alien_uncover_button:
                spendPointsAlertDialog(getString(R.string.spend_points_text_1_dialog_alien_profile_activity) + ageCost
                                + getString(R.string.spend_points_text_2_age_dialog_alien_profile_activity), ageCost, UNCOVER_AGE_MADE);
                whatButtonPressed = 2;
                break;
            case R.id.location_alien_uncover_button:
                spendPointsAlertDialog(getString(R.string.spend_points_text_1_dialog_alien_profile_activity) + locationCost
                                + getString(R.string.spend_points_text_2_location_dialog_alien_profile_activity), locationCost, UNCOVER_LOCATION_MADE);
                whatButtonPressed = 3;
                break;
            case R.id.first_image_uncover:
                spendPointsAlertDialog(getString(R.string.spend_points_text_1_dialog_alien_profile_activity) + photoCost
                                + getString(R.string.spend_points_text_2_photo_dialog_alien_profile_activity), photoCost, UNCOVER_FIRST_PHOTO_MADE);
                whatButtonPressed = 4;
                break;
            case R.id.second_image_uncover:
                spendPointsAlertDialog(getString(R.string.spend_points_text_1_dialog_alien_profile_activity) + photoCost
                                + getString(R.string.spend_points_text_2_photo_dialog_alien_profile_activity), photoCost, UNCOVER_SECOND_PHOTO_MADE);
                whatButtonPressed = 5;
                break;
            case R.id.third_image_uncover:
                spendPointsAlertDialog(getString(R.string.spend_points_text_1_dialog_alien_profile_activity) + photoCost
                                + getString(R.string.spend_points_text_2_photo_dialog_alien_profile_activity), photoCost, UNCOVER_THIRD_PHOTO_MADE);
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
                .setNegativeButton(R.string.no_text_dialog_boxes, null)
                .setPositiveButton(R.string.yes_text_dialog_boxes, null)
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

                        DocumentReference docRef3 = db.collection("chats").document(chatIdIntent);
                        docRef3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentOfChat = task.getResult();
                                    if (documentOfChat != null) {

                                        List<String> listOfUsers = (List<String>) documentOfChat.get(USERS_ID_ARRAY);
                                        List<Boolean> listOfUsersUncoveredFirstPhoto = (List<Boolean>) documentOfChat.get(USERS_FIRST_PHOTO_UNCOVERED);
                                        int index = listOfUsers.indexOf(currentUserID);

                                        Boolean a =  true;
                                        Boolean b = listOfUsersUncoveredFirstPhoto.get(index);

                                        db.collection("chats").document(chatIdIntent)
                                                .update(USERS_FIRST_PHOTO_UNCOVERED, FieldValue.delete());
                                        if (index == 0) {
                                            docRef3.update(USERS_FIRST_PHOTO_UNCOVERED, FieldValue.arrayUnion(b,a));
                                        } else {
                                            docRef3.update(USERS_FIRST_PHOTO_UNCOVERED, FieldValue.arrayUnion(a,b));
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


                    try {
                        updateAlienUserCollection();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    View uncoverDialogView2 = inflater.inflate(R.layout.dialog_uncover, null);
                    TextView uncoverText = uncoverDialogView2.findViewById(R.id.uncover_text);
                    uncoverText.setText(R.string.info_not_enough_points_to_uncover_alien_profile_activity);

                    AlertDialog uncoverDialog2 = new AlertDialog.Builder(AlienProfileActivity.this)
                            .setView(uncoverDialogView2)
                            .setPositiveButton(R.string.understand_text_dialog_boxes, null)
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
