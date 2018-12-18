package demofon.example.com.opener.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import net.openid.appauth.AuthState;

import org.json.JSONException;

import demofon.example.com.opener.constants.Constants;

public class AuthStateLogin {

    private static final String AUTH_STATE = "AUTH_STATE";

    @NonNull
    private Context context;

    public AuthStateLogin(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    public AuthState readAuthState() {
        SharedPreferences authPrefs = context.getSharedPreferences(Constants.AUTH_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String stateStr = authPrefs.getString(AUTH_STATE, null);
        if (!TextUtils.isEmpty(stateStr)) {
            try {
                return AuthState.jsonDeserialize(stateStr);
            } catch (JSONException e) {
                Log.w("AUTH", e.getMessage(), e);
            }
        }
        return new AuthState();
    }

    public void writeAuthState(@NonNull AuthState state) {
        SharedPreferences authPrefs = context.getSharedPreferences(Constants.AUTH_PREFERENCES_NAME, Context.MODE_PRIVATE);
        authPrefs.edit()
                .putString(AUTH_STATE, state.jsonSerializeString())
                .apply();
    }

    public void clearAuthState() {
        context.getSharedPreferences(Constants.AUTH_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(AUTH_STATE)
                .apply();
    }

}
