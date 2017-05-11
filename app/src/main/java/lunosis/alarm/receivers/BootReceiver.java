package lunosis.alarm.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import lunosis.alarm.AlarmApplication;
import lunosis.alarm.AlarmData;
import lunosis.alarm.AlarmDataManager;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmApplication app = (AlarmApplication) context.getApplicationContext();
        AlarmDataManager adm = app.alarmDataManager;

        for (int i = 0; i < adm.Size(); i++)
        {
            AlarmData alarmData = adm.Get(i);
            adm.SetAlarm(alarmData);
        }
    }
}
