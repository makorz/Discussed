package pl.makorz.discussed.Controllers.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import pl.makorz.discussed.Controllers.BlindDateActivity;
import pl.makorz.discussed.Controllers.ChatActivity;
import pl.makorz.discussed.Controllers.Functions.APIService;
import pl.makorz.discussed.Controllers.Notifications.Client;
import pl.makorz.discussed.Controllers.Notifications.Data;
import pl.makorz.discussed.Controllers.Notifications.Response;
import pl.makorz.discussed.Controllers.Notifications.Sender;
import pl.makorz.discussed.Controllers.ProfileActivity;
import pl.makorz.discussed.R;
import retrofit2.Call;
import retrofit2.Callback;

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
    private static final String NR_OF_STARTED_CHATS_TODAY = "nrOfStartedChatsToday";
    private static final String NR_OF_STARTED_BLIND_DATES_TODAY = "nrOfStartedBlindDatesToday";
    private static final String BLIND_DATE_PARTICIPATION_WILL = "blindDateParticipationWill";
    private static final String LAST_DATE_OF_STARTED_CHAT = "lastDateOfStartedChat";
    private static final String LAST_DATE_OF_STARTED_BLIND_DATE = "lastDateOfStartedBlindDate";
    private static final String ARRAY_OF_USERS_WITH_ACTIVE_CONVERSATIONS = "otherUsersIDFromStartedChatsArray";
    private static final String LAST_LOGIN_DATE = "lastLoginDate";
    private static final String PREMIUM = "premium";
    private static final String TOPICS_ARRAY = "chosenTopicsArray";
    private static final String COUNTRY_CODE_FIELD = "locationCountryCode";
    private static final String LOCATION_FIELD = "location";
    private static final String NAME_FIELD = "displayName";
    private static final String FIRST_PHOTO_URI = "firstPhotoUri";

    private String chatID, blindDateID, nameOfUser, nameOfOtherUser, firstPhotoUriOfUser, firstPhotoUriOfOtherUser, idOfOtherUser, countryCode;
    private float latitudeUser, longitudeUser, minLatitude, maxLatitude, minLongitude, maxLongitude;
    private Boolean firstPhotoUploadMade, secondPhotoUploadMade, thirdPhotoUploadMade, locationUploadMade, descriptionUploadMade, ageUploadMade, topicsUploadMade,
            genderUploadMade, nameUploadMade, canUserSearch, isFemaleToSearch, blindDateParticipationWill, isPremium;
    private GeoPoint locationUser, otherUserLocation;
    private int nrOfUserChats, nrOfUserBlindDates, radiusToSearch, randomUser1, randomUser2, randomUser3;
    private int nrOfAttemptsToFindSomeone = 0;
    private Date lastDateOfStartedBlindDate, lastDateOfStartedChat, lastLoginDateOfSearchedUser;

    private Button buttonChat, buttonBlindDate;
    private RangeSlider ageSlider;
    private ProgressBar waitUntilChatAppears;
    private LinearLayout layoutToDimWhenSearching;
    private RadioGroup radioGroupLocation1, radioGroupLocation2, radioGroupGender;
    private AlertDialog fieldValueChangeDialog;

    private ArrayList<String> topicList;
    private final Map<String, Boolean> userSearchAvailability = new HashMap<>();
    private List<String> idsForBlindDate = new ArrayList<>();
    private List<String> namesForBlindDate = new ArrayList<>();
    private List<String> activeChatUsersID = new ArrayList<>();
    private List<Float> ageValues = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DocumentSnapshot userSnapshot, otherUserSnapshot, otherUserSnapshot1, otherUserSnapshot2, otherUserSnapshot3;
    private DocumentReference docRefBlindDates;
    private APIService apiService;


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

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

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
                layoutToDimWhenSearching.setAlpha(0.15f);
                waitUntilChatAppears.setVisibility(View.VISIBLE);
                try {
                    checkIfUserCanSearch(false);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        buttonBlindDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                buttonChat.setEnabled(false);
                buttonBlindDate.setEnabled(false);
                layoutToDimWhenSearching.setAlpha(0.15f);
                waitUntilChatAppears.setVisibility(View.VISIBLE);
                try {
                    checkIfUserCanSearch(true);
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
    private void searchForUser(int minAge, int maxAge, int radius, boolean isFemale, boolean blindDatesSearching) {

        String countryCodeLocal = "world";

        float degreeFor10km = 0.1f;
        float degreeFor25km = 0.23f;
        float degreeFor100km = 0.95f;
        float degreeFor250km = 2.25f;
        float degreeForWorld = 10.0f;

        switch (radius) {
            case 10000:
                minLatitude = latitudeUser - degreeFor10km;
                minLongitude = longitudeUser - degreeFor10km;
                maxLatitude = latitudeUser + degreeFor10km;
                maxLongitude = longitudeUser + degreeFor10km;
                countryCodeLocal = countryCode;
                break;
            case 25000:
                minLatitude = latitudeUser - degreeFor25km;
                minLongitude = longitudeUser - degreeFor25km;
                maxLatitude = latitudeUser + degreeFor25km;
                maxLongitude = longitudeUser + degreeFor25km;
                countryCodeLocal = countryCode;
                break;
            case 100000:
                minLatitude = latitudeUser - degreeFor100km;
                minLongitude = longitudeUser - degreeFor100km;
                maxLatitude = latitudeUser + degreeFor100km;
                maxLongitude = longitudeUser + degreeFor100km;
                countryCodeLocal = countryCode;
                break;
            case 250000:
                minLatitude = latitudeUser - degreeFor250km;
                minLongitude = longitudeUser - degreeFor250km;
                maxLatitude = latitudeUser + degreeFor250km;
                maxLongitude = longitudeUser + degreeFor250km;
                countryCodeLocal = countryCode;
                break;
            case 500000:
                Random r1 = new Random();
                minLatitude = latitudeUser - r1.nextInt(5);
                minLongitude = longitudeUser - r1.nextInt(5);
                maxLatitude = latitudeUser + r1.nextInt(5);
                maxLongitude = longitudeUser + r1.nextInt(5);
                countryCodeLocal = countryCode;
                break;
            default:
                Random r2 = new Random();
                int plusOrMinus = r2.nextInt(2);
                int randomLatitude, randomLongitude;

                // if statement for which south or north because number in latitude field or longitude can be minus

                if (plusOrMinus > 0) {
                    randomLatitude = r2.nextInt(66);
                } else {
                    randomLatitude = r2.nextInt(46) * -1;
                }

                plusOrMinus = r2.nextInt(2);

                if (plusOrMinus > 0) {
                    randomLongitude = r2.nextInt(166);
                } else {
                    randomLongitude = r2.nextInt(146) * -1;
                }

                minLatitude = randomLatitude - degreeForWorld;
                minLongitude = randomLongitude - degreeForWorld;
                maxLatitude = randomLatitude + degreeForWorld;
                maxLongitude = randomLongitude + degreeForWorld;

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
        int moreRandomSearching = r.nextInt(12 - 1) + 1;
        int randomAgeFromChosenRange = r.nextInt(maxAge - minAge) + minAge;
        int randomLimitSize = r.nextInt(101 - 50) + 50;
        String agePickedFromRange = Integer.toString(minAge);
        Query queryUser = null;

        if (!blindDatesSearching) {
            // Randomizing obtaining list of users from certain range
            if (moreRandomSearching <= 3) {
                queryUser = FirebaseFirestore.getInstance().collection("search").document(countryCodeLocal).collection("gender").document(genderForSearch).collection("age")
                        .document(agePickedFromRange).collection("users").orderBy("latitude").startAt(minLatitude).whereArrayContainsAny("topicsList", topicList).limit(randomLimitSize);
            } else if (moreRandomSearching <= 6) {
                queryUser = FirebaseFirestore.getInstance().collection("search").document(countryCodeLocal).collection("gender").document(genderForSearch).collection("age")
                        .document(agePickedFromRange).collection("users").orderBy("longitude").startAt(minLongitude).whereArrayContainsAny("topicsList", topicList).limit(randomLimitSize);
            } else if (moreRandomSearching <= 9) {
                queryUser = FirebaseFirestore.getInstance().collection("search").document(countryCodeLocal).collection("gender").document(genderForSearch).collection("age")
                        .document(agePickedFromRange).collection("users").orderBy("latitude").endAt(maxLatitude).whereArrayContainsAny("topicsList", topicList).limitToLast(randomLimitSize);
            } else {
                queryUser = FirebaseFirestore.getInstance().collection("search").document(countryCodeLocal).collection("gender").document(genderForSearch).collection("age")
                        .document(agePickedFromRange).collection("users").orderBy("longitude").endAt(maxLongitude).whereArrayContainsAny("topicsList", topicList).limitToLast(randomLimitSize);
            }
        } else {
            // Randomizing obtaining list of users from certain range
            if (moreRandomSearching <= 3) {
                queryUser = FirebaseFirestore.getInstance().collection("search").document(countryCodeLocal).collection("gender").document(genderForSearch).collection("age")
                        .document(agePickedFromRange).collection("users").orderBy("latitude").startAt(minLatitude).whereEqualTo("userWantsBlindDates", true).limit(randomLimitSize);
            } else if (moreRandomSearching <= 6) {
                queryUser = FirebaseFirestore.getInstance().collection("search").document(countryCodeLocal).collection("gender").document(genderForSearch).collection("age")
                        .document(agePickedFromRange).collection("users").orderBy("longitude").startAt(minLongitude).whereEqualTo("userWantsBlindDates", true).limit(randomLimitSize);
            } else if (moreRandomSearching <= 9) {
                queryUser = FirebaseFirestore.getInstance().collection("search").document(countryCodeLocal).collection("gender").document(genderForSearch).collection("age")
                        .document(agePickedFromRange).collection("users").orderBy("latitude").endAt(maxLatitude).whereEqualTo("userWantsBlindDates", true).limitToLast(randomLimitSize);
            } else {
                queryUser = FirebaseFirestore.getInstance().collection("search").document(countryCodeLocal).collection("gender").document(genderForSearch).collection("age")
                        .document(agePickedFromRange).collection("users").orderBy("longitude").endAt(maxLongitude).whereEqualTo("userWantsBlindDates", true).limitToLast(randomLimitSize);
            }

        }


        queryUser.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                Log.d(TAG, "USERS NOT FOUND");

                                if (nrOfAttemptsToFindSomeone < 8) {
                                    nrOfAttemptsToFindSomeone++;
                                    searchForUser(minAge, maxAge, radius, isFemale, blindDatesSearching);
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
                                    lastLoginDateOfSearchedUser = document.getDate(LAST_LOGIN_DATE);
                                    Date currentDate = new Date(new Date().getTime());
                                    long dateDiff = currentDate.getTime() - lastLoginDateOfSearchedUser.getTime();
                                    int maxNrOfDaysSinceLogin = 5;
                                    int nrOfDaysSinceLastLogin = (int) (dateDiff / (1000 * 60 * 60 * 24));

                                    if (idOfOtherUser.equals(user.getUid())) {

                                        Log.d(TAG, "User who started searching searched himself/herself ");

                                    } else if (activeChatUsersID.contains(idOfOtherUser) && !blindDatesSearching) {

                                        Log.d(TAG, "User who started searching searched have active chat with that person");

                                    } else if (nrOfDaysSinceLastLogin > maxNrOfDaysSinceLogin) {

                                        Log.d(TAG, "User have not used an app since more than " + nrOfDaysSinceLastLogin + " days");

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

                                Log.d(TAG, "SearchedUsersByQuery" + idOfOtherUsers.toString());
                                int nrOfFoundUsers = idOfOtherUsers.size();

                                if (nrOfFoundUsers > 0 && !blindDatesSearching) {

                                    getUsersNames(idOfOtherUsers, false);

                                } else if (nrOfFoundUsers <= 0 && !blindDatesSearching) {

                                    Toast.makeText(getContext(), "User with such parameters not found, try again with different parameters", Toast.LENGTH_SHORT).show();
                                    nrOfAttemptsToFindSomeone = 0;
                                    idOfOtherUsers.clear();
                                    waitUntilChatAppears.setVisibility(View.INVISIBLE);
                                    layoutToDimWhenSearching.setAlpha(1f);
                                    buttonChat.setEnabled(true);
                                    buttonBlindDate.setEnabled(true);

                                } else if (nrOfFoundUsers > 2) {

                                    getUsersNames(idOfOtherUsers, true);

                                } else {

                                    Log.d(TAG, "ENOUGH USERS NOT FOUND");
                                    Toast.makeText(getContext(), "Users with such parameters not found, try again with different parameters", Toast.LENGTH_SHORT).show();
                                    nrOfAttemptsToFindSomeone = 0;
                                    idOfOtherUsers.clear();
                                    waitUntilChatAppears.setVisibility(View.INVISIBLE);
                                    layoutToDimWhenSearching.setAlpha(1f);
                                    buttonChat.setEnabled(true);
                                    buttonBlindDate.setEnabled(true);

                                }
                            }
                        }
                    }
                });

    }

    // Create Chat Activity
    private void createChat() {
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
                        chat.put("wasUserInActivityNr0", true);
                        chat.put("wasUserInActivityNr1", false);

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
                        user1.put("lastTimeInChatActivity", new Date());
                        Log.d(TAG, user1.toString());
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
                        user2.put("lastTimeInChatActivity", new Date());
                        Log.d(TAG, user2.toString());
                        db.collection("chats").document(chatID).collection("chatUsers").document(idOfOtherUser).set(user2);

                        if (nrOfUserBlindDates == 0) {
                            db.collection("users").document(user.getUid()).update(LAST_DATE_OF_STARTED_CHAT, new Date());
                        }
                        db.collection("users").document(user.getUid()).update(NR_OF_STARTED_CHATS_TODAY, FieldValue.increment(1));

                        db.collection("users").document(user.getUid()).update(ARRAY_OF_USERS_WITH_ACTIVE_CONVERSATIONS, FieldValue.arrayUnion(idOfOtherUser));

                        db.collection("users").document(idOfOtherUser).update(ARRAY_OF_USERS_WITH_ACTIVE_CONVERSATIONS, FieldValue.arrayUnion(user.getUid()));


                        db.collection("chats").document(chatID).set(chat)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        sendNotificationAboutChat(idOfOtherUser, user.getUid(), "New conversation just started with " + nameOfUser
                                                , nameOfOtherUser, nameOfUser, chatID);

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
    private void getUsersNames(ArrayList<String> id, boolean blindDatesSearching) {

        Random userRandom = new Random();
        randomUser1 = userRandom.nextInt(id.size());
        randomUser2 = userRandom.nextInt(id.size());
        randomUser3 = userRandom.nextInt(id.size());

        if (blindDatesSearching) {

            while (randomUser1 == randomUser2 || randomUser2 == randomUser3 || randomUser1 == randomUser3) {
                randomUser1 = userRandom.nextInt(id.size());
                randomUser2 = userRandom.nextInt(id.size());
                randomUser3 = userRandom.nextInt(id.size());
            }

            namesForBlindDate.add(nameOfUser);
            idsForBlindDate.add(user.getUid());

            docRefBlindDates = db.collection("users").document(id.get(randomUser1));

            docRefBlindDates.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task1) {
                    otherUserSnapshot1 = task1.getResult();
                    namesForBlindDate.add(otherUserSnapshot1.getString("displayName"));
                    idsForBlindDate.add(id.get(randomUser1));
                    docRefBlindDates = db.collection("users").document(id.get(randomUser2));
                    docRefBlindDates.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                            otherUserSnapshot2 = task2.getResult();
                            namesForBlindDate.add(otherUserSnapshot2.getString("displayName"));
                            idsForBlindDate.add(id.get(randomUser2));
                            docRefBlindDates = db.collection("users").document(id.get(randomUser3));
                            docRefBlindDates.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task3) {
                                    otherUserSnapshot3 = task3.getResult();
                                    namesForBlindDate.add(otherUserSnapshot3.getString("displayName"));
                                    idsForBlindDate.add(id.get(randomUser3));
                                    Log.d(TAG, namesForBlindDate.toString() + idsForBlindDate.toString());
                                    questionsForBlindDateDialog();


                                }
                            });
                        }
                    });
                }
            });

        } else {

            idOfOtherUser = id.get(randomUser1);
            DocumentReference docRef2 = db.collection("users").document(idOfOtherUser);

            docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                    otherUserSnapshot = task2.getResult();
                    nameOfOtherUser = otherUserSnapshot.getString("displayName");
                    createChat();
                }
            });
        }
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

    public void tooManyTimesStartedToday(boolean isSearchingForBlindDates) {

        layoutToDimWhenSearching.setAlpha(1f);
        waitUntilChatAppears.setVisibility(View.INVISIBLE);
        LayoutInflater inflaterDialog = LayoutInflater.from(getContext());
        View alertNotFiledProfileDialogView = inflaterDialog.inflate(R.layout.dialog_search_not_available, null);
        TextView notFiledProfileAlertTitle = alertNotFiledProfileDialogView.findViewById(R.id.title_of_search_availability_box);
        if (isSearchingForBlindDates) {
            notFiledProfileAlertTitle.setText("You have reached max amount of Blind Dates You can start per day (5). If You want to start more You can get premium account with 20 starts per day!");

        } else {
            notFiledProfileAlertTitle.setText("You have reached max amount of chats You can start per day (5). If You want to write more You can get premium account with 20 starts per day!");

        }

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

    // This functions show dialog boxes where user can input specific data
    private void questionsForBlindDateDialog() {

        // Layout dialog box
        LayoutInflater inflaterDialog = LayoutInflater.from(getContext());
        View dialogView = inflaterDialog.inflate(R.layout.dialog_blind_date_questions_start, null);
        EditText editDialogText1 = dialogView.findViewById(R.id.editText_question1_blind_date);
        EditText editDialogText2 = dialogView.findViewById(R.id.editText_question2_blind_date);
        EditText editDialogText3 = dialogView.findViewById(R.id.editText_question3_blind_date);

        InputFilter lengthFilter = new InputFilter.LengthFilter(500);
        editDialogText1.setHint("Question nr 1 (20-500 letters)");
        editDialogText1.setInputType(InputType.TYPE_CLASS_TEXT);
        editDialogText1.setFilters(new InputFilter[]{lengthFilter});
        editDialogText2.setHint("Question nr 2 (20-500 letters)");
        editDialogText2.setInputType(InputType.TYPE_CLASS_TEXT);
        editDialogText2.setFilters(new InputFilter[]{lengthFilter});
        editDialogText3.setHint("Question nr 3 (20-500 letters)");
        editDialogText3.setInputType(InputType.TYPE_CLASS_TEXT);
        editDialogText3.setFilters(new InputFilter[]{lengthFilter});

        fieldValueChangeDialog = new AlertDialog.Builder(getContext())
                .setTitle("Three questions for Blind Dates")
                .setView(dialogView)  // What to use in dialog box
                .setNegativeButton(R.string.no_text_dialog_boxes, null)
                .setPositiveButton("OK", null)
                .show();

        fieldValueChangeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Clicked positive!");
                List<String> questionsForBlindDate = new ArrayList<>();
                questionsForBlindDate.add("1. " + editDialogText1.getText().toString());
                questionsForBlindDate.add("2. " + editDialogText2.getText().toString());
                questionsForBlindDate.add("3. " + editDialogText3.getText().toString());


                if (questionsForBlindDate.get(0).length() < 20 || questionsForBlindDate.get(1).length() < 20
                        || questionsForBlindDate.get(2).length() < 20) {
                    Toast.makeText(getContext(), getString(R.string.wrong_input_info_profile_activity), Toast.LENGTH_SHORT).show();
                } else {

                    createBlindDate(namesForBlindDate, idsForBlindDate, questionsForBlindDate);

                }
            }
        });
    }

    private void checkIfUserCanSearch(boolean blindDatesSearching) throws ExecutionException, InterruptedException {
        // Download document of current user, to retrieve actual info to profile view
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {

                        isPremium = document.getBoolean(PREMIUM);
                        int maxAmountOfStartedConversationsAndBlindDates = 5;
                        if (isPremium) {
                            maxAmountOfStartedConversationsAndBlindDates = 20;
                        }

                        activeChatUsersID = (ArrayList<String>) document.get("otherUsersIDFromStartedChatsArray");
                        canUserSearch = document.getBoolean(CAN_USER_SEARCH);
                        locationUser = document.getGeoPoint(LOCATION_FIELD);
                        latitudeUser = (float) locationUser.getLatitude();
                        longitudeUser = (float) locationUser.getLongitude();
                        topicList = (ArrayList<String>) document.get(TOPICS_ARRAY);
                        countryCode = document.getString(COUNTRY_CODE_FIELD);
                        nameOfUser = document.getString(NAME_FIELD);
                        firstPhotoUriOfUser = document.getString(FIRST_PHOTO_URI);
                        nrOfUserChats = document.getDouble(NR_OF_STARTED_CHATS_TODAY).intValue();
                        nrOfUserBlindDates = document.getDouble(NR_OF_STARTED_BLIND_DATES_TODAY).intValue();
                        blindDateParticipationWill = document.getBoolean(BLIND_DATE_PARTICIPATION_WILL);

                        // Check if time from first started chat was more than 24 hours and reset the countdown to zero
                        lastDateOfStartedBlindDate = document.getDate(LAST_DATE_OF_STARTED_BLIND_DATE);
                        lastDateOfStartedChat = document.getDate(LAST_DATE_OF_STARTED_CHAT);
                        Date currentDate = new Date(new Date().getTime());
                        long dateDiffChat = currentDate.getTime() - lastDateOfStartedChat.getTime();
                        long dateDiffBlindDate = currentDate.getTime() - lastDateOfStartedBlindDate.getTime();
                        int hoursToReset = 24;
                        int nrOfHoursSinceFirstStartedChat = (int) (dateDiffChat / (1000 * 60 * 60));
                        int nrOfHoursSinceFirstStartedBlindDate = (int) (dateDiffBlindDate / (1000 * 60 * 60));

                        Log.d(TAG, String.valueOf(nrOfUserChats) + "   " + String.valueOf(nrOfUserBlindDates));

                        if (nrOfHoursSinceFirstStartedChat >= hoursToReset) {
                            nrOfUserChats = 0;
                            db.collection("users").document(user.getUid()).update(NR_OF_STARTED_CHATS_TODAY, FieldValue.increment(-nrOfUserChats));

                        }
                        if (nrOfHoursSinceFirstStartedBlindDate >= hoursToReset) {
                            nrOfUserBlindDates = 0;
                            db.collection("users").document(user.getUid()).update(NR_OF_STARTED_BLIND_DATES_TODAY, FieldValue.increment(-nrOfUserBlindDates));

                        }

                        if (canUserSearch && blindDatesSearching && nrOfUserBlindDates <= maxAmountOfStartedConversationsAndBlindDates) {

                            if (!blindDateParticipationWill) {
                                Toast.makeText(getContext(), "You don't want to participate in Blind Dates according to Your profile, if You changed Your mind" +
                                        "check Your profile and update Your will", Toast.LENGTH_SHORT).show();
                                waitUntilChatAppears.setVisibility(View.INVISIBLE);
                                layoutToDimWhenSearching.setAlpha(1f);
                            } else {
                                if (ageValues.get(1) - ageValues.get(0) > 15 || ageValues.get(1) - ageValues.get(0) < 5) {
                                    Toast.makeText(getContext(), "Age range can be set between 5 to 15 years difference.", Toast.LENGTH_SHORT).show();
                                    waitUntilChatAppears.setVisibility(View.INVISIBLE);
                                    layoutToDimWhenSearching.setAlpha(1f);
                                    buttonChat.setEnabled(true);
                                    buttonBlindDate.setEnabled(true);
                                } else if (radioGroupGender.getCheckedRadioButtonId() == -1 || (radioGroupLocation1.getCheckedRadioButtonId() == -1 && radioGroupLocation2.getCheckedRadioButtonId() == -1)) {
                                    Toast.makeText(getContext(), "Check gender of user you are searching and radius where to search for them.", Toast.LENGTH_SHORT).show();
                                    waitUntilChatAppears.setVisibility(View.INVISIBLE);
                                    layoutToDimWhenSearching.setAlpha(1f);
                                    buttonChat.setEnabled(true);
                                    buttonBlindDate.setEnabled(true);
                                } else {
                                    searchForUser((int) Math.round(ageValues.get(0)), (int) Math.round(ageValues.get(1)), radiusToSearch, isFemaleToSearch, blindDatesSearching);
                                }
                            }


                        } else if (canUserSearch && !blindDatesSearching && nrOfUserChats <= maxAmountOfStartedConversationsAndBlindDates) {

                            if (ageValues.get(1) - ageValues.get(0) > 15 || ageValues.get(1) - ageValues.get(0) < 5) {
                                Toast.makeText(getContext(), "Age range can be set between 5 to 15 years difference.", Toast.LENGTH_SHORT).show();
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
                                searchForUser((int) Math.round(ageValues.get(0)), (int) Math.round(ageValues.get(1)), radiusToSearch, isFemaleToSearch, blindDatesSearching);
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

                            tooManyTimesStartedToday(blindDatesSearching);

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

    private void sendNotificationAboutChat(String otherUserID, final String userID, final String message,
                                           final String creatorName, final String otherUserName, final String chatID) {
        DocumentReference docRefUser = db.collection("users").document(otherUserID);

        docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    otherUserSnapshot = task.getResult();
                    if (otherUserSnapshot != null) {

                        String token = otherUserSnapshot.getString("fcmRegistrationToken");
                        Data data = new Data(otherUserID, R.drawable.notification_icon_white, message,
                                otherUserName, userID, creatorName, chatID);
                        Sender sender = new Sender(data, token);

                        apiService.sendNotification(sender).enqueue(new Callback<Response>() {
                            @Override
                            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                                Log.d(TAG, String.valueOf(response.code()));
                                if (response.code() == 200) {
                                    assert response.body() != null;
                                    if (response.body().success != 1) {
                                        Log.d(TAG, "Failure while sending notification.");
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<Response> call, Throwable t) {

                            }
                        });

                    } else {
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }

            }
        });
    }

    private void createBlindDate(List<String> names, List<String> ids, List<String> questions) {
        Map<String, Object> blindDate = new HashMap<>();
        Map<String, Object> user0 = new HashMap<>();
        Map<String, Object> user1 = new HashMap<>();
        Map<String, Object> user2 = new HashMap<>();
        Map<String, Object> user3 = new HashMap<>();
        Map<String, Object> message1 = new HashMap<>();
        Map<String, Object> message2 = new HashMap<>();
        Map<String, Object> message3 = new HashMap<>();

        db.collection("blindDates").add(blindDate)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        blindDateID = documentReference.getId();
                        blindDate.put("blindDateID", blindDateID);
                        blindDate.put("dateOfBlindDateCreation", new Date());
                        blindDate.put("dateOfRound2Started", new Date());
                        blindDate.put("lastMessageDate", new Date());
                        blindDate.put("usersParticipatingThatHaveNotDeletedBlindDates", ids);
                        blindDate.put("userNameNr0", names.get(0));
                        blindDate.put("userNameNr1", names.get(1));
                        blindDate.put("userNameNr2", names.get(2));
                        blindDate.put("userNameNr3", names.get(3));
                        blindDate.put("userIDNr0", ids.get(0));
                        blindDate.put("userIDNr1", ids.get(1));
                        blindDate.put("userIDNr2", ids.get(2));
                        blindDate.put("userIDNr3", ids.get(3));
                        blindDate.put("wasUserRejectedNr1", false);
                        blindDate.put("wasUserRejectedNr2", false);
                        blindDate.put("wasUserRejectedNr3", false);
                        blindDate.put("numberOfRoundInBlindDate", 0);
                        blindDate.put("wasUserInActivityNr0", false);
                        blindDate.put("wasUserInActivityNr1", false);
                        blindDate.put("wasUserInActivityNr2", false);
                        blindDate.put("wasUserInActivityNr3", false);

                        user0.put("userID", ids.get(0));
                        user0.put("userName", names.get(0));
                        user0.put("lastTimeInChatActivity", new Date(new Date().getTime() - 86400000L));
                        db.collection("blindDates").document(blindDateID).collection("blindDateUsers")
                                .document(ids.get(0)).set(user0);

                        user1.put("userID", ids.get(1));
                        user1.put("userName", names.get(1));
                        user1.put("lastTimeInChatActivity", new Date(new Date().getTime() - 86400000L));
                        db.collection("blindDates").document(blindDateID).collection("blindDateUsers")
                                .document(ids.get(1)).set(user1);

                        user2.put("userID", ids.get(2));
                        user2.put("userName", names.get(2));
                        user2.put("lastTimeInChatActivity", new Date(new Date().getTime() - 86400000L));
                        db.collection("blindDates").document(blindDateID).collection("blindDateUsers")
                                .document(ids.get(2)).set(user2);

                        user3.put("userID", ids.get(3));
                        user3.put("userName", names.get(3));
                        user3.put("lastTimeInChatActivity", new Date(new Date().getTime() - 86400000L));
                        db.collection("blindDates").document(blindDateID).collection("blindDateUsers")
                                .document(ids.get(3)).set(user3);

                        message1.put("textOfMessage", questions.get(0));
                        message1.put("userName", names.get(0));
                        message1.put("userID", ids.get(0));
                        message1.put("whatRoundSend", 0);
                        message1.put("typeOfMessage", 0);
                        message1.put("dateOfMessage", new Date());
                        db.collection("blindDates").document(blindDateID).collection("messages").add(message1);

                        message2.put("textOfMessage", questions.get(1));
                        message2.put("userName", names.get(0));
                        message2.put("userID", ids.get(0));
                        message2.put("whatRoundSend", 0);
                        message2.put("typeOfMessage", 0);
                        message2.put("dateOfMessage", new Date());
                        db.collection("blindDates").document(blindDateID).collection("messages").add(message2);

                        message3.put("textOfMessage", questions.get(2));
                        message3.put("userName", names.get(0));
                        message3.put("userID", ids.get(0));
                        message3.put("whatRoundSend", 0);
                        message3.put("typeOfMessage", 0);
                        message3.put("dateOfMessage", new Date());
                        db.collection("blindDates").document(blindDateID).collection("messages").add(message3);

                        if (nrOfUserBlindDates == 0) {
                            db.collection("users").document(user.getUid()).update(LAST_DATE_OF_STARTED_BLIND_DATE, new Date());
                        }

                        db.collection("users").document(user.getUid()).update(NR_OF_STARTED_BLIND_DATES_TODAY, FieldValue.increment(1));

                        db.collection("blindDates").document(blindDateID).set(blindDate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

//                                        sendNotificationAboutChat(idOfOtherUser, user.getUid(),"New conversation just started with " + nameOfUser
//                                                ,nameOfOtherUser, nameOfUser, chatID);

                                        ids.clear();
                                        names.clear();
                                        fieldValueChangeDialog.dismiss();
                                        Intent intent = new Intent(getActivity(), BlindDateActivity.class);
                                        intent.putExtra("blindDateMap", (Serializable) blindDate);
                                        requireActivity().startActivity(intent);
                                        waitUntilChatAppears.setVisibility(View.INVISIBLE);
                                        layoutToDimWhenSearching.setAlpha(1f);
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


}




