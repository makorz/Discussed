package pl.makorz.discussed.Controllers.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import pl.makorz.discussed.Models.Adapters.CustomExpandableListAdapter;
import pl.makorz.discussed.R;

public class AboutFragment extends Fragment {

    private static final String TAG = "AboutFragment";
    private static final String VERSION_NR = "versionNr";
    private static final String GOOD_JOB_COUNTER = "nrOfGoodJobs";

    private LinkedHashMap<String, List<String>> expandableListDetail;
    private String versionNr;
    private TextView versionNrTextView, goodJobTextView;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Loading data from server in second thread
        new Thread() {
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            updateInfoTab();
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                });
            }
        }.start();

        View aboutLayout = inflater.inflate(R.layout.fragment_about, container, false);
        versionNrTextView = aboutLayout.findViewById(R.id.versionNr_text_view);
        goodJobTextView = aboutLayout.findViewById(R.id.goodJobNr_text_view);
        ExpandableListView expandableListView = (ExpandableListView) aboutLayout.findViewById(R.id.tutorial_list_view);
        populateList();
        List<String> expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());
        ExpandableListAdapter expandableListAdapter = new CustomExpandableListAdapter(getContext(), expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);
        return aboutLayout;

    }
    // This function updates information when activity is started
    private void updateInfoTab() throws ExecutionException, InterruptedException {
        // Download document of current user, to retrieve actual info to profile view
        DocumentReference docRef = db.collection("appInfo").document("informations");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {

                        versionNr = document.getString(VERSION_NR);

                        String version1 = getResources().getString(R.string.version_textView_about_tab);
                        String version2 = version1 + " " + versionNr;
                        versionNrTextView.setText(version2);

                        String pointNr = Integer.toString(document.getDouble(GOOD_JOB_COUNTER).intValue());
                        String points1 = getResources().getString(R.string.nr_of_goodJob_clicks_text_about_tab);
                        String points2 = points1 + " " + pointNr;
                        goodJobTextView.setHint(points2);

                    } else {
                        Log.d("LOGGER", "No such document");
                    }

                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });

    }

    // this function populates expandable list with information to show in about tab
    private void populateList() {

        expandableListDetail = new LinkedHashMap<String, List<String>>();

        List<String> tutorials = Arrays.asList(getResources().getStringArray(R.array.about_options));
        List<String> tutorialsAnswers = Arrays.asList(getResources().getStringArray(R.array.about_options_answers));
        List<String> zero = new ArrayList<String>();
        List<String> one = new ArrayList<String>();
        List<String> two = new ArrayList<String>();
        List<String> three = new ArrayList<String>();

        for (int i = 0; i < tutorials.size(); i++) {

            if (i == 0){
                zero.add(tutorialsAnswers.get(i));
                expandableListDetail.put(tutorials.get(i),zero);
            } else if (i == 1) {
                one.add(tutorialsAnswers.get(i));
                expandableListDetail.put(tutorials.get(i),one);
            } else if (i == 2) {
                two.add(tutorialsAnswers.get(i));
                expandableListDetail.put(tutorials.get(i),two);
            } else if (i == 3) {
                three.add(tutorialsAnswers.get(i));
                expandableListDetail.put(tutorials.get(i),three);
            }

        }
    }


}

