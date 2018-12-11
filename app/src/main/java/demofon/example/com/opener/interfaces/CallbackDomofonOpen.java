package demofon.example.com.opener.interfaces;

import net.openid.appauth.AuthState;


public interface CallbackDomofonOpen {
    void onSuccess();

    void onRefresh(AuthState authState);

    void onServerError();

    void onError(String error);
}
