package demofon.example.com.opener;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;

import net.openid.appauth.AuthState;

import org.altbeacon.beacon.BeaconManager;

import java.util.List;
import java.util.Objects;

import demofon.example.com.opener.beacon.BeaconReferenceApplication;
import demofon.example.com.opener.constants.Constants;
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
    private Button mLogout;
    private Switch mBeaconSwitch;
    private ProgressDialog mProgressDialog;
    private BeaconReferenceApplication application;
    private CoordinatorLayout mCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLogout = (Button) findViewById(R.id.domofonBtnLogout);
        mBeaconSwitch = (Switch) findViewById(R.id.domofonBtnBeacon);
        mDomofonRecyclerView = (RecyclerView) findViewById(R.id.domofonRecyclerV);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.domofonSnack);
        mLayoutManager = new LinearLayoutManager(this);
        mDomofonRecyclerView.setLayoutManager(mLayoutManager);
        mStateLogin = new AuthStateLogin(this);
        mProgressDialog = new ProgressDialog(this);
        mLogout.setOnClickListener(this);

//      проверка был ли получен токен

        if (mStateLogin.readAuthState().isAuthorized()) {
            mJwt = new JWT(Objects.requireNonNull(mStateLogin.readAuthState().getAccessToken()));
            getDomofonList(mJwt.getId());
        } else getToken();

//      настройка Switch для обнаружения маяков

        final SharedPreferences sharedPreferences = getSharedPreferences(
                Constants.DOMOFON_PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );
        boolean enable = sharedPreferences.getBoolean("enableMonitoring", false);
        application = ((BeaconReferenceApplication) this.getApplicationContext());
        if (!enable) {
            mBeaconSwitch.setChecked(false);
            application.disableMonitoring();
        } else {
            mBeaconSwitch.setChecked(true);
        }

        mBeaconSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && BeaconManager.getInstanceForApplication(MainActivity.this).getMonitoredRegions().size() == 0) {
                    checkBluetooth();
                    application.onCreate();
                    sharedPreferences
                            .edit()
                            .putBoolean("enableMonitoring", true)
                            .apply();

                } else {
                    application.disableMonitoring();
                    sharedPreferences
                            .edit()
                            .putBoolean("enableMonitoring", false)
                            .apply();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

//        для обнаружения маяков необходимо запросить опасные разрешения на доступ к геопозиции

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.PERMISSION_REQUEST_COARSE_LOCATION);
        }
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
        }
    }

    private void getToken() {
        setProgress(true);

//        Запрос на получение токена с использованием кода авторизации

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
                setProgress(false);
            }
        });
    }

    private void refreshToken() {
        setProgress(true);

//        Повторный запрос на получение токена с использованием refresh токена

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
                setProgress(false);
            }
        });
    }

    private void getDomofonList(final String token) {
        setProgress(true);

//        Обращение к API для получения списка доступных пользователю подъездов

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
                Snackbar.make(mCoordinatorLayout, getString(R.string.error_data), Snackbar.LENGTH_INDEFINITE)
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

    private void checkBluetooth() {
        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth выключен");
                builder.setMessage("Необходимо включить Bluetooth");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
                        if (!bluetooth.isEnabled()) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
                        }
                    }
                });
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                });
                builder.show();
            }
        } catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE не доступен");
            builder.setMessage("Данное устройство не поддерживает Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                }

            });
            builder.show();
        }
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
