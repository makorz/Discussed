package pl.makorz.discussed.Controllers.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import pl.makorz.discussed.Controllers.LoginActivity;
import pl.makorz.discussed.R;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SettingsActivity";

    private static final String USER_PREMIUM = "premium";
    private static final String USER_SEND_GOODJOB = "sendGoodJob";

    public Button buttonGoodJob, buttonDonate, buttonLogout, buttonDeleteAccount;
    public TextView goodJobText;
    public int whatButtonPressed = -1;
    private boolean premium, sendGoodJob;
    private AlertDialog dialog;
    LinearLayout settingsLayout;

    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        settingsLayout = (LinearLayout) inflater.inflate(R.layout.fragment_settings, container, false);
        mAuth = FirebaseAuth.getInstance();

        initView(settingsLayout);

        // Get info if user send already goodJob or has Premium
        db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {

                        sendGoodJob = document.getBoolean(USER_SEND_GOODJOB);
                        premium = document.getBoolean(USER_PREMIUM);

                        if (sendGoodJob) {
                            buttonGoodJob.setEnabled(false);
                            goodJobText.setText("Thanks again for good vibes!");
                        } else {
                            buttonGoodJob.setEnabled(true);
                            goodJobText.setText(R.string.good_job_text_view_settings_tab);
                        }

                    } else {
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });


        return settingsLayout;
    }

    // This function initialises buttons, text views, etc.
    public void initView(LinearLayout layout) {

        buttonLogout = layout.findViewById(R.id.logout_button_settings_tab);
        buttonDonate = layout.findViewById(R.id.donate_button_settings_tab);
        buttonGoodJob = layout.findViewById(R.id.goodJob_button_settings_tab);
        buttonDeleteAccount = layout.findViewById(R.id.deleteAccount_button_settings_tab);
        goodJobText = layout.findViewById(R.id.goodJob_textview_settings_tab);

        buttonLogout.setOnClickListener(this);
        buttonDonate.setOnClickListener(this);
        buttonDonate.setEnabled(false);
        buttonGoodJob.setEnabled(false);
        buttonGoodJob.setOnClickListener(this);
        buttonDeleteAccount.setOnClickListener(this);

    }

    // This function starts functions correlated with certain button
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.goodJob_button_settings_tab:
                db.collection("users").document(currentUser.getUid()).update(USER_SEND_GOODJOB, true);
                db.collection("appInfo").document("informations").update("nrOfGoodJobs", FieldValue.increment(1));
                Toast.makeText(getContext(), "~~~~ Thank You!!! ~~~~", Toast.LENGTH_LONG).show();
                buttonGoodJob.setEnabled(false);
                whatButtonPressed = 1;
                break;
            case R.id.donate_button_settings_tab:
                // Go to payments for premium and donation !!!!!!!!!!!!!!!
                whatButtonPressed = 2;
                break;
            case R.id.logout_button_settings_tab:
                mAuth.signOut();
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                Intent logoutIntent = new Intent(getActivity(), LoginActivity.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logoutIntent);
                whatButtonPressed = 3;
                break;
            case R.id.deleteAccount_button_settings_tab:
                deleteAccountAlertDialog();
                whatButtonPressed = 4;
                break;
            default:
                break;
        }
    }

    // This function tells user that time for change hasn't been reached yet.
    public void deleteAccountAlertDialog() {
        LayoutInflater inflaterDialog = LayoutInflater.from(getContext());
        View deleteAccountDialogView = inflaterDialog.inflate(R.layout.dialog_localisation_not_turned_on, null);
        TextView deleteAccountAlertTitle = deleteAccountDialogView.findViewById(R.id.title_location_not_turned_on);
        deleteAccountAlertTitle.setText("ARE YOU SURE YOU WANT DELETE YOUR PRECIOUS ACCOUNT???");
        deleteAccountAlertTitle.setTextColor(Color.RED);

        AlertDialog deleteAccountDialog = new AlertDialog.Builder(getContext())
                .setView(deleteAccountDialogView)  // What to use in dialog box
                .setNegativeButton(R.string.no_text_dialog_boxes, null)
                .setPositiveButton(R.string.yes_text_dialog_boxes, null)
                .show();

        deleteAccountDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentUser.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    db.collection("users").document(currentUser.getUid()).delete();
                                    Log.d(TAG, "User account deleted.");
                                    mAuth.signOut();
                                    FirebaseAuth.getInstance().signOut();
                                    LoginManager.getInstance().logOut();
                                    Intent logoutIntent = new Intent(getActivity(), LoginActivity.class);
                                    logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(logoutIntent);
                                    Toast.makeText(getContext(), "~~ Thanks for using our App! ~~", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

    }

}