package demofon.example.com.opener.beacon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import org.altbeacon.beacon.BeaconManager;

import demofon.example.com.opener.R;
import demofon.example.com.opener.constants.Constants;

public class BeaconSettingActivity extends AppCompatActivity {

    private EditText mBeaconScan, mBeaconBtw, mBeaconDistance;
    private Switch mBeaconOpen;
    private String mScanValue, mBtwValue, mDistanceValue;
    private boolean mSwitchSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_setting);
        getPreferenceValue();

        mBeaconScan = (EditText) findViewById(R.id.beaconSettingEditScan);
        mBeaconBtw = (EditText) findViewById(R.id.beaconSettingEditBtw);
        mBeaconDistance = (EditText) findViewById(R.id.beaconSettingEditDistance);
        mBeaconOpen = (Switch) findViewById(R.id.beaconSettingSwitch);

        mBeaconScan.setText(mScanValue);
        mBeaconBtw.setText(mBtwValue);
        mBeaconDistance.setText(mDistanceValue);
        mBeaconOpen.setChecked(mSwitchSet);
    }

    @Override
    protected void onPause() {
        super.onPause();
        writeToPreference(
                mBeaconScan.getText().toString(),
                mBeaconBtw.getText().toString(),
                mBeaconDistance.getText().toString(),
                mBeaconOpen.isChecked()
        );
        Toast.makeText(this, getString(R.string.beacon_setting_toast_set), Toast.LENGTH_SHORT).show();
        if (BeaconManager.getInstanceForApplication(this).getMonitoredRegions().size() != 0) {
            BeaconReferenceApplication application = ((BeaconReferenceApplication) this.getApplicationContext());
            application.setNewSetting();
        }

    }

    public void getPreferenceValue() {
        SharedPreferences sp = getSharedPreferences(
                Constants.SETTING_PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );
        mScanValue = sp.getString(Constants.SETTING_SCAN_VALUE, Constants.SETTING_SCAN_VALUE_DEF);
        mBtwValue = sp.getString(Constants.SETTING_SCAN_BTW_VALUE, Constants.SETTING_SCAN_BTW_VALUE_DEF);
        mDistanceValue = sp.getString(Constants.SETTING_SCAN_DISTANCE_VALUE, Constants.SETTING_SCAN_DISTANCE_VALUE_DEF);
        mSwitchSet = sp.getBoolean(Constants.SETTING_SWITCH_VALUE, Constants.SETTING_SWITCH_VALUE_DEF);
    }

    public void writeToPreference(String beaconScan, String beaconBtw, String beaconDistance, boolean switchSet) {
        if (beaconScan.equals("")) beaconScan = Constants.SETTING_SCAN_VALUE_DEF;
        if (beaconBtw.equals("")) beaconBtw = Constants.SETTING_SCAN_BTW_VALUE_DEF;
        if (beaconDistance.equals("")) beaconDistance = Constants.SETTING_SCAN_DISTANCE_VALUE_DEF;

        SharedPreferences.Editor editor = getSharedPreferences(
                Constants.SETTING_PREFERENCES_NAME,
                Context.MODE_PRIVATE
        ).edit();
        editor.putString(Constants.SETTING_SCAN_VALUE, beaconScan);
        editor.putString(Constants.SETTING_SCAN_BTW_VALUE, beaconBtw);
        editor.putString(Constants.SETTING_SCAN_DISTANCE_VALUE, beaconDistance);
        editor.putBoolean(Constants.SETTING_SWITCH_VALUE, switchSet);
        editor.apply();
    }

    public void goToSystemSetting(View view) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}