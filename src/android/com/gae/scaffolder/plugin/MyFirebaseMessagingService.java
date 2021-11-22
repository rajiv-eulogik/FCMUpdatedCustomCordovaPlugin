package com.gae.scaffolder.plugin;

import android.media.MediaPlayer;
import android.content.ContentResolver ;
import androidx.core.app.NotificationCompat;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import java.util.Map;
import java.util.HashMap;
import android.media.AudioAttributes ;
import android.graphics.Color ;
import android.os.Vibrator;
import android.app.PendingIntent;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import 	android.media.Ringtone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.media.AudioManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;



public class MyFirebaseMessagingService extends FirebaseMessagingService {
    
    private static final String TAG = "FCMPlugin";
    protected Context context;
    
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New token: " + token);
        FCMPlugin.sendTokenRefresh(token);
    }
    
    /**
    * Called when message is received.
    *
    * @param remoteMessage Object representing e message received from Firebase Cloud Messaging.
    */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.See sendNotification method below.
        Log.d(TAG, "==> MyFirebaseMessagingService onMessageReceived");
        
        if(remoteMessage.getNotification() != null){
            Log.d(TAG, "\tNotification Title: " + remoteMessage.getNotification().getTitle());
            Log.d(TAG, "\tNotification Message: " + remoteMessage.getNotification().getBody());
        }
        
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("wasTapped", false);
        
        if(remoteMessage.getNotification() != null){
            data.put("title", remoteMessage.getNotification().getTitle());
            data.put("body", remoteMessage.getNotification().getBody());
        }
        JSONObject args;;
        for (String key : remoteMessage.getData().keySet()) {
            Object value = remoteMessage.getData().get(key);
            if(key.equals("channel")) {
                Log.d(TAG, "\tNotification Data: FCM === " + key);
                try {
                    args = new JSONObject(remoteMessage.getData().get(key));
                    // new FCMPluginChannelCreator(this).createNotificationChannelAlt(args);
                    // getAndroidChannelNotification(args.getString("title"), args.getString("body"));
                    createNotification(args.getString("title"), args.getString("body"));
                }
                catch (JSONException e) {
                    //some exception handler code.
                    Log.d(TAG, "\tNotification Title: ");
                }  
            }
            Log.d(TAG, "\tKey: " + key + " Value: " + value);
            data.put(key, value);
        }
        
        Log.d(TAG, "\tNotification Data: " + data.toString());
        FCMPlugin.sendPushPayload(data);
        
    }
    
    public void getAndroidChannelNotification(String title, String body) {
        Notification.Builder nb = new Notification.Builder(getApplicationContext(), "urgent_calls")
        .setContentTitle(title)
        .setContentText(body)
        .setSmallIcon(getApplicationInfo().icon)   
        .setChannelId("urgent_calls")
        .setAutoCancel(false);
        NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mManager.notify(100, nb.build());
    }
    
    
    public void createNotification(String title, String body) {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if(title.equals("Missed call!")) { 
            Log.d(TAG, "Missed call title ---- " + title);
            PackageManager pm = getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "missed_calls")
            .setSmallIcon(getApplicationInfo().icon)
            .setContentTitle(title)
            .setContentText(body)
            .setCategory(NotificationCompat.CATEGORY_MISSED_CALL)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(pIntent);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE ) ;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ) {
                int importance = NotificationManager.IMPORTANCE_HIGH ;
                NotificationChannel notificationChannel = new NotificationChannel( "missed_calls" , "NOTIFICATION_CHANNEL_NAME_MISSED" , importance) ;
                notificationChannel.enableLights( true ) ;
                notificationChannel.setLightColor(Color.RED ) ;
                notificationChannel.enableVibration( true ) ;
                notificationChannel.setVibrationPattern( new long []{ 100 , 200 , 300 , 400 , 500 , 400 , 300 , 200 , 400 }) ;
                mBuilder.setChannelId( "missed_calls" ) ;
                assert mNotificationManager != null;
                mNotificationManager.createNotificationChannel(notificationChannel) ;
            }
            assert mNotificationManager != null;
            mNotificationManager.cancel(200);
            mNotificationManager.notify(500, mBuilder.build()) ;
        }
        else {
            Log.d(TAG, "Else Missed call title ---- " + title);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION )
                .setUsage(AudioAttributes.USAGE_NOTIFICATION )
                .build() ;
            PackageManager pm = getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Uri sound = Uri.parse (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/ring" ) ;
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "urgent_calls")
            .setSmallIcon(getApplicationInfo().icon)
            .setContentTitle(title)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVibrate(new long []{ 1000 , 2000 , 1000 , 4000 , 1000 , 2000 , 1000 , 4000 , 1000 })
            .setWhen(System.currentTimeMillis())
            .setContentText(body)
            .setOngoing(false)
            .setContentIntent(pIntent)
            .setSound(sound)
            .setAutoCancel(true)
            .setFullScreenIntent(pIntent, true);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE ) ;
            Log.d(TAG, "hello wordl this a new notification");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ) {
                Log.d(TAG, "My version is  ---- channel required");
                int importance = NotificationManager.IMPORTANCE_HIGH ;
                NotificationChannel notificationChannel = new NotificationChannel( "urgent_calls" , "NOTIFICATION_CHANNEL_NAME" , importance) ;
                notificationChannel.setSound(sound, audioAttributes) ;
                notificationChannel.enableLights( true ) ;
                notificationChannel.setLightColor(Color.RED ) ;
                notificationChannel.enableVibration(true) ;
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationChannel.shouldVibrate();
                notificationChannel.setVibrationPattern( new long []{ 1000 , 2000 , 1000 , 4000 , 1000 , 2000 , 1000 , 4000 , 1000 }) ;
                mBuilder.setChannelId("urgent_calls") ;
                mNotificationManager.createNotificationChannel(notificationChannel) ;
            }
            assert mNotificationManager != null;
            mNotificationManager.cancel(500) ;
            audioManager.setMode(AudioManager.MODE_RINGTONE);
            // audioManager.setSpeakerphoneOn(true);
            mNotificationManager.notify(200, mBuilder.build()) ;
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = android.os.Build.VERSION.SDK_INT >= 20 ? powerManager.isInteractive() : powerManager.isScreenOn(); // check if screen is on
            if (!isScreenOn) {
                PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:notificationLock");
                wl.acquire(20000); //set your time in milliseconds
                wl.release(); //set your time in milliseconds
            }
            if(android.os.Build.VERSION.SDK_INT == 10) {
                Log.d(TAG, "LOGR -- Version 10");
            }
            // wakeScreen();
        }
        // Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // long[] pattern = {100, 1000, 100, 1000};
        // if (mVibrator != null) {
        //     AudioAttributes audioAttributes = new AudioAttributes.Builder()
        //             .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        //             .setUsage(AudioAttributes.USAGE_ALARM) //key
        //             .build();
        //     mVibrator.vibrate(pattern, 2, audioAttributes);
        // }
    }
    // [END receive_message]


    // public void wakeScreen() {
    //     PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
    //     boolean isScreenOn = pm.isInteractive();
    //     if(isScreenOn == false) {
    //         PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
    //         wl.acquire(10000);
    //         PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");
    //         wl_cpu.acquire(10000);
    //     }
    // }
}
