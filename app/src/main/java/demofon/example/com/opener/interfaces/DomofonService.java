package demofon.example.com.opener.interfaces;

import java.util.List;

import demofon.example.com.opener.domofon.DomofonList;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;

public interface DomofonService {
    @Headers({
            "Content-Type: application/json;charset=utf-8",
            "User-Agent: student-app"
    })
    @GET("domofon/relays?pagesize=20&pagination=1")
    Call<List<DomofonList>> getDomofon(@Header("Authorization") String token);

}
