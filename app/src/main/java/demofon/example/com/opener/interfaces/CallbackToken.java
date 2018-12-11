package demofon.example.com.opener.interfaces;

import net.openid.appauth.AuthState;

public interface CallbackToken {
    void onSuccess(AuthState authState);

    void onError(String error);
}
