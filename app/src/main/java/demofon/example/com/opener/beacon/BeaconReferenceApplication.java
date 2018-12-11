package demofon.example.com.opener.beacon;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.Collection;

import demofon.example.com.opener.R;

public class BeaconReferenceApplication extends Application implements BootstrapNotifier, BeaconConsumer {
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean haveDetectedBeaconsSinceBoot = false;
    private BeaconActivity monitoringActivity = null;
    private String cumulativeLog = "";
    private BeaconManager beaconManager;

    public void onCreate() {
        super.onCreate();
        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.setDebug(true);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_lock);
        builder.setContentTitle("Поиск beacons");
        Intent intent = new Intent(this, BeaconActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification Channel ID",
                    "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Notification Channel Description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);
        beaconManager.setEnableScheduledScanJobs(false);

//        beaconManager.setBackgroundBetweenScanPeriod(0);
//        beaconManager.setBackgroundScanPeriod(1100);
//
//        Region region = new Region("backgroundRegion",
//                null, null, null);
//        regionBootstrap = new RegionBootstrap(this, region);
//        backgroundPowerSaver = new BackgroundPowerSaver(this);
    }

    public void disableMonitoring() {
        if (regionBootstrap != null) {
            regionBootstrap.disable();
            regionBootstrap = null;
            beaconManager.unbind(this);
        }
    }

    public void enableMonitoring() {
        Region region = new Region("backgroundRegion",
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
    }

    @Override
    public void didEnterRegion(Region arg0) {
        if (!haveDetectedBeaconsSinceBoot) {
            haveDetectedBeaconsSinceBoot = true;
        } else {
            if (monitoringActivity != null) {
                toDisplay("Beacon снова виден");
            } else {
                sendNotification();
            }
        }
    }

    @Override
    public void didExitRegion(Region region) {
        toDisplay("Beacon больше не виден");
        beaconManager.unbind(this);
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        String mStateBeacon;
        if (state == 1) {
            beaconManager.bind(this);
            mStateBeacon = "Beacon IN";
        } else mStateBeacon = "Beacon OUT";

        toDisplay("Текущее состояние: " + mStateBeacon);
    }

    private void sendNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Обнаружен Beacon")
                        .setContentText("Beacon рядом")
                        .setSmallIcon(R.drawable.ic_lock_open);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, BeaconActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    public void setMonitoringActivity(BeaconActivity activity) {
        this.monitoringActivity = activity;
    }

    private void toDisplay(String line) {
        cumulativeLog += (line + "\n");
        if (this.monitoringActivity != null) {
            this.monitoringActivity.updateResult(cumulativeLog);
        }
    }

    private void toDisplay(String UUID, String major, String minor, String metr) {
        if (this.monitoringActivity != null) {
            this.monitoringActivity.updateResult(UUID, major, minor, metr);
        }
    }

    public String getLog() {
        return cumulativeLog;
    }

    @Override
    public void onBeaconServiceConnect() {
        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Beacon firstBeacon = beacons.iterator().next();
                    toDisplay(
                            "UUID: " + firstBeacon.getId1().toString(),
                            "Major: " + firstBeacon.getId2().toString(),
                            "Minor: " + firstBeacon.getId3().toString(),
                            "Distance: " + String.valueOf(firstBeacon.getDistance())
                    );
                }
            }

        };
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
        } catch (RemoteException e) {
        }
    }
}
