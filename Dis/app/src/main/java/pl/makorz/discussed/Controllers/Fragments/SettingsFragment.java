package pl.makorz.discussed.Controllers.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.TimeUnit;

import pl.makorz.discussed.Controllers.LoginActivity;
import pl.makorz.discussed.R;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SettingsActivity";
    private FirebaseAuth mAuth;
    public Button buttonGoodJob, buttonDonate, buttonLogout, buttonDeleteAccount;
    public int whatButtonPressed = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        LinearLayout settingsLayout = (LinearLayout)inflater.inflate(R.layout.fragment_settings, container, false);
        initView(settingsLayout);
        return settingsLayout;

    }

    // This function initialises buttons, text views, etc.
    public void initView(LinearLayout layout) {

        buttonLogout = layout.findViewById(R.id.logout_button_settings_tab);
        buttonDonate = layout.findViewById(R.id.donate_button_settings_tab);
        buttonGoodJob = layout.findViewById(R.id.goodjob_button_settings_tab);
        buttonDeleteAccount = layout.findViewById (R.id.deleteAccount_button_settings_tab);

        buttonLogout.setOnClickListener(this);
        buttonDonate.setOnClickListener(this);
        buttonGoodJob.setOnClickListener(this);
        buttonDeleteAccount.setOnClickListener(this);

    }

    // This function starts functions correlated with certain button
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.goodjob_button_settings_tab:

                whatButtonPressed = 1;
                break;
            case R.id.donate_button_settings_tab:

                break;
            case R.id.logout_button_settings_tab:
                mAuth.signOut();
                Intent logoutIntent = new Intent(getActivity(), LoginActivity.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logoutIntent);
                whatButtonPressed = 3;
                break;
            case R.id.deleteAccount_button_settings_tab:

                whatButtonPressed = 4;
                break;
            default:
                break;
        }
    }

}