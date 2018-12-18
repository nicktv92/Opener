package demofon.example.com.opener.beacon;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import demofon.example.com.opener.MainActivity;
import demofon.example.com.opener.R;

public class BeaconNotification {

    private static final String NOTIFICATION_TAG = "Beacon";

    public static void notify(final Context context,
                              final String address, String domofon) {
        final Resources res = context.getResources();

        Intent intent = new Intent(context, OpenDomofonService.class);
        intent.putExtra("domofon", domofon);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openIntent = PendingIntent.getService(context, 0, intent, 0);
        final String title = res.getString(
                R.string.beacon_notification_title);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_lock_open)
                .setContentTitle(title)
                .setContentText(address)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(context, MainActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(
                        R.drawable.ic_lock_open,
                        res.getString(R.string.action_open),
                       openIntent)
                .setAutoCancel(true);
        notify(context, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_TAG, 0, notification);
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_TAG, 0);
    }
}
