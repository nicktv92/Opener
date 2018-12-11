package demofon.example.com.opener;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;

import net.openid.appauth.AuthState;

import java.util.List;
import java.util.Objects;

import demofon.example.com.opener.beacon.BeaconActivity;
import demofon.example.com.opener.domofon.DomofonAdapter;
import demofon.example.com.opener.domofon.DomofonList;
import demofon.example.com.opener.domofon.DomofonRequest;
import demofon.example.com.opener.interfaces.CallbackDomofonList;
import demofon.example.com.opener.interfaces.CallbackToken;
import demofon.example.com.opener.login.AuthStateLogin;
import demofon.example.com.opener.login.AuthToken;
import demofon.example.com.opener.login.LoginActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private AuthStateLogin mStateLogin;
    private RecyclerView mDomofonRecyclerView;
    private JWT mJwt;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button mLogout, mBeacon;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLogout = (Button) findViewById(R.id.domofonBtnLogout);
        mBeacon = (Button) findViewById(R.id.domofonBtnBeacon);
        mDomofonRecyclerView = (RecyclerView) findViewById(R.id.domofonRecyclerV);
        mLayoutManager = new LinearLayoutManager(this);
        mDomofonRecyclerView.setLayoutManager(mLayoutManager);
        mStateLogin = new AuthStateLogin(this);
        mProgressDialog = new ProgressDialog(this);
        mLogout.setOnClickListener(this);
        mBeacon.setOnClickListener(this);

        if (mStateLogin.readAuthState().isAuthorized()) {
            mJwt = new JWT(Objects.requireNonNull(mStateLogin.readAuthState().getAccessToken()));
            getDomofonList(mJwt.getId());
        } else getToken();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.domofonBtnLogout:
                mStateLogin.clearAuthState();
                Intent login = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(login);
                finish();
                break;
            case R.id.domofonBtnBeacon:
                Intent intent = new Intent(this, BeaconActivity.class);
                startActivity(intent);
        }
    }

    private void getToken() {
        setProgress(true);
        AuthToken.getToken(this, getIntent(), new CallbackToken() {
            @Override
            public void onSuccess(AuthState authState) {
                mStateLogin.writeAuthState(authState);
                mJwt = new JWT(authState.getAccessToken());
                getDomofonList(mJwt.getId());
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.error_token) + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshToken() {
        setProgress(true);
        AuthToken.refreshToken(this, mStateLogin.readAuthState(), new CallbackToken() {
            @Override
            public void onSuccess(AuthState authState) {
                mStateLogin.writeAuthState(authState);
                mJwt = new JWT(authState.getAccessToken());
                getDomofonList(mJwt.getId());
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.error_token) + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDomofonList(final String token) {
        setProgress(true);
        DomofonRequest.getList(token, new CallbackDomofonList() {
            @Override
            public void onSuccess(List<DomofonList> domofonLists) {
                DomofonAdapter adapter = new DomofonAdapter(domofonLists, getApplicationContext(), MainActivity.this);
                mDomofonRecyclerView.setAdapter(adapter);
                setProgress(false);
            }

            @Override
            public void onRefresh() {
                refreshToken();
            }

            @Override
            public void onError(String error) {
                setProgress(false);
                Toast.makeText(MainActivity.this, getString(R.string.error_data) + error,
                        Toast.LENGTH_SHORT).show();
                Snackbar.make(mDomofonRecyclerView, getString(R.string.error_data), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.update_data), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getDomofonList(token);
                            }
                        }).setActionTextColor(getResources().getColor(R.color.colorPrimary))
                        .show();
            }
        });

    }

    private void setProgress(boolean progress) {
        if (progress && !mProgressDialog.isShowing()) {
            mProgressDialog.setMessage(getString(R.string.progress_load));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
        if (!progress) mProgressDialog.dismiss();
    }
}
