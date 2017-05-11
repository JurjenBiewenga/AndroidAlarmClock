package lunosis.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lunosis.alarm.modifiers.AlarmModifier;
import lunosis.alarm.receivers.AlarmReceiver;

public class AlarmDataManager
{
    private List<AlarmData> alarms;
    private Context applicationContext;

    public AlarmDataManager(Context context)
    {
        applicationContext = context;
        alarms = LoadAlarms();
    }

    public boolean SaveAlarms()
    {
        FileOutputStream fos = null;
        ObjectOutputStream os;
        try {
            fos = applicationContext.openFileOutput("alarms.ser", Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(alarms);
            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<AlarmData> LoadAlarms()
    {
        List<AlarmData> alarms = new ArrayList<>();
        FileInputStream fis;
        ObjectInputStream is;
        try {
            fis = applicationContext.openFileInput("alarms.ser");
            is = new ObjectInputStream(fis);
            alarms = (List<AlarmData>) is.readObject();
            is.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return alarms;
    }

    public void SetAlarm(AlarmData data)
    {
        SetAlarm(data, 0);
    }

    public void SetAlarm(AlarmData data, int pendingIntentFlag)
    {
        SetAlarm(data, 0, pendingIntentFlag);
    }

    public void SetAlarm(AlarmData data, long alarmOffset, int pendingIntentFlag)
    {
        if(data.enabled)
        {
            Intent i = new Intent(applicationContext, AlarmReceiver.class);
            i.putExtra("Index", data.id);
            PendingIntent intent = PendingIntent.getBroadcast(applicationContext, data.id, i, pendingIntentFlag);
            AlarmManager a = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);

            long ms = GetTimeTillNextAlarm(data) + alarmOffset;
            data.SetNextAlarm(ms);
            Log.d("Alarm", "Scheduling alarm at: " + String.valueOf(new Date(ms)));
            a.set(AlarmManager.RTC_WAKEUP, ms, intent);
        }
    }

    public void CancelAlarm(AlarmData data)
    {
        Intent i = new Intent(applicationContext, AlarmReceiver.class);
        i.putExtra("Index", data.id);
        PendingIntent intent = PendingIntent.getBroadcast(applicationContext, 0, i, 0);
        AlarmManager a = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        a.cancel(intent);
    }

    public void Add(AlarmData data)
    {
        alarms.add(data);
    }

    public AlarmData Get(int index)
    {
        return alarms.get(index);
    }

    public void Set(int index, AlarmData data)
    {
        alarms.set(index, data);
    }

    public int Size()
    {
        return  alarms.size();
    }

    public static long GetTimeTillNextAlarm(AlarmData data)
    {
        Calendar alarmDay = Calendar.getInstance();
        boolean skipped;
        do
        {
            skipped = false;
            if(data.alarmModifiers.size() > 0)
            {
                AlarmModifier mod = data.alarmModifiers.get(0);
                data = mod.Execute(data);
                data.alarmModifiers.remove(0);
            }

            alarmDay.set(Calendar.HOUR_OF_DAY, data.hour);
            alarmDay.set(Calendar.MINUTE, data.minute);
            alarmDay.set(Calendar.SECOND, 0);

            if(!data.IsValidAlarm(alarmDay))
                alarmDay = data.GetNextValidDay(alarmDay);

            if(data.skipNext)
            {
                skipped = true;
                data.skipNext = false;
                alarmDay = data.GetNextValidDay(alarmDay);
            }
        } while (!data.IsValidAlarm(alarmDay) && !skipped);

        return alarmDay.getTimeInMillis();
    }
}
