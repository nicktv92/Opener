package demofon.example.com.opener.login;

import android.net.Uri;

public class LoginConstants {

    public static final String AUTHORIZATION_ENDPOINT = "https://id.is74.ru/authorize";
    public static final String TOKEN_ENDPOINT = "https://id.is74.ru/token";
    public static final String API_URL = "https://api.is74.ru";
    public static final Uri REDIRECT_URI = Uri.parse("com.example.demofon://app/oauth2redirect");

}
