package demofon.example.com.opener.beacon;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.Collection;

import demofon.example.com.opener.MainActivity;
import demofon.example.com.opener.R;
import demofon.example.com.opener.constants.Constants;

public class BeaconReferenceApplication extends Application implements BootstrapNotifier, RangeNotifier {
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private BeaconManager beaconManager;
    private boolean isNotify, isOpen;
    private static final String TAG = "BeaconReferenceApp";

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        isNotify = false;
        isOpen = false;

//        первичная настройка beaconManager,
//        по умолчанию производится поиск AltBeacon,
//        для поиска iBeacon необходимо установить соответствеющий макет

        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.setDebug(true);

//        настройка оповещения о включенном мониторинге маяков
//        и включение foreground service для продолжительного сканирования

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_lock);
        builder.setContentTitle("Поиск beacons");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("opener",
                    "Opener", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Open th door");
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);

//        отключение JobScheduler (важно в Android 8+) для продолжительного фонового сканирования

        beaconManager.setEnableScheduledScanJobs(false);

//        BeaconManager.setRegionExitPeriod(20000L);
//        backgroundPowerSaver = new BackgroundPowerSaver(this);

        beaconManager.setBackgroundMode(true);
        beaconManager.setBackgroundBetweenScanPeriod(4000L);
        beaconManager.setBackgroundScanPeriod(1300L);

//        Настройка региона сканирования, при обнаружении маяка с заданным ID, приложение пробудится

        Region region = new Region("backgroundRegion",
                Identifier.parse("00f0dbe5-688a-4ef0-bedc-deac3e00a9db"), null, null);
        regionBootstrap = new RegionBootstrap(this, region);
    }

    public void disableMonitoring() {
        if (regionBootstrap != null) {
            regionBootstrap.disable();
            regionBootstrap = null;
        }
    }

    @Override
    public void didEnterRegion(Region region) {

//        маяк с заданным ID обнаружен, запускается ранжирование

        Log.d(TAG, "didEnterRegion " + region.getUniqueId());
        beaconManager.addRangeNotifier(this);
        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.d(TAG, "didEnterRegionNo");
        }
    }

    @Override
    public void didExitRegion(Region region) {

//        маяк с заданным ID потерян, ранжирование останавливается

        Log.d(TAG, "didExitRegion" + region.getUniqueId());
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
            cancelNotification();
            isOpen = false;
        } catch (RemoteException e) {
            Log.d(TAG, "didExitRegionNo");
        }
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        Log.d(TAG, "didDetermineStateForRegion");
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

//        ранжирование найденных маяков
//        если параметр Major обнаруженного маяка соотвествует какому-либо domofon_id из списка
//        пользователь получает оповещение, с возможностью открыть подъезд,
//        либо запустится OpenDomofonService, если пользователь подойдет ближе заданного расстояния

        Log.d(TAG, "didRangeBeaconsInRegion");
        if (beacons.size() > 0) {
            SharedPreferences sharedPreferences = getSharedPreferences(
                    Constants.DOMOFON_PREFERENCES_NAME,
                    Context.MODE_PRIVATE
            );
            int size = sharedPreferences.getInt("size", 0);
            for (Beacon beacon : beacons) {
                for (int i = 0; i < size; i++) {
                    String domofonId = sharedPreferences.getString(i + "domofon", null);
                    String address = sharedPreferences.getString(i + "address", null);
                    if (beacon.getId2().toString().equals(domofonId)) {
                        sendNotification(address, domofonId);
                        if (beacon.getDistance() < Constants.DISTANCE_TO_OPEN && !isOpen) {
                            Intent intent = new Intent(this, OpenDomofonService.class);
                            intent.putExtra("domofon", domofonId);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startService(intent);
                            isOpen = true;
                        }
                    }
                }
            }
        }
        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(String address, String domofonId) {
        if (!isNotify) {
            BeaconNotification.notify(this, address, domofonId);
            isNotify = true;
        }
    }

    private void cancelNotification() {
        BeaconNotification.cancel(this);
        isNotify = false;
    }
}