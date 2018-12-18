package demofon.example.com.opener.login;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretPost;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import demofon.example.com.opener.BuildConfig;
import demofon.example.com.opener.interfaces.CallbackToken;

public class AuthToken {

    public static void getToken(final Context context, Intent intent, final CallbackToken tokenCallback) {
        AuthorizationResponse resp = AuthorizationResponse.fromIntent(intent);
        AuthorizationException ex = AuthorizationException.fromIntent(intent);
        final AuthState authState = new AuthState(resp, ex);
        if (resp == null && ex != null) {
            Toast.makeText(context,
                    String.valueOf(ex.errorDescription),
                    Toast.LENGTH_SHORT).show();
        } else {
            ClientAuthentication clientAuth = new ClientSecretPost(BuildConfig.API_KEY_AUTH);  // client secret
            AuthorizationService authService = new AuthorizationService(context);
            authService.performTokenRequest(
                    resp.createTokenExchangeRequest(),
                    clientAuth,
                    new AuthorizationService.TokenResponseCallback() {
                        @Override
                        public void onTokenRequestCompleted(
                                TokenResponse resp, AuthorizationException ex) {
                            if (resp != null) {
                                authState.update(resp, ex);
                                tokenCallback.onSuccess(authState);
                            } else {
                                tokenCallback.onError(ex.error);
                            }
                        }
                    });
        }
    }

    public static void refreshToken(final Context context, final AuthState authState, final CallbackToken tokenCallback) {
        new Thread(new Runnable() {
            public void run() {
                AuthorizationService authService = new AuthorizationService(context);
                TokenRequest tokenRequest = authState.createTokenRefreshRequest();
                ClientAuthentication clientAuth = new ClientSecretPost(BuildConfig.API_KEY_AUTH);   // client secret
                authService.performTokenRequest(
                        tokenRequest,
                        clientAuth,
                        new AuthorizationService.TokenResponseCallback() {
                            @Override
                            public void onTokenRequestCompleted(
                                    TokenResponse resp, AuthorizationException ex) {
                                if (resp != null) {
                                    authState.update(resp, ex);
                                    tokenCallback.onSuccess(authState);
                                } else {
                                    tokenCallback.onError(ex.error);
                                }
                            }
                        });
            }
        }).start();
    }
}
