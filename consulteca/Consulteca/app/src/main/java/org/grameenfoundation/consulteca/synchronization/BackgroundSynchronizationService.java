package org.grameenfoundation.consulteca.synchronization;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;
import org.grameenfoundation.consulteca.R;

/**
 * Background Service that initiates the synchronization process in the background.
 */
public class BackgroundSynchronizationService extends Service implements SynchronizationListener {
    private NotificationManager notificationManager;
    private static final String NOTIFICATION_TAG = "70eHGpKGfU2QO8Q50Dp1";
    private static final int NOTIFICATION_ID = 1;


    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        SynchronizationManager.getInstance().registerListener(this);
        SynchronizationManager.getInstance().start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        notificationManager.cancelAll();
        super.onDestroy();
        SynchronizationManager.getInstance().unRegisterListener(this);
        this.stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void synchronizationStart() {
        Log.i(BackgroundSynchronizationService.class.getName(), "Background Synchronization Started.");
    }

    @Override
    public void synchronizationUpdate(Integer step, Integer max, String message, Boolean reset) {
        Log.i(BackgroundSynchronizationService.class.getName(), message + step + " out of " + max);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(message)
                .setContentText(message)
                .setSmallIcon(R.drawable.mobile_app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.mobile_app_icon))
                .setProgress(max, step, false).getNotification();

        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notification);
    }

    @Override
    public void synchronizationUpdate(String message, Boolean indeterminate) {
        Log.i(BackgroundSynchronizationService.class.getName(), message);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(message)
                .setContentText(message)
                .setSmallIcon(R.drawable.mobile_app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.mobile_app_icon))
                .setProgress(0, 0, indeterminate).getNotification();

        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notification);
    }

    @Override
    public void synchronizationComplete() {
        Log.i(BackgroundSynchronizationService.class.getName(), "Background Synchronization Completed.");
        Notification notification = new Notification.Builder(this)
                .setContentText(this.getResources().getString(R.string.synchronization_complete_msg))
                .setContentTitle(this.getResources().getString(R.string.synchronization_progress_bar_title))
                .setSmallIcon(R.drawable.mobile_app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.mobile_app_icon))
                .getNotification();

        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notification);
    }

    @Override
    public void onSynchronizationError(Throwable throwable) {
        Log.e(BackgroundSynchronizationService.class.getName(), throwable.getMessage());
        Notification notification = new Notification.Builder(this)
                .setContentText(this.getResources().getString(R.string.processing_keywords_msg))
                .setContentTitle(this.getResources().getString(R.string.synchronization_progress_bar_title))
                .setSmallIcon(R.drawable.mobile_app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), android.R.drawable.stat_notify_error))
                .getNotification();
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notification);
    }
}
