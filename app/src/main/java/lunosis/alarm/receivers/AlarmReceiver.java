package lunosis.alarm.receivers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.io.Console;

import lunosis.alarm.AlarmActivity;
import lunosis.alarm.AlarmData;

public class AlarmReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "");
//        wl.acquire();
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        //v.vibrate(50);
        //v.vibrate(new long[]{0, 25,25,25,25,25,25,25,25,25,25}, -1);
        Intent i = new Intent(context, AlarmActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("bundle", intent.getBundleExtra("bundle"));
        context.startActivity(i);
//        wl.release();
        completeWakefulIntent(intent);
    }
}
