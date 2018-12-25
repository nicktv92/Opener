package demofon.example.com.opener.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import demofon.example.com.opener.MainActivity;
import demofon.example.com.opener.R;


public class LoginActivity extends AppCompatActivity {
    private ProgressBar mLoginBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Проверка авторизации пользователя
//        если пользователь авторизован, вызывается MainActivity

        AuthStateLogin stateLogin = new AuthStateLogin(this);
        if (stateLogin.readAuthState().isAuthorized()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }
        setContentView(R.layout.activity_login);
        mLoginBar = (ProgressBar) findViewById(R.id.loginProgressBar);
        final Button mLoginBtn = (Button) findViewById(R.id.loginBtn);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AuthBuild.isInternetConnected(LoginActivity.this)) {
                    newAuth();
                } else {
                    Snackbar.make(v, "No connection", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void newAuth() {
        mLoginBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            public void run() {
                AuthBuild.createRequest(LoginActivity.this);
            }
        }).start();
    }
}