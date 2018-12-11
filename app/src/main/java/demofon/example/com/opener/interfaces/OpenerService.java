package demofon.example.com.opener.interfaces;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OpenerService {
    @Headers({
            "Content-Type: application/json;charset=utf-8",
            "User-Agent: student-app"
    })
    @POST("domofon/relays/{id}/open")
    Call<ResponseBody> open(
            @Header("Authorization") String token,
            @Path("id") String id
    );
}
