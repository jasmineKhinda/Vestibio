package com.andreyaleev.metrosong.fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.andreyaleev.metrosong.Constants;
import com.andreyaleev.metrosong.R;
import com.andreyaleev.metrosong.activities.MainActivity;
import com.andreyaleev.metrosong.tools.ResourcesHelper;
import com.andreyaleev.metrosong.tools.Utils;

/**
 * Created by Andrey on 25.10.2016.
 */

public abstract class MetronomableFragment extends BaseFragment {

    protected short initialVolume;
    protected AudioManager audio;
    protected BroadcastReceiver notificationReceiver;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        audio = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        initialVolume = (short) audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void registerNotificationReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_STOP);

        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.ACTION_STOP)) {
                    onStopMetronome();
                }
            }
        };
        getContext().registerReceiver(notificationReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.checkAndStopService(getContext());
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, initialVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        if (notificationReceiver != null) {
            getContext().unregisterReceiver(notificationReceiver);
        }
    }

    protected abstract void onStopMetronome();


    protected void issueServiceNotification() {
        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(getContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.service_started))
                .setOngoing(true);
//                .setDefaults(Notification.DEFAULT_ALL);

        if (Utils.isHigherVersion(Build.VERSION_CODES.JELLY_BEAN)) {
            builder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.service_started)));

            Intent stopIntent = new Intent(Constants.ACTION_STOP);
            PendingIntent resetPendingIntent = PendingIntent.getBroadcast(getContext(), (int) System.currentTimeMillis(), stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(ResourcesHelper.getStopIconResId(), getString(R.string.stop), resetPendingIntent);
        }
        Intent resultIntent = new Intent(getContext().getApplicationContext(), MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        resultIntent.addFlags(Notification.FLAG_ONGOING_EVENT);
        resultIntent.addFlags(Notification.FLAG_NO_CLEAR);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        getContext(),
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager mManager = (NotificationManager) getContext().getApplicationContext().getSystemService(getContext().getApplicationContext().NOTIFICATION_SERVICE);
        mManager.notify(Constants.SERVICE_NOTIFICATION_ID, builder.build());
    }
}
