package lunosis.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
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
    public static AlarmData defaultAlarmData;
    private List<AlarmData> alarms;
    private Context applicationContext;

    public AlarmDataManager(Context context)
    {
        applicationContext = context;
        alarms = LoadAlarms();
        defaultAlarmData = LoadDefaultAlarmData();
        if(defaultAlarmData == null)
        {
            defaultAlarmData = CreateDefaultAlarmData();
            SaveDefaultAlarmData();
        }
    }

    public boolean SaveAlarms()
    {
        return WriteToFile("alarms.ser", alarms);
    }

    public List<AlarmData> LoadAlarms()
    {
        List<AlarmData> data = new ArrayList<>();
        List<AlarmData> newData = (List<AlarmData>)ReadFromFile("alarms.ser");
        if(newData != null)
            data = newData;

        return data;
    }

    public boolean SaveDefaultAlarmData()
    {
       return WriteToFile("defaultAlarm.ser", defaultAlarmData);
    }

    public AlarmData LoadDefaultAlarmData()
    {
        return (AlarmData) ReadFromFile("defaultAlarm.ser");
    }

    public AlarmData CreateDefaultAlarmData()
    {
        AlarmData data = new AlarmData(0,0);
        data.setStartVolume(0);
        data.setEndVolume(80);
        data.setVolumeLerpLength(40);
        data.setStartIntensity(10);
        data.setEndIntensity(100);
        data.setVibrateLerpLength(40);
        data.setRingtonePath(RingtoneManager.getActualDefaultRingtoneUri(applicationContext, RingtoneManager.TYPE_ALL));

        return data;
    }

    private boolean WriteToFile(String name, Object serializable)
    {
        FileOutputStream fos = null;
        ObjectOutputStream os;
        try {
            fos = applicationContext.openFileOutput(name, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(serializable);
            os.close();
            fos.close();
        } catch (Exception e) {
            Log.e("Alarm", Log.getStackTraceString(e));
            return false;
        }
        return true;
    }

    private Object ReadFromFile(String name)
    {
        Object object = null;
        FileInputStream fis;
        ObjectInputStream is;
        try {
            fis = applicationContext.openFileInput(name);
            is = new ObjectInputStream(fis);
            object = is.readObject();
            is.close();
            fis.close();
        } catch (Exception e) {
            Log.e("Alarm", Log.getStackTraceString(e));
            //e.printStackTrace();
        }
        return object;
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
        if(data.isEnabled())
        {
            Intent i = new Intent(applicationContext, AlarmReceiver.class);
            Bundle b = new Bundle();
            b.putSerializable("AlarmData", data);
            b.putInt("index", GetIndexByAlarmId(data.id));
            i.putExtra("bundle",b);
            PendingIntent intent = PendingIntent.getBroadcast(applicationContext, data.id, i, pendingIntentFlag | PendingIntent.FLAG_UPDATE_CURRENT);
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
        PendingIntent intent = PendingIntent.getBroadcast(applicationContext, data.id, i, 0);
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

    public int GetIndexByAlarmId(int id)
    {
        for(int i = 0; i < alarms.size(); i++)
        {
            AlarmData data = alarms.get(i);
            if (data.id == id)
                return i;
        }
        return -1;
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

            alarmDay.set(Calendar.HOUR_OF_DAY, data.getHour());
            alarmDay.set(Calendar.MINUTE, data.getMinute());
            alarmDay.set(Calendar.SECOND, 0);

            if(!data.IsValidAlarm(alarmDay))
                alarmDay = data.GetNextValidDay(alarmDay);

            if(data.ShouldSkipNext())
            {
                skipped = true;
                data.setSkipNext(false);
                alarmDay = data.GetNextValidDay(alarmDay);
            }
        } while (!data.IsValidAlarm(alarmDay) && !skipped);

        return alarmDay.getTimeInMillis();
    }
}
