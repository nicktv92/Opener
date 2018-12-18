package demofon.example.com.opener.beacon;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.Toast;

import net.openid.appauth.AuthState;

import demofon.example.com.opener.R;
import demofon.example.com.opener.domofon.DomofonRequest;
import demofon.example.com.opener.interfaces.CallbackDomofonOpen;
import demofon.example.com.opener.interfaces.CallbackToken;
import demofon.example.com.opener.login.AuthStateLogin;
import demofon.example.com.opener.login.AuthToken;


public class OpenDomofonService extends IntentService {

    private String mDomofonId;

    public OpenDomofonService() {
        super("OpenDomofonService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mDomofonId = intent.getStringExtra("domofon");
        openDomofon();
    }

    private void openDomofon(){
        DomofonRequest.openDomofon(this, mDomofonId, new CallbackDomofonOpen() {
            @Override
            public void onSuccess() {
                Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                OpenDomofonService.this.sendBroadcast(it);
                Toast.makeText(
                        OpenDomofonService.this,
                        getString(R.string.domofon_adapter_open),
                        Toast.LENGTH_SHORT)
                        .show();

            }

            @Override
            public void onRefresh(AuthState authState) {
                    refreshToken(authState);
            }

            @Override
            public void onServerError() {
                Toast.makeText(
                        OpenDomofonService.this,
                        getString(R.string.domofon_adapter_server),
                        Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onError(String error) {
                Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                OpenDomofonService.this.sendBroadcast(it);
                Toast.makeText(OpenDomofonService.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshToken(AuthState authState) {
        AuthToken.refreshToken(this, authState, new CallbackToken() {
            @Override
            public void onSuccess(AuthState authState) {
                AuthStateLogin stateLogin = new AuthStateLogin(OpenDomofonService.this);
                stateLogin.writeAuthState(authState);
                openDomofon();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(
                        OpenDomofonService.this,
                        getString(R.string.error_token) + error,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
