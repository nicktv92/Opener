package demofon.example.com.opener.domofon;

import android.content.Context;
import android.support.annotation.NonNull;

import com.auth0.android.jwt.JWT;

import java.util.List;

import demofon.example.com.opener.interfaces.CallbackDomofonList;
import demofon.example.com.opener.interfaces.CallbackDomofonOpen;
import demofon.example.com.opener.interfaces.DomofonService;
import demofon.example.com.opener.interfaces.OpenerService;
import demofon.example.com.opener.login.AuthStateLogin;
import demofon.example.com.opener.constants.LoginConstants;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DomofonRequest {

    public static void getList(final String token, final CallbackDomofonList domofonCallback) {
        new Thread(new Runnable() {
            public void run() {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(LoginConstants.API_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                DomofonService service = retrofit.create(DomofonService.class);

                Call<List<DomofonList>> call = service.getDomofon("Bearer " + token);
                call.enqueue(new Callback<List<DomofonList>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<DomofonList>> call, @NonNull Response<List<DomofonList>> response) {
                        switch (response.code()) {
                            case 200:
                                domofonCallback.onSuccess(response.body());
                                break;
                            case 401:
                                domofonCallback.onRefresh();
                                break;
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<DomofonList>> call, @NonNull Throwable t) {
                        domofonCallback.onError(t.getMessage());
                    }
                });
            }
        }).start();
    }

    public static void openDomofon(Context context, String id, final CallbackDomofonOpen open) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(LoginConstants.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        OpenerService service = retrofit.create(OpenerService.class);
        final AuthStateLogin token = new AuthStateLogin(context);
        JWT jwt = new JWT(token.readAuthState().getAccessToken());
        Call<ResponseBody> call = service.open("Bearer " + jwt.getId(), id);
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                switch (response.code()) {
                    case 200:
                        open.onSuccess();
                        break;
                    case 401:
                        open.onRefresh(token.readAuthState());
                        break;
                    case 500:
                        open.onServerError();
                        break;
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                open.onError(t.getMessage());
            }
        });
    }
}