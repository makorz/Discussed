package pl.makorz.discussed.Controllers.Functions;

import pl.makorz.discussed.Controllers.Notifications.Response;
import pl.makorz.discussed.Controllers.Notifications.SenderB;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIServiceB {

    @Headers({
            "Authorization:key=AAAA1ywfeeg:APA91bFYDEfPRUdCidfO-I4F6PE1frB5zv3p4N0dxySwmBG65uwg17iaNDuGghLv04EnVM3_-NAJMRSdyoIm1BvsoAKXGkOZ8XCwwxKioJeYF7gva6cRYlrs28quw1Gh3OkUvYXa6hzj",
            "Content-Type:application/json"
    })

    @POST("fcm/send")
    Call<Response> sendNotification (@Body SenderB body);
}
