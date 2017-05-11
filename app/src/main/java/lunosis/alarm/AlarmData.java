package lunosis.alarm;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import lunosis.alarm.modifiers.AlarmModifier;
public class AlarmData implements Serializable {

    public int id;
    public boolean enabled;
    public int hour;
    public int minute;
    public Uri ringtonePath;
    public int startVolume = 0;
    public int endVolume = 0;
    public int volumeLerpLength = 20;
    public int startIntensity = 0;
    public int endIntensity = 100;
    public int vibrateLerpLength = 20;
    public long nextAlarm;

    public boolean skipNext;

    public Boolean[] days = {false,true,true,true,true,true,false};
    public static String[] weekdays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    public List<AlarmModifier> alarmModifiers = new ArrayList<>();

    public AlarmData(int h, int m)
    {
        hour =h;
        minute = m;
        enabled = true;
        id = new Random().nextInt(99999999);
    }

    public void SetNextAlarm(long ms)
    {
        nextAlarm = ms;
    }

    public Calendar GetNextValidDay(Calendar cal)
    {
        do
        {
            cal.add(Calendar.DAY_OF_WEEK, 1);
        } while (!IsValidDay(cal));
        return cal;
    }

    public boolean IsValidDay( Calendar cal)
    {
        int day = cal.get(Calendar.DAY_OF_WEEK) - 1;
        return days[day];
    }

    public boolean IsValidAlarm(Calendar cal)
    {
        Calendar today = Calendar.getInstance();
        return today.before(cal) && IsValidDay(cal);
    }

    public boolean IsExpectedToGoOffToday()
    {
        Date d = new Date(nextAlarm);
        return Calendar.getInstance().before(d);
    }
}
