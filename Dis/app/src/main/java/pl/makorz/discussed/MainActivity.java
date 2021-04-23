package pl.makorz.discussed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import pl.makorz.discussed.Fragments.BlindDateFragment;
import pl.makorz.discussed.Fragments.ConversationsFragment;
import pl.makorz.discussed.Fragments.MainFragment;
import pl.makorz.discussed.Fragments.SettingsFragment;
import pl.makorz.discussed.Fragments.AboutFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static int ageOfAccount = -1;
    private String[] titles;
    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private int currentPosition = 0;
    String searchID = "";


    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //no dark theme

        titles = getResources().getStringArray(R.array.titles);
        drawerList = (ListView)findViewById(R.id.drawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

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
        switch(position) {
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
                Intent logoutIntent = new Intent(this, LoginActivity.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logoutIntent);
                break;
            default:
                fragment = new MainFragment();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
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
                //Code to run when the Create Order item is clicked
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

    @Override
    protected void onStart() {
        super.onStart();
        fillUserDocument();
    }

    private void fillUserDocument() {

        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        ageOfAccount = document.getLong("age").intValue();
                    } else {
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });

        // If the account is new complete the User document
        if (ageOfAccount == 0) {

            Map<String, Object> addToSearchCollection = new HashMap<>();
            addToSearchCollection.put("locationOfUser",new GeoPoint(0,0));
            addToSearchCollection.put("idOfUser", user.getUid());
            addToSearchCollection.put("ageOfUser",1);
            addToSearchCollection.put("genderOfUserFemale",false);

            db.collection("search").add(addToSearchCollection)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        searchID = documentReference.getId();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });

            Map<String, Object> userCompletion = new HashMap<>();
            userCompletion.put("photoURL1", "null");
            userCompletion.put("photoURL2", "null");
            userCompletion.put("photoURL3", "null");
            userCompletion.put("isActive", true);
            userCompletion.put("searchID", searchID);
            userCompletion.put("blindDateParticipationWill", true);
            userCompletion.put("filledNecessaryInfo", false);
            userCompletion.put("premium", false);
            userCompletion.put("genderFemale",false);
            userCompletion.put("ageOfUser", 0);
            docRef.set(userCompletion, SetOptions.merge());

//            Map<String, Object> defaultChatOrBlindDateDocument = new HashMap<>();
//            defaultChatOrBlindDateDocument.put("chatID", "null");
//            defaultChatOrBlindDateDocument.put("dateOfChatCreation", new Date());
//            defaultChatOrBlindDateDocument.put("dateOfLastActivity", new Date());
//            defaultChatOrBlindDateDocument.put("nameOfOtherUser","null");
//
//            for (int i = 0; i <= 9; i++) {
//                String Nr = Integer.toString(i);
//                String chatPath = "chat" + Nr;
//                String blindDatesPath = "blindDate" + Nr;
//                docRef.collection("userChats").document(chatPath).set(defaultChatOrBlindDateDocument);
//                docRef.collection("userBlindDates").document(blindDatesPath).set(defaultChatOrBlindDateDocument);
//            }

            Map<String, Object> userUpdateValues = new HashMap<>();
            userUpdateValues.put("location", new GeoPoint(0,0));
            userUpdateValues.put("age",1);

            ageOfAccount++;
            docRef.update(userUpdateValues);
        }

    }



}