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
import pl.makorz.discussed.Functions.DescriptionFilter;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    TextView profileDescriptionText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ActionBar actionBar = getActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        profileDescriptionText = findViewById(R.id.own_description_my_profile);


        // Download document of current user, to retrieve actual info to profilel view
        DocumentReference docRef = db.collection("users").document(currentUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        Log.i("LOGGER","First "+ document.getString("description"));
                        profileDescriptionText.setText(document.getString("description"));
                    } else {
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });

        Button buttonDescriptionChange = (Button) findViewById(R.id.description_own_change);
        buttonDescriptionChange.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                showAlertDialog();

            }
        });

    }


    private void showAlertDialog() {

        // Layout dialog boxa
        LayoutInflater inflaterDialog = LayoutInflater.from(this);
        View dialogView = inflaterDialog.inflate(R.layout.dialog_descrition_change, null);
        EditText editDialogText = dialogView.findViewById(R.id.dialog_description_edit_text);
        // Filter input
        DescriptionFilter descriptionFilter = new DescriptionFilter(ProfileActivity.this);
        InputFilter lengthFilter = new InputFilter.LengthFilter(50);
        editDialogText.setFilters(new InputFilter[] {lengthFilter});
        editDialogText.setFilters(new DescriptionFilter[] {descriptionFilter});

        AlertDialog descriptionChangeDialog = new AlertDialog.Builder(this)
                .setTitle("Update Your info Bar")
                .setView(dialogView)  // What to use in dialog box
                .setNegativeButton("Abort!",null)
                .setPositiveButton("OK", null)
                .show();

        descriptionChangeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Clicked positive!");
                String descriptionEntered = editDialogText.getText().toString();
                if (descriptionEntered.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Cannot enter empty text", Toast.LENGTH_SHORT).show();
                } else {
                    profileDescriptionText.setText(descriptionEntered);
                    // Upadte firestore with new description, we take certain uid of user from firestore
                    //db = FirebaseFirestore.getInstance();
                    db.collection("users").document(currentUser.getUid())
                            .update("description", descriptionEntered); //what you update
                    descriptionChangeDialog.dismiss();
                }

            }
        });
    }


}