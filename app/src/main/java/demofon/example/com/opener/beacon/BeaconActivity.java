package demofon.example.com.opener.beacon;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.BeaconManager;

import demofon.example.com.opener.R;

public class BeaconActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mBeaconResult;
    private TextView mUUID, mMajor, mMinor, mMetr;
    private Button mBeaconScanner;
    private BeaconReferenceApplication application;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

        mBeaconResult = (EditText) findViewById(R.id.beaconResult);
        mUUID = (TextView) findViewById(R.id.beaconUUID);
        mMajor = (TextView) findViewById(R.id.beaconMajor);
        mMinor = (TextView) findViewById(R.id.beaconMinor);
        mMetr = (TextView) findViewById(R.id.beaconMetr);
        mBeaconScanner = (Button) findViewById(R.id.beaconBtnScanner);
        checkBluetooth();
        mBeaconScanner.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.beaconBtnScanner:
                checkBluetooth();
                application = ((BeaconReferenceApplication) this.getApplicationContext());
                if (BeaconManager.getInstanceForApplication(this).getMonitoredRegions().size() > 0) {
                    application.disableMonitoring();
                    mBeaconScanner.setText("scanner off");
                } else {
                    application.enableMonitoring();
                    mBeaconScanner.setText("scanner on");
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_COARSE_LOCATION);
        } else {
            mBeaconScanner.setVisibility(View.VISIBLE);
            BeaconReferenceApplication application = ((BeaconReferenceApplication) this.getApplicationContext());
            application.setMonitoringActivity(this);
            updateResult(application.getLog());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((BeaconReferenceApplication) this.getApplicationContext()).setMonitoringActivity(null);
        application.disableMonitoring();
    }

    public void updateResult(final String result) {
        runOnUiThread(new Runnable() {
            public void run() {
                mBeaconResult.setText(result);
            }
        });
    }

    public void updateResult(final String UUID, final String major, final String minor, final String metr) {
        runOnUiThread(new Runnable() {
            public void run() {
                mUUID.setText(UUID);
                mMajor.setText(major);
                mMinor.setText(minor);
                mMetr.setText(metr);
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
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mBeaconScanner.setVisibility(View.VISIBLE);
                } else {
                    mBeaconScanner.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Необходимо разрешение", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
}
