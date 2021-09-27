package pl.makorz.discussed.Controllers.Notifications;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import pl.makorz.discussed.Controllers.BlindDateActivity;
import pl.makorz.discussed.Controllers.ChatActivity;

public class FirebaseMessaging extends FirebaseMessagingService {

    public String TAG = "FirebaseMessaging";
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String userTargetedID = remoteMessage.getData().get("userTargetedID");
        int type = Integer.parseInt(remoteMessage.getData().get("typeData"));

        if (currentUser != null && userTargetedID.equals(currentUser.getUid())) {
            if (type == 1) {
                sendOreoNotificationB(remoteMessage);
            } else {
                sendOreoNotification(remoteMessage);
            }
        }

    }

    private void sendOreoNotification(RemoteMessage remoteMessage) {

        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String whoSendID = remoteMessage.getData().get("whoSendID");
        String whoSendName = remoteMessage.getData().get("whoSendName");
        String chatID = remoteMessage.getData().get("chatID");

        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("idOfOtherUser", whoSendID);
        bundle.putString("otherUserName", whoSendName);
        bundle.putString("chatIdIntent", chatID);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, whoSendID.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String groupKey = "groupNotifications";

        OreoNotification oreoNotification = new OreoNotification(this);
        NotificationCompat.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent, icon, groupKey);

        oreoNotification.getManager().notify(whoSendID.hashCode(), builder.build());

    }

    private void sendOreoNotificationB(RemoteMessage remoteMessage) {

        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String whoSendID = remoteMessage.getData().get("whoSendID");

        Map<String, String> map = new HashMap<>();
        map.put("blindDateID", remoteMessage.getData().get("blindDateID"));
        map.put("fromNotification", String.valueOf(true));

        Intent intent = new Intent(this, BlindDateActivity.class);
        intent.putExtra("blindDateMap", (Serializable) map);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, whoSendID.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String groupKey = "groupNotifications";

        OreoNotification oreoNotification = new OreoNotification(this);
        NotificationCompat.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent, icon, groupKey);

        oreoNotification.getManager().notify(whoSendID.hashCode(), builder.build());

    }

    @Override
    public void onNewToken(@NonNull String s) {
        if (currentUser != null) {
            super.onNewToken(s);
            DocumentReference docRef = db.collection("users").document(currentUser.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            int ageUser = document.getDouble("age").intValue();
                            if (ageUser >= 0) {
                                com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                    @Override
                                    public void onComplete(@NonNull Task<String> task) {
                                        if (!task.isSuccessful()) {
                                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                            return;
                                        }
                                        // Get new FCM registration token
                                        String token = task.getResult();
                                        db.collection("users").document(currentUser.getUid()).update("fcmRegistrationToken", token);
                                    }
                                });
                            }

                        }
                    }
                }
            });
        }
    }
}
