package pl.makorz.discussed.Controllers;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import pl.makorz.discussed.Controllers.Functions.*;
import pl.makorz.discussed.R;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ProfileActivity";

    public static final String AGE_FIELD = "ageOfUser";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String NAME_FIELD = "displayName";
    public static final String LOCATION_FIELD = "location";
    public static final String TOPICS_FIELD = "chosenTopics";
    public static final String FIRST_PHOTO_URI = "firstPhotoUri";
    public static final String SECOND_PHOTO_URI = "secondPhotoUri";
    public static final String THIRD_PHOTO_URI = "thirdPhotoUri";
    public static final String FIRST_PHOTO_UPLOAD_MADE = "firstPhotoUploadMade";
    public static final String SECOND_PHOTO_UPLOAD_MADE = "secondPhotoUploadMade";
    public static final String THIRD_PHOTO_UPLOAD_MADE = "thirdPhotoUploadMade";
    public static final String FIRST_PHOTO_UPLOAD_DATE = "firstPhotoUploadDate";
    public static final String SECOND_PHOTO_UPLOAD_DATE = "secondPhotoUploadDate";
    public static final String THIRD_PHOTO_UPLOAD_DATE = "thirdPhotoUploadDate";

    public static int whatButtonPressed = -1;
    private Uri imagePath;

    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    private TextView profileDescriptionText, ageText, locationText, nameText, titleText;
    public Button buttonDescriptionChange, buttonAgeChange, buttonLocationChange, buttonNameChange, buttonTopicsChange, buttonFirstImageChange,
            buttonSecondImageChange, buttonThirdImageChange;
    private ImageView firstImageView, secondImageView, thirdImageView, imageFullView;
    private Boolean firstPhotoUploadMade, secondPhotoUploadMade, thirdPhotoUploadMade, locationUploadMade, descriptionUploadMade, ageUploadMade, topicsUploadMade;
    private AlertDialog dialog;
    private TextView messageAlertView;
    private String firstPhotoUri, secondPhotoUri, thirdPhotoUri;

    // Main function of creating activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Alert of loading data of profile from server
        loadingAlertDialog();
        messageAlertView.setText(R.string.alert_dialog_loading_profile);
        initView();
        // Loading data from server in second thread
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            updateUserCollection();
                            android.os.SystemClock.sleep(1000);
                            dialog.dismiss();
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                });
            }
        }.start();

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

                        String age = Integer.toString(document.getDouble(AGE_FIELD).intValue());
                        ageText.setText(age);

                        double latitude = document.getGeoPoint(LOCATION_FIELD).getLatitude();
                        double longitude = document.getGeoPoint(LOCATION_FIELD).getLongitude();
                        String location = "[ " + latitude + ", " + longitude + " ]";
                        locationText.setText(location);

                        nameText.setText(document.getString(NAME_FIELD));
                        profileDescriptionText.setText(document.getString(DESCRIPTION_FIELD));
                        String title = "Welcome " + document.getString(NAME_FIELD) + " to Your profile!";
                        titleText.setText(title);

                        firstPhotoUri = document.getString(FIRST_PHOTO_URI);
                        secondPhotoUri = document.getString(SECOND_PHOTO_URI);
                        thirdPhotoUri = document.getString(THIRD_PHOTO_URI);

                        firstPhotoUploadMade = document.getBoolean(FIRST_PHOTO_UPLOAD_MADE);
                        secondPhotoUploadMade = document.getBoolean(SECOND_PHOTO_UPLOAD_MADE);
                        thirdPhotoUploadMade = document.getBoolean(THIRD_PHOTO_UPLOAD_MADE);

                        if (firstPhotoUploadMade) {
                            Glide.with(getApplicationContext()).load(firstPhotoUri).into(firstImageView);
                        }
                        if (secondPhotoUploadMade) {
                            Glide.with(getApplicationContext()).load(secondPhotoUri).into(secondImageView);
                        }
                        if (thirdPhotoUploadMade) {
                            Glide.with(getApplicationContext()).load(thirdPhotoUri).into(thirdImageView);
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

    // This functions show dialog boxes where user can input specific data
    private void showAlertDialog(String whatFieldUpdate, String titleDialogBox) {

        // Layout dialog box
        LayoutInflater inflaterDialog = LayoutInflater.from(this);
        View dialogView = inflaterDialog.inflate(R.layout.dialog_description_change, null);
        EditText editDialogText = dialogView.findViewById(R.id.dialog_description_edit_text);
        // Filter input
        if (whatFieldUpdate.equals(AGE_FIELD)) {
            editDialogText.setHint("Enter Your age (Between 18 and 115)");
            editDialogText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if (whatFieldUpdate.equals(DESCRIPTION_FIELD)) {
            editDialogText.setHint("Describe Yourself (Between 50-500 letters)");
            DescriptionFilter descriptionFilter = new DescriptionFilter(ProfileActivity.this);
            InputFilter lengthFilter = new InputFilter.LengthFilter(500);
            editDialogText.setFilters(new InputFilter[]{lengthFilter});
            editDialogText.setFilters(new DescriptionFilter[]{descriptionFilter});
        } else if (whatFieldUpdate.equals(NAME_FIELD)) {
            editDialogText.setHint("Enter Your name (Between 3-35 letters)");
            InputFilter lengthFilter = new InputFilter.LengthFilter(35);
            editDialogText.setFilters(new InputFilter[]{lengthFilter});
        }

        AlertDialog fieldValueChangeDialog = new AlertDialog.Builder(this)
                .setTitle(titleDialogBox)
                .setView(dialogView)  // What to use in dialog box
                .setNegativeButton("Abort!", null)
                .setPositiveButton("OK", null)
                .show();

        fieldValueChangeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Clicked positive!");
                if (whatButtonPressed == 2) {
                    int valueEntered = Integer.parseInt(editDialogText.getText().toString());
                    if (valueEntered < 18 || valueEntered > 115) {
                        Toast.makeText(ProfileActivity.this, "Set age from 18 to 115 years old!", Toast.LENGTH_SHORT).show();
                    } else {

                        db.collection("users").document(currentUser.getUid())
                                .update(whatFieldUpdate, valueEntered);//what you update
                        try {
                            updateUserCollection();
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        fieldValueChangeDialog.dismiss();
                    }
                } else if (whatButtonPressed == 1) {

                    String valueEntered = editDialogText.getText().toString();
                    if (valueEntered.isEmpty()) {
                        Toast.makeText(ProfileActivity.this, "You need to type something!!!", Toast.LENGTH_SHORT).show();
                    } else {
                        db.collection("users").document(currentUser.getUid())
                                .update(whatFieldUpdate, valueEntered); //what you update
                        try {
                            updateUserCollection();
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        fieldValueChangeDialog.dismiss();
                    }
                } else if (whatButtonPressed == 4) {
                    String valueEntered = editDialogText.getText().toString();
                    if (valueEntered.isEmpty()) {
                        Toast.makeText(ProfileActivity.this, "You need to type something!!!", Toast.LENGTH_SHORT).show();
                    } else {
                        db.collection("users").document(currentUser.getUid())
                                .update(whatFieldUpdate, valueEntered); //what you update
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

    // this function initialises buttons, text views, etc.
    public void initView() {

        profileDescriptionText = findViewById(R.id.own_description_my_profile);
        ageText = findViewById(R.id.own_age_my_profile);
        nameText = findViewById(R.id.name_my_profile);
        titleText = findViewById(R.id.title_my_profile);
        locationText = findViewById(R.id.own_location_my_profile);

        firstImageView = findViewById(R.id.imageview_first_image_own_profile);
        secondImageView = findViewById(R.id.imageview_second_image_own_profile);
        thirdImageView = findViewById(R.id.imageview_third_image_own_profile);

        buttonDescriptionChange = findViewById(R.id.description_own_change);
        buttonAgeChange = findViewById(R.id.age_own_change);
        buttonLocationChange = findViewById(R.id.location_own_change);
        buttonNameChange = findViewById(R.id.name_own_change);
        buttonTopicsChange = findViewById(R.id.topics_own_change);
        buttonFirstImageChange = findViewById(R.id.first_image_change);
        buttonSecondImageChange = findViewById(R.id.second_image_change);
        buttonThirdImageChange = findViewById(R.id.third_image_change);

        buttonDescriptionChange.setOnClickListener(this);
        buttonAgeChange.setOnClickListener(this);
        buttonLocationChange.setOnClickListener(this);
        buttonNameChange.setOnClickListener(this);
        buttonTopicsChange.setOnClickListener(this);
        buttonFirstImageChange.setOnClickListener(this);
        buttonSecondImageChange.setOnClickListener(this);
        buttonThirdImageChange.setOnClickListener(this);
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
        }
    }

    // This function uploads photo on Firebase Storage and shows alert dialog of uploading
    private void uploadImage(String nameOfPhoto, ImageView imageView, String userParameter1, String userParameter2, String userParameter3) {

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
                    Toast.makeText(ProfileActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            Map<String, Object> userCompletion = new HashMap<>();
                            userCompletion.put(userParameter1, uri.toString());
                            userCompletion.put(userParameter2, true);
                            userCompletion.put(userParameter3, new Date());
                            db.collection("users").document(currentUser.getUid()).set(userCompletion, SetOptions.merge());
                        }
                    });
                    dialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "Upload finished!", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot
                            .getTotalByteCount());
                    messageAlertView.setText("Uploaded " + (int) progress + "%");
                }
            });

        }
    }

    // This function starts functions correlated with certain button
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.description_own_change:
                showAlertDialog(DESCRIPTION_FIELD, "Your Description");
                whatButtonPressed = 1;
                break;
            case R.id.age_own_change:
                showAlertDialog(AGE_FIELD, "Your Age");
                whatButtonPressed = 2;
                break;
            case R.id.location_own_change:
                showAlertDialog(LOCATION_FIELD, "Your Location");
                whatButtonPressed = 3;
                break;
            case R.id.name_own_change:
                showAlertDialog(NAME_FIELD, "Your Name");
                whatButtonPressed = 4;
                break;
            case R.id.first_image_change:
                chooseImage(1);
                whatButtonPressed = 5;
                break;
            case R.id.second_image_change:
                chooseImage(2);
                whatButtonPressed = 6;
                break;
            case R.id.third_image_change:
                chooseImage(3);
                whatButtonPressed = 7;
                break;
            case R.id.topics_own_change:
                pickUpTopics();
                whatButtonPressed = 8;
                break;
            case R.id.imageview_first_image_own_profile:
                photoShowDialog(firstPhotoUri);
                whatButtonPressed = 9;
                break;
            case R.id.imageview_second_image_own_profile:
                photoShowDialog(secondPhotoUri);
                whatButtonPressed = 10;
                break;
            case R.id.imageview_third_image_own_profile:
                photoShowDialog(thirdPhotoUri);
                whatButtonPressed = 11;
                break;
            default:
                break;
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
        messageAlertView.setText("Profile info loading...");
        dialog = builder.create();
        dialog.show();
    }

    //This function let's You pick up Your favorite topics
    public void pickUpTopics() {
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
        imageFullView = dialogAlertView.findViewById(R.id.full_screen_photo_show);
        Glide.with(getApplicationContext()).load(imagePath).into(imageFullView);
        builder.setView(dialogAlertView);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

}