package pl.makorz.discussed.Controllers;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.maps.model.LatLng;
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
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import pl.makorz.discussed.Models.Adapters.TopicsAdapter;
import pl.makorz.discussed.Models.Topic;
import pl.makorz.discussed.R;
import static com.google.firebase.firestore.FieldValue.arrayUnion;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ProfileActivity";

    private static final String AGE_FIELD = "ageOfUser";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String NAME_FIELD = "displayName";
    private static final String LOCATION_FIELD = "location";
    private static final String TOPICS_ARRAY = "chosenTopicsArray";
    private static final String GENDER_FIELD = "genderFemale";
    private static final String FIRST_PHOTO_URI = "firstPhotoUri";
    private static final String SECOND_PHOTO_URI = "secondPhotoUri";
    private static final String THIRD_PHOTO_URI = "thirdPhotoUri";
    private static final String CAN_USER_SEARCH = "canUserSearch";
    private static final String COUNTRY_NAME_FIELD = "locationCountryName";
    private static final String COUNTRY_CODE_FIELD = "locationCountryCode";
    private static final String PLACE_NAME_FIELD = "placeName";
    private static final String BLIND_DATE_PARTICIPATION_WILL = "blindDateParticipationWill";
    private static final String SEARCH_ID_COUNTRY = "searchIDCountry";
    private static final String SEARCH_ID_WORLD = "searchIDWorld";
    private static final String NR_OF_USERS_TO_SEARCH = "nrOfUsers";

    private static final String FIRST_PHOTO_UPLOAD_MADE = "firstPhotoUploadMade";
    private static final String SECOND_PHOTO_UPLOAD_MADE = "secondPhotoUploadMade";
    private static final String THIRD_PHOTO_UPLOAD_MADE = "thirdPhotoUploadMade";
    private static final String LOCATION_UPLOAD_MADE = "locationUploadMade";
    private static final String TOPICS_UPLOAD_MADE = "topicsUploadMade";
    private static final String AGE_UPLOAD_MADE = "ageUploadMade";
    private static final String DESCRIPTION_UPLOAD_MADE = "descriptionUploadMade";
    private static final String GENDER_UPLOAD_MADE = "genderUploadMade";
    private static final String NAME_UPLOAD_MADE = "nameUploadMade";

    private static final String FIRST_PHOTO_UPLOAD_DATE = "firstPhotoUploadDate";
    private static final String SECOND_PHOTO_UPLOAD_DATE = "secondPhotoUploadDate";
    private static final String THIRD_PHOTO_UPLOAD_DATE = "thirdPhotoUploadDate";
    private static final String AGE_UPLOAD_DATE = "ageUploadDate";
    private static final String GENDER_UPLOAD_DATE = "genderUploadDate";
    private static final String LOCATION_UPLOAD_DATE = "locationUploadDate";
    private static final String TOPICS_UPLOAD_DATE = "topicsUploadDate";
    private static final String DESCRIPTION_UPLOAD_DATE = "descriptionUploadDate";
    private static final String NAME_UPLOAD_DATE = "nameUploadDate";

    public static int whatButtonPressed = -1;
    private Uri imagePath;

    public Button buttonDescriptionChange, buttonAgeChange, buttonLocationChange, buttonNameChange, buttonTopicsChange, buttonFirstImageChange,
            buttonSecondImageChange, buttonThirdImageChange, buttonGenderChange;
    private TextView profileDescriptionText, ageText, locationText, nameText, titleText, topicsText, genderText, messageAlertView;
    private ImageView firstImageView, secondImageView, thirdImageView;
    private Boolean firstPhotoUploadMade, secondPhotoUploadMade, thirdPhotoUploadMade, locationUploadMade, descriptionUploadMade, ageUploadMade, topicsUploadMade,
            genderUploadMade, nameUploadMade, whatGender, canUserSearch;
    private AlertDialog dialog;
    private String firstPhotoUri, secondPhotoUri, thirdPhotoUri, searchIDCountry, searchIDWorld;
    private ConstraintLayout layoutToDimWhenSearching;
    private ArrayList<String> topicList;
    private TopicsAdapter adapterTopics;
    private Date currentDate, firstPhotoUploadDate, secondPhotoUploadDate, thirdPhotoUploadDate, locationUploadDate, descriptionUploadDate, ageUploadDate,
            genderUploadDate, nameUploadDate;

    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    // Main function of creating activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().hide();

        // Alert of loading data of profile from server
        loadingAlertDialog();
        messageAlertView.setText(R.string.alert_dialog_loading_profile);
        layoutToDimWhenSearching = findViewById(R.id.layout_Constraint_Profile);
        layoutToDimWhenSearching.setAlpha(0.0f);
        initView();

        try {
            updateUserCollection();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        currentDate = new Date(new Date().getTime());

    }

    // This function updates profile information when activity is started
    private void updateUserCollection() throws ExecutionException, InterruptedException {
        // Download document of current user, to retrieve actual info to profile view
        DocumentReference docRef = db.collection("users").document(currentUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {

                        firstPhotoUploadMade = document.getBoolean(FIRST_PHOTO_UPLOAD_MADE);
                        secondPhotoUploadMade = document.getBoolean(SECOND_PHOTO_UPLOAD_MADE);
                        thirdPhotoUploadMade = document.getBoolean(THIRD_PHOTO_UPLOAD_MADE);
                        locationUploadMade = document.getBoolean(LOCATION_UPLOAD_MADE);
                        nameUploadMade = document.getBoolean(NAME_UPLOAD_MADE);
                        ageUploadMade = document.getBoolean(AGE_UPLOAD_MADE);
                        topicsUploadMade = document.getBoolean(TOPICS_UPLOAD_MADE);
                        descriptionUploadMade = document.getBoolean(DESCRIPTION_UPLOAD_MADE);
                        genderUploadMade = document.getBoolean(GENDER_UPLOAD_MADE);
                        canUserSearch = document.getBoolean(CAN_USER_SEARCH);

                        firstPhotoUri = document.getString(FIRST_PHOTO_URI);
                        secondPhotoUri = document.getString(SECOND_PHOTO_URI);
                        thirdPhotoUri = document.getString(THIRD_PHOTO_URI);

                        String title1 = getString(R.string.welcome_text_1_profile_activity);
                        String title2 = document.getString(NAME_FIELD) + " ";
                        String title3 = getString(R.string.welcome_text_2_profile_activity);
                        SpannableString titleTextWords = new SpannableString(title1 + title2 + title3);
                        titleTextWords.setSpan(new StyleSpan(Typeface.ITALIC), title1.length(), title1.length() + title2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        titleText.setText(titleTextWords);

                        nameText.setText(R.string.not_filled);
                        if (nameUploadMade) {
                            nameText.setText(document.getString(NAME_FIELD));
                        }
                        profileDescriptionText.setText(R.string.not_filled);
                        if (descriptionUploadMade) {
                            profileDescriptionText.setText(document.getString(DESCRIPTION_FIELD));
                        }

                        ageText.setText(R.string.not_filled);
                        if (ageUploadMade) {
                            String age = Integer.toString(document.getDouble(AGE_FIELD).intValue());
                            ageText.setText(age);
                        }
                        locationText.setText(R.string.not_filled);
                        if (locationUploadMade) {
                            String placeName = document.getString(PLACE_NAME_FIELD);
                            String countryName = document.getString(COUNTRY_NAME_FIELD);
                            String localisation = placeName + ", " + countryName + ".";
                            locationText.setText(localisation);
                        }

                        firstPhotoUploadDate = document.getDate(FIRST_PHOTO_UPLOAD_DATE);
                        secondPhotoUploadDate = document.getDate(SECOND_PHOTO_UPLOAD_DATE);
                        thirdPhotoUploadDate = document.getDate(THIRD_PHOTO_UPLOAD_DATE);
                        locationUploadDate = document.getDate(LOCATION_UPLOAD_DATE);
                        nameUploadDate = document.getDate(NAME_UPLOAD_DATE);
                        ageUploadDate = document.getDate(AGE_UPLOAD_DATE);
                        descriptionUploadDate = document.getDate(DESCRIPTION_UPLOAD_DATE);
                        genderUploadDate = document.getDate(GENDER_UPLOAD_DATE);

                        topicsText.setText(R.string.not_filled);
                        if (topicsUploadMade) {
                            topicList = (ArrayList<String>) document.get(TOPICS_ARRAY);
                            StringBuilder topicsListInTextView = new StringBuilder(getString(R.string.your_topics_text_profile_activity));
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
                        }

                        genderText.setText(R.string.not_filled);
                        if (genderUploadMade){
                            whatGender = document.getBoolean(GENDER_FIELD);
                            if (!whatGender) {
                                genderText.setText(R.string.gender_male_profile_activity);
                            } else {
                                genderText.setText(R.string.gender_female_profile_activity);
                            }
                        }
                        if (firstPhotoUploadMade) {
                            Glide.with(getApplicationContext()).load(firstPhotoUri).into(firstImageView);
                        }
                        if (secondPhotoUploadMade) {
                            Glide.with(getApplicationContext()).load(secondPhotoUri).into(secondImageView);
                        }
                        if (thirdPhotoUploadMade) {
                            Glide.with(getApplicationContext()).load(thirdPhotoUri).into(thirdImageView);
                        }

                        if (firstPhotoUploadMade && secondPhotoUploadMade && thirdPhotoUploadMade && locationUploadMade && nameUploadMade && ageUploadMade && topicsUploadMade
                                && descriptionUploadMade && genderUploadMade && !canUserSearch ) {

                            addUserInSearchCollection(currentUser.getUid(), document.getDouble(AGE_FIELD).intValue(), whatGender, document.getString(COUNTRY_CODE_FIELD),
                                    document.getGeoPoint(LOCATION_FIELD), document.getBoolean(BLIND_DATE_PARTICIPATION_WILL),topicList);

                            db.collection("users").document(currentUser.getUid()).update(CAN_USER_SEARCH, true);
                            canUserSearch = true;

                        }


                    } else {
                        Log.d("LOGGER", "No such document");
                    }

                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
                layoutToDimWhenSearching.setAlpha(1.0f);
                dialog.dismiss();
            }
        });

    }

    // This functions show dialog boxes where user can input specific data
    private void descriptionAgeNameChangeDialog(String whatFieldUpdate, String titleDialogBox) {

        // Layout dialog box
        LayoutInflater inflaterDialog = LayoutInflater.from(this);
        View dialogView = inflaterDialog.inflate(R.layout.dialog_description_change, null);
        EditText editDialogText = dialogView.findViewById(R.id.dialog_description_edit_text);
        // Filter input
        if (whatFieldUpdate.equals(AGE_FIELD)) {
            editDialogText.setHint(R.string.chose_age_title_profile_activity);
            editDialogText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if (whatFieldUpdate.equals(DESCRIPTION_FIELD)) {
            editDialogText.setHint(R.string.description_dialog_hint);
           // DescriptionFilter descriptionFilter = new DescriptionFilter(ProfileActivity.this);
            InputFilter lengthFilter = new InputFilter.LengthFilter(500);
            editDialogText.setFilters(new InputFilter[]{lengthFilter});
            //editDialogText.setFilters(new DescriptionFilter[]{descriptionFilter});
        } else if (whatFieldUpdate.equals(NAME_FIELD)) {
            editDialogText.setHint(R.string.chose_name_title_profile_activity);
            InputFilter lengthFilter = new InputFilter.LengthFilter(35);
            editDialogText.setFilters(new InputFilter[]{lengthFilter});
        }

        AlertDialog fieldValueChangeDialog = new AlertDialog.Builder(this)
                .setTitle(titleDialogBox)
                .setView(dialogView)  // What to use in dialog box
                .setNegativeButton(R.string.no_text_dialog_boxes, null)
                .setPositiveButton("OK", null)
                .show();

        fieldValueChangeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Clicked positive!");
                if (whatButtonPressed == 2) {
                    int valueEntered = Integer.parseInt(editDialogText.getText().toString());
                    if (valueEntered < 18 || valueEntered > 115) {
                        Toast.makeText(ProfileActivity.this, R.string.wrong_age_info_profile_activity, Toast.LENGTH_SHORT).show();
                    } else {

                        db.collection("users").document(currentUser.getUid())
                                .update(whatFieldUpdate, valueEntered);//what you update
                        if (!ageUploadMade) {
                            db.collection("users").document(currentUser.getUid())
                                    .update(AGE_UPLOAD_MADE, true);
                        }
                        db.collection("users").document(currentUser.getUid())
                                .update(AGE_UPLOAD_DATE, new Date());
                        try {
                            updateUserCollection();
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        fieldValueChangeDialog.dismiss();
                    }
                } else if (whatButtonPressed == 1) {

                    String valueEntered = editDialogText.getText().toString();
                    if (valueEntered.length() < 50) {
                        Toast.makeText(ProfileActivity.this, getString(R.string.wrong_input_info_profile_activity), Toast.LENGTH_SHORT).show();
                    } else {
                        db.collection("users").document(currentUser.getUid())
                                .update(whatFieldUpdate, valueEntered); //what you update
                        if (!descriptionUploadMade) {
                            db.collection("users").document(currentUser.getUid())
                                    .update(DESCRIPTION_UPLOAD_MADE, true);
                        }
                        db.collection("users").document(currentUser.getUid())
                                .update(DESCRIPTION_UPLOAD_DATE, new Date());
                        try {
                            updateUserCollection();
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        fieldValueChangeDialog.dismiss();
                    }
                } else if (whatButtonPressed == 4) {
                    String valueEntered = editDialogText.getText().toString();
                    if (valueEntered.length() < 3) {
                        Toast.makeText(ProfileActivity.this, getString(R.string.wrong_input_info_profile_activity), Toast.LENGTH_SHORT).show();
                    } else {
                        db.collection("users").document(currentUser.getUid())
                                .update(whatFieldUpdate, valueEntered); //what you update
                        if (!nameUploadMade) {
                            db.collection("users").document(currentUser.getUid())
                                    .update(NAME_UPLOAD_MADE, true);
                        }
                        db.collection("users").document(currentUser.getUid())
                                .update(NAME_UPLOAD_DATE, new Date());
                        try {
                            updateUserCollection();
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        fieldValueChangeDialog.dismiss();
                    }
                }

            }
        });
    }

    // This function initialises buttons, text views, etc.
    public void initView() {

        profileDescriptionText = findViewById(R.id.own_description_my_profile);
        ageText = findViewById(R.id.own_age_my_profile);
        nameText = findViewById(R.id.name_my_profile);
        titleText = findViewById(R.id.title_alien_profile_textview);
        locationText = findViewById(R.id.own_location_my_profile);
        topicsText = findViewById(R.id.topics_my_profile);
        genderText = findViewById(R.id.own_gender_my_profile);

        firstImageView = findViewById(R.id.imageview_first_image_alien_profile);
        secondImageView = findViewById(R.id.imageview_second_image_alien_profile);
        thirdImageView = findViewById(R.id.imageview_third_image_alien_profile);

        buttonDescriptionChange = findViewById(R.id.description_own_change);
        buttonAgeChange = findViewById(R.id.age_own_change);
        buttonLocationChange = findViewById(R.id.location_own_change);
        buttonNameChange = findViewById(R.id.name_own_change);
        buttonTopicsChange = findViewById(R.id.topics_own_change);
        buttonFirstImageChange = findViewById(R.id.first_image_change);
        buttonSecondImageChange = findViewById(R.id.second_image_change);
        buttonThirdImageChange = findViewById(R.id.third_image_change);
        buttonGenderChange = findViewById(R.id.gender_own_change);

        buttonDescriptionChange.setOnClickListener(this);
        buttonAgeChange.setOnClickListener(this);
        buttonLocationChange.setOnClickListener(this);
        buttonNameChange.setOnClickListener(this);
        buttonTopicsChange.setOnClickListener(this);
        buttonFirstImageChange.setOnClickListener(this);
        buttonSecondImageChange.setOnClickListener(this);
        buttonThirdImageChange.setOnClickListener(this);
        buttonGenderChange.setOnClickListener(this);

        firstImageView.setOnClickListener(this);
        secondImageView.setOnClickListener(this);
        thirdImageView.setOnClickListener(this);

    }

    // This function opens file explorer to choose image
    private void chooseImage(int i) {
        if ((checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            if (i == 1) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            } else if (i == 2) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2);
            } else if (i == 3) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 3);
            }
        }
    }

    // This function check permission for storage
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseImage(1);
            }
        }
    }

    // This function saves chosen photo to ImageView and starts upload of photo on server
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imagePath);
                bitmap = rotateBitmap(bitmap, imagePath);
                firstImageView.setImageBitmap(bitmap);
                uploadImage("userPhotos/" + currentUser.getUid() + "/photoNr1.jpg", firstImageView, FIRST_PHOTO_URI,
                        FIRST_PHOTO_UPLOAD_MADE, FIRST_PHOTO_UPLOAD_DATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imagePath);
                bitmap = rotateBitmap(bitmap, imagePath);
                secondImageView.setImageBitmap(bitmap);
                uploadImage("userPhotos/" + currentUser.getUid() + "/photoNr2.jpg", secondImageView, SECOND_PHOTO_URI,
                        SECOND_PHOTO_UPLOAD_MADE, SECOND_PHOTO_UPLOAD_DATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == 3 && resultCode == RESULT_OK) {
            imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imagePath);
                bitmap = rotateBitmap(bitmap, imagePath);
                thirdImageView.setImageBitmap(bitmap);
                uploadImage("userPhotos/" + currentUser.getUid() + "/photoNr3.jpg", thirdImageView, THIRD_PHOTO_URI,
                        THIRD_PHOTO_UPLOAD_MADE, THIRD_PHOTO_UPLOAD_DATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == 5  ) {

            if (resultCode == RESULT_OK) {
                // location activity for result
                String placeName = data.getStringExtra("placeName");
                String countryName = data.getStringExtra("countryName");
                String countryCode = data.getStringExtra("countryCode");
                LatLng latLngParameters = new LatLng(data.getDoubleExtra("latitude", 0), data.getDoubleExtra("longitude", 0));


                db.collection("users").document(currentUser.getUid())
                        .update(PLACE_NAME_FIELD, placeName);
                db.collection("users").document(currentUser.getUid())
                        .update(COUNTRY_NAME_FIELD, countryName);
                db.collection("users").document(currentUser.getUid())
                        .update(COUNTRY_CODE_FIELD, countryCode);
                db.collection("users").document(currentUser.getUid())
                        .update(LOCATION_FIELD, new GeoPoint(latLngParameters.latitude,latLngParameters.longitude));

                if (!locationUploadMade && placeName.length() > 1 && countryCode.length() > 1 && countryName.length() > 1) {
                    db.collection("users").document(currentUser.getUid())
                            .update(LOCATION_UPLOAD_MADE, true);
                }
                db.collection("users").document(currentUser.getUid()).update(LOCATION_UPLOAD_DATE, new Date());
                try {
                    updateUserCollection();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    // This function uploads photo on Firebase Storage and shows alert dialog of uploading
    private void uploadImage(String nameOfPhoto, ImageView imageView, String photoUri, String wasPhotoUploadMade, String dateOfPhotoUpload) {

        if (imagePath != null) {

            loadingAlertDialog();
            messageAlertView.setText(R.string.dialog_box_upload_photo);
            dialog.show();

            StorageReference ref = storageReference.child(nameOfPhoto);
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream boos = new ByteArrayOutputStream();
            bitmap = scaleDown(bitmap, 1280, true);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, boos);
            byte[] data = boos.toByteArray();

            //UploadImage
            UploadTask uploadTask = ref.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    dialog.dismiss();
                    Toast.makeText(ProfileActivity.this, getString(R.string.upload_failed_profile_activity), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            if (photoUri.equals("firstPhotoUri")) {
                                firstPhotoUri = uri.toString();
                            } else if (photoUri.equals("secondPhotoUri")) {
                                secondPhotoUri = uri.toString();
                            } else if (photoUri.equals("thirdPhotoUri")) {
                                thirdPhotoUri = uri.toString();
                            }

                            Map<String, Object> userCompletion = new HashMap<>();
                            userCompletion.put(photoUri, uri.toString());
                            userCompletion.put(wasPhotoUploadMade, true);
                            userCompletion.put(dateOfPhotoUpload, new Date());
                            db.collection("users").document(currentUser.getUid()).set(userCompletion, SetOptions.merge());
                        }
                    });
                    dialog.dismiss();
                    Toast.makeText(ProfileActivity.this, getString(R.string.upload_finished_photo_profile_activity), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot
                            .getTotalByteCount());
                    String a = getString(R.string.upload_progress_1_profile_activity) + (int) progress +  getString(R.string.upload_progress_2_profile_activity);
                    messageAlertView.setText(a);
                }
            });

        }
    }

    // This function starts functions correlated with certain button
    @Override
    public void onClick(View view) {
        int daysToChangeName = 7;
        int daysToChangeAge = 30;
        int daysToChangePhoto = 7;
        int daysToChangeDescription = 7;
        int daysToChangeGender = 30;
        int daysToChangeLocation = 0;
        long nrOfDaysSinceChange;
        long dateDiff;

        switch (view.getId()) {
            case R.id.description_own_change:
                dateDiff = currentDate.getTime() - descriptionUploadDate.getTime();
                nrOfDaysSinceChange = TimeUnit.DAYS.convert(dateDiff, TimeUnit.MILLISECONDS);
                if ( nrOfDaysSinceChange >= daysToChangeDescription) {
                    descriptionAgeNameChangeDialog(DESCRIPTION_FIELD, getString(R.string.your_description_text_profile_activity));
                } else {
                    timeOfChangeAlertDialog(nrOfDaysSinceChange, daysToChangeDescription);
                }
                whatButtonPressed = 1;
                break;
            case R.id.age_own_change:
                dateDiff = currentDate.getTime() - ageUploadDate.getTime();
                nrOfDaysSinceChange = TimeUnit.DAYS.convert(dateDiff, TimeUnit.MILLISECONDS);
                Log.d("DAYYYY", String.valueOf(nrOfDaysSinceChange));
                if (nrOfDaysSinceChange > daysToChangeAge) {
                    descriptionAgeNameChangeDialog(AGE_FIELD, getString(R.string.your_age_text_profile_activity));
                } else {
                    timeOfChangeAlertDialog(nrOfDaysSinceChange, daysToChangeAge);
                }
                whatButtonPressed = 2;
                break;
            case R.id.location_own_change:
                dateDiff = currentDate.getTime() - locationUploadDate.getTime();
                nrOfDaysSinceChange = TimeUnit.DAYS.convert(dateDiff, TimeUnit.MILLISECONDS);
                Log.d("DAYYYY", String.valueOf(nrOfDaysSinceChange));
                if (nrOfDaysSinceChange >= daysToChangeLocation) {
                    locationChange();
                } else {
                    timeOfChangeAlertDialog(nrOfDaysSinceChange, daysToChangeLocation);
                }
                whatButtonPressed = 3;
                break;
            case R.id.name_own_change:
                dateDiff = currentDate.getTime() - nameUploadDate.getTime();
                nrOfDaysSinceChange = TimeUnit.DAYS.convert(dateDiff, TimeUnit.MILLISECONDS);
                if (nrOfDaysSinceChange > daysToChangeName) {
                    descriptionAgeNameChangeDialog(NAME_FIELD, getString(R.string.your_name_text_profile_activity));
                } else {
                    timeOfChangeAlertDialog(nrOfDaysSinceChange, daysToChangeName);
                }
                whatButtonPressed = 4;
                break;
            case R.id.first_image_change:
                dateDiff = currentDate.getTime() - firstPhotoUploadDate.getTime();
                nrOfDaysSinceChange = TimeUnit.DAYS.convert(dateDiff, TimeUnit.MILLISECONDS);
                if (nrOfDaysSinceChange > daysToChangePhoto) {
                    chooseImage(1);
                } else {
                    timeOfChangeAlertDialog(nrOfDaysSinceChange, daysToChangePhoto);
                }
                whatButtonPressed = 5;
                break;
            case R.id.second_image_change:
                dateDiff = currentDate.getTime() - secondPhotoUploadDate.getTime();
                nrOfDaysSinceChange = TimeUnit.DAYS.convert(dateDiff, TimeUnit.MILLISECONDS);
                if (nrOfDaysSinceChange > daysToChangePhoto) {
                    chooseImage(2);
                } else {
                    timeOfChangeAlertDialog(nrOfDaysSinceChange, daysToChangeName);
                }
                whatButtonPressed = 6;
                break;
            case R.id.third_image_change:
                dateDiff = currentDate.getTime() - thirdPhotoUploadDate.getTime();
                nrOfDaysSinceChange = TimeUnit.DAYS.convert(dateDiff, TimeUnit.MILLISECONDS);
                if (nrOfDaysSinceChange > daysToChangePhoto) {
                    chooseImage(3);
                } else {
                    timeOfChangeAlertDialog(nrOfDaysSinceChange, daysToChangeName);
                }
                whatButtonPressed = 7;
                break;
            case R.id.topics_own_change:
                pickUpTopicsDialog();
                whatButtonPressed = 8;
                break;
            case R.id.imageview_first_image_alien_profile:
                photoShowDialog(firstPhotoUri);
                whatButtonPressed = 9;
                break;
            case R.id.imageview_second_image_alien_profile:
                photoShowDialog(secondPhotoUri);
                whatButtonPressed = 10;
                break;
            case R.id.imageview_third_image_alien_profile:
                photoShowDialog(thirdPhotoUri);
                whatButtonPressed = 11;
                break;
            case R.id.gender_own_change:
                dateDiff = currentDate.getTime() - genderUploadDate.getTime();
                nrOfDaysSinceChange = TimeUnit.DAYS.convert(dateDiff, TimeUnit.MILLISECONDS);
                if (nrOfDaysSinceChange > daysToChangeGender) {
                    genderChangeDialog();
                } else {
                    timeOfChangeAlertDialog(nrOfDaysSinceChange, daysToChangeGender);
                }
                whatButtonPressed = 12;
                break;
            default:
                break;
        }
    }

    // This function let's user change gender
    public void genderChangeDialog() {

        LayoutInflater inflaterDialog = LayoutInflater.from(this);
        View dialogView = inflaterDialog.inflate(R.layout.dialog_gender_change, null);

        RadioGroup genderGroup = dialogView.findViewById(R.id.gender_pick_radio_group);
        int maleID = 20;
        int femaleID = 21;
        RadioButton maleButton = dialogView.findViewById(R.id.male_radio);
        maleButton.setId(maleID);
        RadioButton femaleButton = dialogView.findViewById(R.id.female_radio);
        femaleButton.setId(femaleID);

        AlertDialog genderChangeDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.choose_gender_box_profile_activity)
                .setView(dialogView)  // What to use in dialog box
                .setNegativeButton(R.string.no_text_dialog_boxes, null)
                .setPositiveButton("OK", null)
                .show();

        genderChangeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedID = genderGroup.getCheckedRadioButtonId();
                switch (selectedID) {
                    case 20:
                        db.collection("users").document(currentUser.getUid())
                                .update(GENDER_FIELD, false);
                        if (!genderUploadMade) {
                            db.collection("users").document(currentUser.getUid())
                                    .update(GENDER_UPLOAD_MADE, true);
                        }
                        db.collection("users").document(currentUser.getUid())
                                .update(GENDER_UPLOAD_DATE, new Date());
                        break;
                    case 21:
                        db.collection("users").document(currentUser.getUid())
                                .update(GENDER_FIELD, true);
                        if (!genderUploadMade) {
                            db.collection("users").document(currentUser.getUid())
                                    .update(GENDER_UPLOAD_MADE, true);
                        }
                        db.collection("users").document(currentUser.getUid())
                                .update(GENDER_UPLOAD_DATE, new Date());
                        break;
                    default:
                        break;
                }

                try {
                    updateUserCollection();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                genderChangeDialog.dismiss();

            }
        });
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

    //This function let's You pick up Your favorite topics
    public void pickUpTopicsDialog() {
        LayoutInflater inflaterDialog = LayoutInflater.from(this);
        View topicsView = inflaterDialog.inflate(R.layout.dialog_topics_change, null);
        RecyclerView topicsRecycler = topicsView.findViewById(R.id.picked_topics_own_profile);

        // Load Main Topics List and set the Recyclerview inside the DialogBox
        Query queryListOfTopics = db.collection("topics").orderBy("topicTitle");

        FirestoreRecyclerOptions<Topic> options = new FirestoreRecyclerOptions.Builder<Topic>()
                .setQuery(queryListOfTopics, Topic.class)
                .setLifecycleOwner(this)
                .build();

        adapterTopics = new TopicsAdapter(options);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        topicsRecycler.setLayoutManager(layoutManager);
        topicsRecycler.setAdapter(adapterTopics);

        AlertDialog topicsChangeDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pickup_topics_title_profile_activity)
                .setView(topicsView)  // What to use in dialog box
                .setNegativeButton(R.string.no_text_dialog_boxes, null)
                .setPositiveButton("OK", null)
                .show();

        // Create a list of chosen topics and prevent from choosing more than 10
        adapterTopics.startListening();
        ArrayList<String> topics = new ArrayList<>();
        adapterTopics.setOnItemCLickListener(new TopicsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(boolean isFavorite, int position, String topicTitle, CompoundButton cb) {
                if (topics.size() < 5) {
                    if (!topics.contains(topicTitle)){
                        topics.add(topicTitle);
                    } else {
                        topics.remove(topicTitle);
                        cb.setChecked(false);
                    }
                } else {
                    topics.remove(topicTitle);
                    cb.setChecked(false);
                    Toast.makeText(ProfileActivity.this, getString(R.string.topics_warning_max_profile_activity), Toast.LENGTH_SHORT).show();
                }
            }
        });

        topicsChangeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (topics.size() < 2) {
                    int howManyTopicsLeft = 2 - topics.size();
                    String a = getString(R.string.more_topics_info_1_profile_activity) + howManyTopicsLeft + getString(R.string.more_topics_info_2_profile_activity);
                    Toast.makeText(ProfileActivity.this, a, Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "Clicked positive!");
                    adapterTopics.stopListening();
                    // Delete the old array
                    db.collection("users").document(currentUser.getUid())
                            .update(TOPICS_ARRAY, FieldValue.delete());
                    // Update the chosenTopicsArray in User document
                    for (int i = 0; i < topics.size(); i++) {
                        db.collection("users").document(currentUser.getUid())
                                .update(TOPICS_ARRAY, arrayUnion(topics.get(i)));
                    }
                    if (!topicsUploadMade) {
                        db.collection("users").document(currentUser.getUid())
                                .update(TOPICS_UPLOAD_MADE, true);
                    }
                    db.collection("users").document(currentUser.getUid())
                            .update(TOPICS_UPLOAD_DATE, new Date());

                    try {
                        updateUserCollection();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    topicsChangeDialog.dismiss();
                }
            }
       });
    }

    // this function scales down uploaded images sizes to save server space
    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {

        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        if (ratio >= 1.0) {

            return realImage;
        } else {
            return Bitmap.createScaledBitmap(realImage, width,
                    height, filter);
        }

    }

    // This function properly rotates added image from gallery
    public Bitmap rotateBitmap(Bitmap bitmap, Uri imgPath) {
        InputStream in;
        ExifInterface exifInterface = null;
        Matrix mat = new Matrix();
        try {
            in = getContentResolver().openInputStream(imgPath);
            exifInterface = new ExifInterface(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                mat.postRotate(90);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
            case ExifInterface.ORIENTATION_ROTATE_180:
                mat.postRotate(180);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
            case ExifInterface.ORIENTATION_ROTATE_270:
                mat.postRotate(270);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
            default:
                return bitmap;
        }
    }

    // This function shows images in bigger format
    public void photoShowDialog(String imagePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogAlertView = inflater.inflate(R.layout.photo_full_screen, null);
        ImageView imageFullView = dialogAlertView.findViewById(R.id.full_screen_photo_show);
        Glide.with(getApplicationContext()).load(imagePath).into(imageFullView);
        builder.setView(dialogAlertView);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    // This function tells user that time for change hasn't been reached yet.
    public void timeOfChangeAlertDialog(long nrOfDaysSinceChange, long daysLimit) {
        LayoutInflater inflaterDialog = LayoutInflater.from(this);
        View alertDateDialogView = inflaterDialog.inflate(R.layout.dialog_date_alert, null);
        TextView dateAlertTitle = alertDateDialogView.findViewById(R.id.title_of_date_alert_box);
        TextView dateAlertText = alertDateDialogView.findViewById(R.id.text_of_date_alert_box);
        dateAlertTitle.setText(getString(R.string.date_alert_text));
        dateAlertText.setText(String.valueOf(daysLimit - nrOfDaysSinceChange));

        AlertDialog alertDateDialog = new AlertDialog.Builder(this)
                .setView(alertDateDialogView)  // What to use in dialog box
                .setPositiveButton("OK", null)
                .show();

        alertDateDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDateDialog.dismiss();
            }
        });

    }

    // This function send to LocationActivity to set location
    public void locationChange() {
        int LAUNCH_LOCATION_ACTIVITY = 5;
        Intent i = new Intent(this, LocationActivity.class);
        startActivityForResult(i, LAUNCH_LOCATION_ACTIVITY);
    }

    // This function adds User in Search collection (UserID, User Gender, User Location, User Age, User Topics)
    public void addUserInSearchCollection(String userID, int userAge, boolean isUserFemale, String countryCode, GeoPoint userLocation,
                                                 boolean userWantBlindDates, ArrayList<String> userTopics) {

        String gender;
        if (isUserFemale) {
            gender = "female";
        } else {
            gender = "male";
        }
        String age = Integer.toString(userAge);

        Map<String, Object> addToSearchCollection = new HashMap<>();
        addToSearchCollection.put("locationOfUser", userLocation);
        addToSearchCollection.put("idOfUser", userID);
        addToSearchCollection.put("userWantsBlindDates", userWantBlindDates);
        addToSearchCollection.put("topicsList", userTopics);

        Map<String, Object> addNrOfUsersField = new HashMap<>();
        addNrOfUsersField.put(NR_OF_USERS_TO_SEARCH,1);

        // SearchID to specific country
        db.collection("search").document(countryCode).collection("gender").document(gender)
                .collection("age").document(age).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {

                        db.collection("search").document(countryCode).collection("gender").document(gender)
                                .collection("age").document(age).collection("users").add(addToSearchCollection).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                                searchIDCountry = documentReference.getId();

                                db.collection("users").document(currentUser.getUid())
                                        .update(SEARCH_ID_COUNTRY, searchIDCountry);

                                db.collection("search").document(countryCode).collection("gender").document(gender)
                                        .collection("age").document(age).update(NR_OF_USERS_TO_SEARCH, FieldValue.increment(1));

                            }
                        });

                    } else {
                        Log.d("LOGGER", "No such document");
                        db.collection("search").document(countryCode).collection("gender").document(gender)
                                .collection("age").document(age).set(addNrOfUsersField,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                    db.collection("search").document(countryCode).collection("gender").document(gender)
                                            .collection("age").document(age).collection("users").add(addToSearchCollection).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {

                                            searchIDCountry = documentReference.getId();

                                            db.collection("users").document(currentUser.getUid())
                                                    .update(SEARCH_ID_COUNTRY, searchIDCountry);

                                        }
                                    });
                            }
                        });
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });

        // SearchID to world
        db.collection("search").document("world").collection("gender").document(gender)
                .collection("age").document(age).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {

                        db.collection("search").document("world").collection("gender").document(gender)
                                .collection("age").document(age).collection("users").add(addToSearchCollection).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                                searchIDWorld = documentReference.getId();

                                db.collection("users").document(currentUser.getUid())
                                        .update(SEARCH_ID_WORLD, searchIDWorld);

                                db.collection("search").document("world").collection("gender").document(gender)
                                        .collection("age").document(age).update(NR_OF_USERS_TO_SEARCH, FieldValue.increment(1));

                            }
                        });

                    } else {
                        Log.d("LOGGER", "No such document");
                        db.collection("search").document("world").collection("gender").document(gender)
                                .collection("age").document(age).set(addNrOfUsersField,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                db.collection("search").document("world").collection("gender").document(gender)
                                        .collection("age").document(age).collection("users").add(addToSearchCollection).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {

                                        searchIDWorld = documentReference.getId();

                                        db.collection("users").document(currentUser.getUid())
                                                .update(SEARCH_ID_COUNTRY, searchIDWorld);

                                    }
                                });
                            }
                        });
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });

    }

}