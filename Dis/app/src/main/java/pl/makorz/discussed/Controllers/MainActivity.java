package pl.makorz.discussed.Controllers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pl.makorz.discussed.Controllers.Fragments.*;

import pl.makorz.discussed.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private String[] titles;
    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private AlertDialog dialog;
    private int currentPosition = 0;
    private int introPage = 0;
    private int userAge;
    public String searchID = "";
    private String countryCode, searchWorld, searchCountry;
    private boolean profileCompleted = false;
    private boolean isPremium, canUserSearch, isUserFemale;

    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //no dark theme
        int USER_NEW_ACCOUNT = getIntent().getIntExtra("USER_NEW_ACCOUNT", -10);
        Date USER_LOGIN_DATE = (Date) getIntent().getSerializableExtra("USER_LOGIN_DATE");
        Log.d(TAG, String.valueOf(USER_NEW_ACCOUNT));
        setContentView(R.layout.activity_main);
        initView(savedInstanceState);

        if (USER_NEW_ACCOUNT == 0) {
            introAlertDialog();
        } else {
            db.collection("users").document(user.getUid())
                    .update("lastLoginDate", USER_LOGIN_DATE);
            db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            isPremium = document.getBoolean("premium");
                            canUserSearch = document.getBoolean("canUserSearch");
                            isUserFemale = document.getBoolean("genderFemale");
                            countryCode = document.getString("locationCountryCode");
                            searchCountry = document.getString("searchIDCountry");
                            searchWorld = document.getString("searchIDWorld");
                            userAge = document.getDouble("ageOfUser").intValue();
                            if (canUserSearch) {

                                String gender;
                                if (isUserFemale) {
                                    gender = "female";
                                } else {
                                    gender = "male";
                                }
                                String age = Integer.toString(userAge);

                                db.collection("search").document("world").collection("gender").document(gender)
                                        .collection("age").document(age).collection("users").document(searchWorld).update("lastLoginDate", USER_LOGIN_DATE);

                                db.collection("search").document(countryCode).collection("gender").document(gender)
                                        .collection("age").document(age).collection("users").document(searchCountry).update("lastLoginDate", USER_LOGIN_DATE);

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



    }

    public void initView(Bundle savedInstanceState) {


        titles = getResources().getStringArray(R.array.titles);
        drawerList = findViewById(R.id.drawer);
        drawerLayout = findViewById(R.id.drawer_layout);

        //Populate the ListView
        drawerList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_activated_1, titles));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Retrieve Firebase Authentication Instance to logout user
        mAuth = FirebaseAuth.getInstance();

        //Display the correct fragment.
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt("position");
            setActionBarTitle(currentPosition);
        } else {
            selectItem(0);
        }

        //Create the ActionBarDrawerToggle
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.open_drawer, R.string.close_drawer) {
            //Called when a drawer has settled in a completely closed state
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            //Called when a drawer has settled in a completely open state.
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        FragmentManager fragMan = getSupportFragmentManager();
                        Fragment fragment = fragMan.findFragmentByTag("visible_fragment");
                        if (fragment instanceof MainFragment) {
                            currentPosition = 0;
                        }
                        if (fragment instanceof BlindDateFragment) {
                            currentPosition = 1;
                        }
                        if (fragment instanceof ConversationsFragment) {
                            currentPosition = 2;
                        }
                        if (fragment instanceof SettingsFragment) {
                            currentPosition = 3;
                        }
                        if (fragment instanceof AboutFragment) {
                            currentPosition = 4;
                        }
                        setActionBarTitle(currentPosition);
                        drawerList.setItemChecked(currentPosition, true);
                    }
                }
        );

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {

        // Update the main content by replacing fragments
        currentPosition = position;
        Fragment fragment;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (position) {
            case 1:
                fragment = new BlindDateFragment();
                break;
            case 2:
                fragment = new ConversationsFragment();
                break;
            case 3:
                fragment = new SettingsFragment();
                break;
            case 4:
                fragment = new AboutFragment();
                break;
            case 5:
                fragment = new MainFragment();
                Log.i(TAG, "Logout");
                // Logout user
                mAuth.signOut();
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                Intent logoutIntent = new Intent(this, LoginActivity.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logoutIntent);
                break;
            default:
                fragment = new MainFragment();

        }

        ft.replace(R.id.content_frame, fragment, "visible_fragment");
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
        //Set the action bar title
        setActionBarTitle(position);
        //Close drawer
        drawerLayout.closeDrawer(drawerList);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", currentPosition);
    }

    private void setActionBarTitle(int position) {
        String title;
        if (position == 0) {
            title = getResources().getString(R.string.app_name);
        } else {
            title = titles[position];
        }
        getSupportActionBar().setTitle(title);
    }

    // Show MyProfile icon in menu bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (isPremium) {
            menu.getItem(0).setIcon(R.drawable.own_profile_icon_premium);
        }
        return super.onCreateOptionsMenu(menu);
    }

    // What happens if you click on icon on menu bar (MyProfile lunch)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_show_my_profile:
                //Code to run when the Profile Icon is clicked
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Not blank screen after pressing back on main fragment
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else if (getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }

    }

//    @Override
//    protected void onResume() {
////        if (dialog.isShowing()) {
////            dialog.dismiss();
////        }
//        super.onResume();
//    }

    // Function updates collections when user is first time in app.
    private void fillUserDocument() throws ExecutionException, InterruptedException {

        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        Date date = new Date(new Date().getTime());
                        Log.d(TAG, date.toString());
                        Date dateIntro = new Date(new Date().getTime() - 35 * 86400000L); // Previous date for first time
                        Log.d(TAG, dateIntro.toString());
                        Map<String, Object> userCompletion = new HashMap<>();
                        userCompletion.put("isActive", true);
                        userCompletion.put("searchID", searchID);
                        userCompletion.put("lastLoginDate", date);
                        userCompletion.put("canUserSearch", false);
                        userCompletion.put("sendGoodJob", false);
                        userCompletion.put("blindDateParticipationWill", true);
                        userCompletion.put("filledNecessaryInfo", false);
                        userCompletion.put("premium", false);
                        userCompletion.put("displayName", "");
                        userCompletion.put("genderFemale", false);
                        userCompletion.put("ageOfUser", 0);
                        userCompletion.put("firstPhotoUri", "null");
                        userCompletion.put("firstPhotoUploadMade", false);
                        userCompletion.put("firstPhotoUploadDate", dateIntro);
                        userCompletion.put("secondPhotoUri", "null");
                        userCompletion.put("secondPhotoUploadMade", false);
                        userCompletion.put("secondPhotoUploadDate", dateIntro);
                        userCompletion.put("thirdPhotoUri", "null");
                        userCompletion.put("thirdPhotoUploadMade", false);
                        userCompletion.put("thirdPhotoUploadDate", dateIntro);
                        userCompletion.put("locationUploadMade", false);
                        userCompletion.put("locationUploadDate", dateIntro);
                        userCompletion.put("locationCountryName", "");
                        userCompletion.put("locationCountryCode", "");
                        userCompletion.put("placeName", "");
                        userCompletion.put("locationLatLng", new LatLng(0, 0));
                        userCompletion.put("descriptionUploadMade", false);
                        userCompletion.put("descriptionUploadDate", dateIntro);
                        userCompletion.put("ageUploadMade", false);
                        userCompletion.put("ageUploadDate", dateIntro);
                        userCompletion.put("nameUploadMade", false);
                        userCompletion.put("nameUploadDate", dateIntro);
                        userCompletion.put("genderUploadMade", false);
                        userCompletion.put("genderUploadDate", dateIntro);
                        userCompletion.put("topicsUploadMade", false);
                        userCompletion.put("topicsUploadDate", dateIntro);
                        userCompletion.put("chosenTopicsArray", Arrays.asList("", "", ""));
                        userCompletion.put("nrOfOngoingChats", 0);
                        userCompletion.put("nrOfOngoingBlindDates", 0);
                        userCompletion.put("fcmRegistrationToken", "MessageTokenNULL");
                        Log.d(TAG, userCompletion.toString());
                        docRef.set(userCompletion, SetOptions.merge());

                        Map<String, Object> userUpdateValues = new HashMap<>();
                        userUpdateValues.put("location", new GeoPoint(0, 0));
                        userUpdateValues.put("age", 1);
                        docRef.update(userUpdateValues);

                        profileCompleted = true;

                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                    return;
                                }

                                // Get new FCM registration token
                                String token = task.getResult();
                                Log.d(TAG, token);
                                Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                                db.collection("users").document(user.getUid()).update("fcmRegistrationToken", token);
                            }
                        });
                    }
                }
            }
        });

    }

    // PopUp with entry info about app.
    private void introAlertDialog() {

        new Thread() {
            public void run() {
                android.os.SystemClock.sleep(1500);
                try {
                    fillUserDocument();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        LayoutInflater inflaterDialog = LayoutInflater.from(this);
        View introDialogView = inflaterDialog.inflate(R.layout.intro_dialog_box, null);

        AlertDialog introDialog = new AlertDialog.Builder(this)
                .setView(introDialogView)  // What to use in dialog box
                .setPositiveButton(R.string.next_dialog_boxes, null)
                .setCancelable(false)
                .show();

        introDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (introPage == 0) {
                    View introDialogView2 = inflaterDialog.inflate(R.layout.intro_dialog_box2, null);

                    AlertDialog introDialog2 = new AlertDialog.Builder(MainActivity.this)
                            .setView(introDialogView2)  // What to use in dialog box
                            .setPositiveButton(R.string.understand_text_dialog_boxes, null)
                            .setCancelable(false)
                            .show();
                    introPage++;
                    introDialog2.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (profileCompleted) {
                                introDialog2.dismiss();
                            }
                        }
                    });
                    introDialog.dismiss();
                } else if (introPage == 1) {
                    introDialog.dismiss();
                }

            }
        });
    }

}



