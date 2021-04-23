package pl.makorz.discussed;

import android.app.ActionBar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.LatLng;

import java.text.DecimalFormat;

import pl.makorz.discussed.Functions.DescriptionFilter;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ProfileActivity";

    public static final String AGE_FIELD = "ageOfUser";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String NAME_FIELD = "displayName";
    public static final String LOCATION_FIELD = "location";
    public static int whatButtonPressed = -1;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();;
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private TextView profileDescriptionText, ageText, locationText, nameText, titleText;
    Button buttonDescriptionChange, buttonAgeChange, buttonLocationChange, buttonNameChange;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //ActionBar actionBar = getActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
        updateUserCollection();

    }

    private void updateUserCollection() {
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
                    } else {
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });
    }

    private void showAlertDialog(String whatFieldUpdate, String titleDialogBox) {

        // Layout dialog boxa
        LayoutInflater inflaterDialog = LayoutInflater.from(this);
        View dialogView = inflaterDialog.inflate(R.layout.dialog_description_change, null);
        EditText editDialogText = dialogView.findViewById(R.id.dialog_description_edit_text);
        // Filter input
        DescriptionFilter descriptionFilter = new DescriptionFilter(ProfileActivity.this);
        InputFilter lengthFilter = new InputFilter.LengthFilter(50);
        editDialogText.setFilters(new InputFilter[] {lengthFilter});
        editDialogText.setFilters(new DescriptionFilter[] {descriptionFilter});

        AlertDialog fieldValueChangeDialog = new AlertDialog.Builder(this)
                .setTitle(titleDialogBox)
                .setView(dialogView)  // What to use in dialog box
                .setNegativeButton("Abort!",null)
                .setPositiveButton("OK", null)
                .show();

        fieldValueChangeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Clicked positive!");
                if (whatButtonPressed == 2) {
                    int valueEntered = Integer.parseInt(editDialogText.getText().toString());
                    if (valueEntered < 12 || valueEntered >= 100) {
                        Toast.makeText(ProfileActivity.this, "Give normal age!!!", Toast.LENGTH_SHORT).show();
                    } else {
                        db.collection("users").document(currentUser.getUid())
                                .update(whatFieldUpdate, valueEntered); //what you update
                        updateUserCollection();
                        fieldValueChangeDialog.dismiss();
                    }
                } else {
                    String valueEntered = editDialogText.getText().toString();
                    if (valueEntered.isEmpty()) {
                        Toast.makeText(ProfileActivity.this, "You need to type something!!!", Toast.LENGTH_SHORT).show();
                    } else {
                        //profileDescriptionText.setText(valueEntered);
                        // Upadte firestore with new description, we take certain uid of user from firestore
                        // db = FirebaseFirestore.getInstance();
                        db.collection("users").document(currentUser.getUid())
                                .update(whatFieldUpdate, valueEntered); //what you update

                        updateUserCollection();
                        fieldValueChangeDialog.dismiss();
                    }
                }

            }
        });
    }

    public void initView() {

        profileDescriptionText = findViewById(R.id.own_description_my_profile);
        ageText = findViewById(R.id.own_age_my_profile);
        nameText = findViewById(R.id.name_my_profile);
        titleText = findViewById(R.id.title_my_profile);
        locationText = findViewById(R.id.own_location_my_profile);

        buttonDescriptionChange = (Button) findViewById(R.id.description_own_change);
        buttonAgeChange = (Button) findViewById(R.id.age_own_change);
        buttonLocationChange = (Button) findViewById(R.id.location_own_change);
        buttonNameChange = (Button) findViewById(R.id.name_own_change);

        buttonDescriptionChange.setOnClickListener(this);
        buttonAgeChange.setOnClickListener(this);
        buttonLocationChange.setOnClickListener(this);
        buttonNameChange.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.description_own_change:
                showAlertDialog(DESCRIPTION_FIELD,"Update Your Description");
                whatButtonPressed = 1;
                break;
            case R.id.age_own_change:
                showAlertDialog(AGE_FIELD, "Your Age");
                whatButtonPressed = 2;
                break;
            case R.id.location_own_change:
                showAlertDialog(LOCATION_FIELD,"Update Your Location");
                whatButtonPressed = 3;
                break;
            case R.id.name_own_change:
                showAlertDialog(NAME_FIELD,"Update Your Name");
                whatButtonPressed = 4;
                break;
            default:
                break;
        }




    }
}