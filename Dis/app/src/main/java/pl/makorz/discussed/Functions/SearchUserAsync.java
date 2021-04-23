package pl.makorz.discussed.Functions;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class SearchUserAsync extends AsyncTask<String,Integer,String> {

    String idOfSearchedUser;
    String userID;

    @Override
    protected String doInBackground(String... strings) {
        userID = strings[0];
        searchForUser();
        return idOfSearchedUser;
    }

    @Override
    protected void onPostExecute(String s) {


        super.onPostExecute(s);
    }

    private void searchForUser() {
        Query queryUser = FirebaseFirestore.getInstance().collection("search").whereNotEqualTo("idOfUser",userID).limit(1);
        queryUser.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                idOfSearchedUser = document.get("idOfUser").toString();

                            }

                        }
                    }
                });

    }

}
