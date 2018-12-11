package demofon.example.com.opener.login;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;

import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import demofon.example.com.opener.BuildConfig;
import demofon.example.com.opener.MainActivity;

public class AuthBuild {

    static void createRequest(Context context) {
        AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                Uri.parse(LoginConstants.AUTHORIZATION_ENDPOINT) /* auth endpoint */,
                Uri.parse(LoginConstants.TOKEN_ENDPOINT) /* token endpoint */
        );
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                serviceConfiguration,
                BuildConfig.API_ID_AUTH,
                ResponseTypeValues.CODE,
                LoginConstants.REDIRECT_URI
        );
        AuthorizationRequest request = builder
                .setCodeVerifier(null, null, null)
                .build();

        AuthorizationService authService = new AuthorizationService(context);
        authService.performAuthorizationRequest(
                request,
                PendingIntent.getActivity(context,
                        0,
                        new Intent(context, MainActivity.class),   //positive
                        0),
                PendingIntent.getActivity(context,
                        0,
                        new Intent(context, LoginActivity.class),   //negative
                        0)
        );
    }

    public static boolean isInternetConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
