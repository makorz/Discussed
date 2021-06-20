package pl.makorz.discussed.Controllers.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;


import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import pl.makorz.discussed.Controllers.LoginActivity;
import pl.makorz.discussed.R;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsActivity";
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Retrive Firebase Authentication Instance to logout user
        mAuth = FirebaseAuth.getInstance();

        RelativeLayout settingsLayout = (RelativeLayout)inflater.inflate(R.layout.fragment_settings, container, false);
        Button button = (Button) settingsLayout.findViewById(R.id.button_logout);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.i(TAG, "Logout");
                // Logout user
                mAuth.signOut();
                Intent logoutIntent = new Intent(getActivity(), LoginActivity.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(logoutIntent);
            }
        });
        return settingsLayout;
    }

}