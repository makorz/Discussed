package pl.makorz.discussed.Controllers.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pl.makorz.discussed.Controllers.ChatActivity;
import pl.makorz.discussed.R;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragmentActivity";

    private static final String CAN_USER_SEARCH = "canUserSearch";
    private static final String FIRST_PHOTO_UPLOAD_MADE = "firstPhotoUploadMade";
    private static final String SECOND_PHOTO_UPLOAD_MADE = "secondPhotoUploadMade";
    private static final String THIRD_PHOTO_UPLOAD_MADE = "thirdPhotoUploadMade";
    private static final String LOCATION_UPLOAD_MADE = "locationUploadMade";
    private static final String TOPICS_UPLOAD_MADE = "topicsUploadMade";
    private static final String AGE_UPLOAD_MADE = "ageUploadMade";
    private static final String DESCRIPTION_UPLOAD_MADE = "descriptionUploadMade";
    private static final String GENDER_UPLOAD_MADE = "genderUploadMade";
    private static final String NAME_UPLOAD_MADE = "nameUploadMade";
    private static final String NR_OF_USER_CHATS = "nrOfOngoingChats";

    private static final String TOPICS_ARRAY = "chosenTopicsArray";
    private static final String COUNTRY_CODE_FIELD = "locationCountryCode";
    private static final String LOCATION_FIELD = "location";
    private static final String NAME_FIELD = "displayName";
    private static final String FIRST_PHOTO_URI = "firstPhotoUri";

    private String chatID, nameOfUser, nameOfOtherUser, firstPhotoUriOfUser, firstPhotoUriOfOtherUser, idOfOtherUser, countryCode;
    private Button buttonChat, buttonBlindDate;
    private RangeSlider ageSlider;
    private ProgressBar waitUntilChatAppears;
    private LinearLayout layoutToDimWhenSearching;
    private Boolean firstPhotoUploadMade, secondPhotoUploadMade, thirdPhotoUploadMade, locationUploadMade, descriptionUploadMade, ageUploadMade, topicsUploadMade,
            genderUploadMade, nameUploadMade, canUserSearch, isFemaleToSearch;
    private GeoPoint locationUser, otherUserLocation;
    private int nrOfUserChats, radiusToSearch;
    private int nrOfAttemptsToFindSomeone = 0;
    private List<Float> ageValues = new ArrayList<>();
    private RadioGroup radioGroupLocation1, radioGroupLocation2, radioGroupGender;

    private float latitudeUser, longitudeUser, minLatitude, maxLatitude, minLongitude, maxLongitude;
    private ArrayList<String> topicList;
    private final Map<String, Boolean> userSearchAvailability = new HashMap<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DocumentSnapshot userSnapshot, otherUserSnapshot;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //no dark theme
        View mainView = inflater.inflate(R.layout.fragment_main, container, false);
        waitUntilChatAppears = mainView.findViewById(R.id.progressBarOfSearch);
        layoutToDimWhenSearching = mainView.findViewById(R.id.linearLayoutOfSearch);
        buttonChat = mainView.findViewById(R.id.button_search_chatMate);
        buttonBlindDate = mainView.findViewById(R.id.button_start_blindDate);
        ageSlider = mainView.findViewById(R.id.seekBar_age);
        radioGroupLocation1 = mainView.findViewById(R.id.radioButtonGroup_location_column1);
        radioGroupLocation2 = mainView.findViewById(R.id.radioButtonGroup_location_column2);
        radioGroupGender = mainView.findViewById(R.id.radioButtonGroup_gender);

        radioGroupLocation1.clearCheck();
        radioGroupLocation2.clearCheck();
        radioGroupGender.clearCheck();
        radioGroupLocation1.setOnCheckedChangeListener(listenerLocation1);
        radioGroupLocation2.setOnCheckedChangeListener(listenerLocation2);
        radioGroupGender.setOnCheckedChangeListener(listenerGender);
        // Default ageValues
        ageValues.add(0, 30f);
        ageValues.add(1, 40f);

        // Age slider saves picked values
        ageSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                ageValues = ageSlider.getValues();
            }
        });

        buttonChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonChat.setEnabled(false);
                buttonBlindDate.setEnabled(false);
                layoutToDimWhenSearching.setAlpha(0.3f);
                waitUntilChatAppears.setVisibility(View.VISIBLE);
                try {
                    checkIfUserCanSearch();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        return mainView;
    }

    // Listeners made for two RadioGroups Location, to select only 1 item from both groups (two radio groups were made because in single radio group not possible ot make two columns)
    private final RadioGroup.OnCheckedChangeListener listenerLocation1 = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                radioGroupLocation2.setOnCheckedChangeListener(null); // remove the listener before clearing so we don't throw that stackoverflow exception
                radioGroupLocation2.clearCheck(); // clear the second RadioGroup!
                radioGroupLocation2.setOnCheckedChangeListener(listenerLocation2); //reset the listener
                switch (checkedId) {
                    case R.id.radioButton_10:
                        radiusToSearch = 10000;
                        break;
                    case R.id.radioButton_25:
                        radiusToSearch = 25000;
                        break;
                    case R.id.radioButton_100:
                        radiusToSearch = 100000;
                        break;
                }
            }
        }
    };

    private final RadioGroup.OnCheckedChangeListener listenerLocation2 = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                radioGroupLocation1.setOnCheckedChangeListener(null);
                radioGroupLocation1.clearCheck();
                radioGroupLocation1.setOnCheckedChangeListener(listenerLocation1);
                switch (checkedId) {
                    case R.id.radioButton_250:
                        radiusToSearch = 250000;
                        break;
                    case R.id.radioButton_country:
                        radiusToSearch = 500000;
                        break;
                    case R.id.radioButton_world:
                        radiusToSearch = 1000000;
                        break;
                }

            }
        }
    };

    private final RadioGroup.OnCheckedChangeListener listenerGender = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radioButton_male:
                    isFemaleToSearch = false;
                    break;
                case R.id.radioButton_female:
                    isFemaleToSearch = true;
                    break;
            }

        }
    };

    @Override
    public void onStart() {
        buttonChat.setEnabled(true);
        buttonBlindDate.setEnabled(true);
        super.onStart();
    }

    // Searching user with some parameters
    private void searchForUser2(int minAge, int maxAge, int radius, boolean isFemale) {

        String countryCodeLocal = "world";

        float degreeFor10km = 0.1f;
        float degreeFor25km = 0.23f;
        float degreeFor100km = 0.95f;
        float degreeFor250km = 2.25f;

        switch (radius) {
            case 10000:
                minLatitude = latitudeUser - degreeFor10km;
                minLongitude = longitudeUser - degreeFor10km;
                countryCodeLocal = countryCode;
                break;
            case 25000:
                minLatitude = latitudeUser - degreeFor25km;
                minLongitude = longitudeUser - degreeFor25km;
                countryCodeLocal = countryCode;
                break;
            case 100000:
                minLatitude = latitudeUser - degreeFor100km;
                minLongitude = longitudeUser - degreeFor100km;
                countryCodeLocal = countryCode;
                break;
            case 250000:
                minLatitude = latitudeUser - degreeFor250km;
                minLongitude = longitudeUser - degreeFor250km;
                countryCodeLocal = countryCode;
                break;
            case 500000:
                Random r1 = new Random();
                minLatitude = latitudeUser - r1.nextInt(5);
                minLongitude = longitudeUser - r1.nextInt(5);
                countryCodeLocal = countryCode;
                break;
            default:
                Random r2 = new Random();
                Random r3 = new Random();
                int plusOrMinus = r3.nextInt(2);

                // if for which south or north because number in latitude field or longitude can be minus

                if (plusOrMinus > 0) {
                    minLatitude = r2.nextInt(80);
                    minLongitude = r2.nextInt(170);
                } else {
                    minLatitude = r2.nextInt(80) * -1;
                    minLongitude = r2.nextInt(170) * -1;
                }

                break;
        }

        String genderForSearch;

        if (isFemale) {
            genderForSearch = "female";
        } else {
            genderForSearch = "male";
        }

        ArrayList<String> idOfOtherUsers = new ArrayList<>();

        Random r = new Random();
        int moreRandomSearching = r.nextInt(10 - 1) + 1;
        int randomAgeFromChosenRange = r.nextInt(maxAge - minAge) + minAge;
        String ageFromRange = Integer.toString(minAge);
        Query queryUser;

        if (moreRandomSearching <= 5) {
            queryUser = FirebaseFirestore.getInstance().collection("search").document(countryCodeLocal).collection("gender").document(genderForSearch).collection("age")
                    .document(ageFromRange).collection("users").orderBy("latitude").startAt(minLatitude).whereArrayContainsAny("topicsList", topicList).limit(50);
        } else {
            queryUser = FirebaseFirestore.getInstance().collection("search").document(countryCodeLocal).collection("gender").document(genderForSearch).collection("age")
                    .document(ageFromRange).collection("users").orderBy("longitude").startAt(minLongitude).whereArrayContainsAny("topicsList", topicList).limit(50);
        }

        queryUser.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                Log.d(TAG, "USERS NOT FOUND");

                                if (nrOfAttemptsToFindSomeone < 3) {
                                    nrOfAttemptsToFindSomeone++;
                                    searchForUser2(minAge, maxAge, radius, isFemale);
                                } else {
                                    Toast.makeText(getContext(), "Users with such parameters not found, try again with different parameters", Toast.LENGTH_SHORT).show();
                                    nrOfAttemptsToFindSomeone = 0;
                                    waitUntilChatAppears.setVisibility(View.INVISIBLE);
                                    layoutToDimWhenSearching.setAlpha(1f);
                                    buttonChat.setEnabled(true);
                                    buttonBlindDate.setEnabled(true);
                                }

                            } else {

                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    idOfOtherUser = document.get("idOfUser").toString();

                                    if (idOfOtherUser.equals(user.getUid())) {

                                        Log.d(TAG, "User who started searching searched himself/herself");

                                    } else {

                                        otherUserLocation = document.getGeoPoint("locationOfUser");

                                        if (radius < 500000) { // Radius

                                            float[] results = new float[1];
                                            Location.distanceBetween(latitudeUser, longitudeUser, otherUserLocation.getLatitude(), otherUserLocation.getLongitude(), results);
                                            float distanceInMeters = results[0];

                                            if (distanceInMeters < radius) {
                                                idOfOtherUsers.add(idOfOtherUser);
                                            }

                                        } else if (radius == 500000) { // Country
                                            idOfOtherUsers.add(idOfOtherUser);
                                        } else if (otherUserLocation.getLongitude() >= minLongitude && otherUserLocation.getLatitude() >= minLatitude) { // World
                                            idOfOtherUsers.add(idOfOtherUser);
                                        }
                                    }
                                }

                                Log.d(TAG, "querysearchusersssssss" + idOfOtherUsers.toString());

                                int nrOfFoundUsers = idOfOtherUsers.size();
                                if (nrOfFoundUsers > 0) {
                                    Random userRandom = new Random();

                                    Log.d(TAG, "nrofusers" + nrOfFoundUsers);
                                    int randomUserFromArrayNumber = userRandom.nextInt(nrOfFoundUsers);
                                    Log.d(TAG, "randomNr" + randomUserFromArrayNumber);
                                    getUsersNames(idOfOtherUsers.get(randomUserFromArrayNumber));

                                }
                            }
                        }
                    }
                });

    }

    // Create Chat Activity
    private void generateChatInFirestore() {
        Map<String, Object> chat = new HashMap<>();
        Map<String, Object> user1 = new HashMap<>();
        Map<String, Object> user2 = new HashMap<>();

        db.collection("chats").add(chat)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        chatID = documentReference.getId();
                        chat.put("chatID", chatID);
                        chat.put("dateOfChatCreation", new Date());
                        chat.put("usersParticipatingName", Arrays.asList(nameOfUser, nameOfOtherUser));
                        chat.put("usersThatHaveNotDeletedConversation", Arrays.asList(user.getUid(), idOfOtherUser));
                        chat.put("lastMessage", "");
                        chat.put("lastMessageDate", new Date());
                        chat.put("usersParticipatingFirstImageUri", Arrays.asList(firstPhotoUriOfUser, firstPhotoUriOfOtherUser));
                        chat.put("usersParticipatingID", Arrays.asList(user.getUid(), idOfOtherUser));
                        chat.put("isFirstPhotoOfUserUncovered", Arrays.asList(false, false));

                        user1.put("userID", user.getUid());
                        user1.put("pointsFromOtherUser", (Integer) 0);
                        user1.put("userName", nameOfUser);
                        user1.put("uncoverStrangerFirstPhoto", false);
                        user1.put("uncoverStrangerSecondPhoto", false);
                        user1.put("uncoverStrangerThirdPhoto", false);
                        user1.put("uncoverStrangerDescription", false);
                        user1.put("uncoverStrangerAge", false);
                        user1.put("uncoverStrangerLocation", false);
                        user1.put("uncoverStrangerGender", false);
                        db.collection("chats").document(chatID).collection("chatUsers").document(user.getUid()).set(user1);

                        user2.put("userID", idOfOtherUser);
                        user2.put("pointsFromOtherUser", (Integer) 0);
                        user2.put("userName", nameOfOtherUser);
                        user2.put("uncoverStrangerFirstPhoto", false);
                        user2.put("uncoverStrangerSecondPhoto", false);
                        user2.put("uncoverStrangerThirdPhoto", false);
                        user2.put("uncoverStrangerDescription", false);
                        user2.put("uncoverStrangerAge", false);
                        user2.put("uncoverStrangerLocation", false);
                        user2.put("uncoverStrangerGender", false);
                        db.collection("chats").document(chatID).collection("chatUsers").document(idOfOtherUser).set(user2);

                        db.collection("users").document(user.getUid()).update(NR_OF_USER_CHATS, FieldValue.increment(1));

                        db.collection("chats").document(chatID).set(chat)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                                        intent.putExtra("chatIdIntent", chatID);
                                        intent.putExtra("otherUserName", nameOfOtherUser);
                                        intent.putExtra("idOfOtherUser", idOfOtherUser);
                                        waitUntilChatAppears.setVisibility(View.INVISIBLE);
                                        layoutToDimWhenSearching.setAlpha(1f);
                                        requireActivity().startActivity(intent);

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });

    }

    // Get names of user that was searched
    private void getUsersNames(String id) {

        int numCores = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(numCores * 2, numCores * 2,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

        String str = id.replaceAll("\\s", "");

        DocumentReference docRef2 = db.collection("users").document(str);
        Task<DocumentSnapshot> taskOtherUser = docRef2.get();

//        DocumentReference docRef = db.collection("users").document(user.getUid());
//        Task<DocumentSnapshot> taskUser = docRef.get();

//        taskUser.addOnCompleteListener(executor, new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                userSnapshot = task.getResult();
//                nameOfUser = userSnapshot.getString("displayName");
//                firstPhotoUriOfUser = userSnapshot.getString("firstPhotoUri");

        taskOtherUser.addOnCompleteListener(executor, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                otherUserSnapshot = task2.getResult();
                nameOfOtherUser = otherUserSnapshot.getString("displayName");
                firstPhotoUriOfOtherUser = otherUserSnapshot.getString("firstPhotoUri");
                Log.d(TAG, "onCompleteTASKSKAKSAKDK: " + nameOfOtherUser);
                generateChatInFirestore();
            }
        });
//            }
//        });
    }

    public void profileNotFiledAlertDialog() {

        layoutToDimWhenSearching.setAlpha(1f);
        waitUntilChatAppears.setVisibility(View.INVISIBLE);
        LayoutInflater inflaterDialog = LayoutInflater.from(getContext());
        View alertNotFiledProfileDialogView = inflaterDialog.inflate(R.layout.dialog_search_not_available, null);
        TextView notFiledProfileAlertTitle = alertNotFiledProfileDialogView.findViewById(R.id.title_of_search_availability_box);

        int i = 0;
        StringBuilder whatWasNotFilled = new StringBuilder(getString(R.string.title_not_filled_profile_to_search_alert_dialog));

        for (Map.Entry<String, Boolean> entry : userSearchAvailability.entrySet()) {
            String addTopic = "";
            if (!entry.getValue()) {
                i++;
                String nrOfTopic = String.valueOf(i);
                addTopic = nrOfTopic + ". " + entry.getKey() + "\n";
            }
            whatWasNotFilled.append(addTopic);
        }
        notFiledProfileAlertTitle.setText(whatWasNotFilled);
        userSearchAvailability.clear();

        AlertDialog alertDateDialog = new AlertDialog.Builder(getContext())
                .setView(alertNotFiledProfileDialogView)  // What to use in dialog box
                .setPositiveButton("OK", null)
                .show();

        alertDateDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonChat.setEnabled(true);
                buttonBlindDate.setEnabled(true);
                alertDateDialog.dismiss();
            }
        });

    }

    public void notEnoughSpaceForChats() {

        layoutToDimWhenSearching.setAlpha(1f);
        waitUntilChatAppears.setVisibility(View.INVISIBLE);
        LayoutInflater inflaterDialog = LayoutInflater.from(getContext());
        View alertNotFiledProfileDialogView = inflaterDialog.inflate(R.layout.dialog_search_not_available, null);
        TextView notFiledProfileAlertTitle = alertNotFiledProfileDialogView.findViewById(R.id.title_of_search_availability_box);
        notFiledProfileAlertTitle.setText(R.string.title_not_enough_sapce_for_chats_alert_dialog);

        AlertDialog alertDateDialog = new AlertDialog.Builder(getContext())
                .setView(alertNotFiledProfileDialogView)  // What to use in dialog box
                .setPositiveButton("OK", null)
                .show();

        alertDateDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonChat.setEnabled(true);
                buttonBlindDate.setEnabled(true);
                alertDateDialog.dismiss();
            }
        });

    }

    private void checkIfUserCanSearch() throws ExecutionException, InterruptedException {
        // Download document of current user, to retrieve actual info to profile view
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {

                        canUserSearch = document.getBoolean(CAN_USER_SEARCH);
                        locationUser = document.getGeoPoint(LOCATION_FIELD);
                        latitudeUser = (float) locationUser.getLatitude();
                        longitudeUser = (float) locationUser.getLongitude();
                        topicList = (ArrayList<String>) document.get(TOPICS_ARRAY);
                        countryCode = document.getString(COUNTRY_CODE_FIELD);
                        nameOfUser = document.getString(NAME_FIELD);
                        firstPhotoUriOfUser = document.getString(FIRST_PHOTO_URI);
                        nrOfUserChats = document.getDouble(NR_OF_USER_CHATS).intValue();

                        if (canUserSearch && nrOfUserChats <= 20) {

                            if (ageValues.get(1) - ageValues.get(0) > 10 || ageValues.get(1) - ageValues.get(0) < 5 ) {
                                Toast.makeText(getContext(), "Age range can be set between 5 to 10 years difference.", Toast.LENGTH_SHORT).show();
                                waitUntilChatAppears.setVisibility(View.INVISIBLE);
                                layoutToDimWhenSearching.setAlpha(1f);
                                buttonChat.setEnabled(true);
                                buttonBlindDate.setEnabled(true);
                            } else if (radioGroupGender.getCheckedRadioButtonId() == -1 || (radioGroupLocation1.getCheckedRadioButtonId() == -1 && radioGroupLocation2.getCheckedRadioButtonId() == -1)) {
                                Toast.makeText(getContext(), "Check gender of user you are searching and radius where to search for him/her.", Toast.LENGTH_SHORT).show();
                                waitUntilChatAppears.setVisibility(View.INVISIBLE);
                                layoutToDimWhenSearching.setAlpha(1f);
                                buttonChat.setEnabled(true);
                                buttonBlindDate.setEnabled(true);
                            } else {
                                searchForUser2( (int) Math.round(ageValues.get(0)), (int) Math.round(ageValues.get(1)), radiusToSearch, isFemaleToSearch);
                                // searchForUser2(27, 27, 100000, false);
                            }

                        } else if (!canUserSearch) {

                            firstPhotoUploadMade = document.getBoolean(FIRST_PHOTO_UPLOAD_MADE);
                            secondPhotoUploadMade = document.getBoolean(SECOND_PHOTO_UPLOAD_MADE);
                            thirdPhotoUploadMade = document.getBoolean(THIRD_PHOTO_UPLOAD_MADE);
                            locationUploadMade = document.getBoolean(LOCATION_UPLOAD_MADE);
                            nameUploadMade = document.getBoolean(NAME_UPLOAD_MADE);
                            ageUploadMade = document.getBoolean(AGE_UPLOAD_MADE);
                            topicsUploadMade = document.getBoolean(TOPICS_UPLOAD_MADE);
                            descriptionUploadMade = document.getBoolean(DESCRIPTION_UPLOAD_MADE);
                            genderUploadMade = document.getBoolean(GENDER_UPLOAD_MADE);

                            userSearchAvailability.put("First Photo", firstPhotoUploadMade);
                            userSearchAvailability.put("Second Photo", secondPhotoUploadMade);
                            userSearchAvailability.put("Third Photo", thirdPhotoUploadMade);
                            userSearchAvailability.put("Location", locationUploadMade);
                            userSearchAvailability.put("Name", nameUploadMade);
                            userSearchAvailability.put("Age", ageUploadMade);
                            userSearchAvailability.put("Topics", topicsUploadMade);
                            userSearchAvailability.put("Description", descriptionUploadMade);
                            userSearchAvailability.put("Gender", genderUploadMade);

                            profileNotFiledAlertDialog();

                        } else {

                            notEnoughSpaceForChats();

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



