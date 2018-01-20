package lunosis.alarm;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import lunosis.alarm.modifiers.AlarmModifier;
public class AlarmData implements Serializable
{
    public int id;
    private Boolean enabled;
    private Integer hour;
    private Integer minute;
    private Uri ringtonePath;
    private Integer startVolume = 0;
    private Integer endVolume = 0;
    private Integer volumeLerpLength = 20;
    private Integer startIntensity = 0;
    private Integer endIntensity = 100;
    private Integer vibrateLerpLength = 20;
    private Long nextAlarm;
    private int[] snoozeTimes = new int[]{15,30,45,60};

    private Boolean skipNext = false;

    public Boolean[] days = {false,true,true,true,true,true,false};
    public static String[] weekdays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    public List<AlarmModifier> alarmModifiers = new ArrayList<>();

    public AlarmData(int h, int m)
    {
        setHour(h);
        setMinute(m);
        setEnabled(true);
        id = new Random().nextInt(99999999);
    }

    public void SetNextAlarm(long ms)
    {
        setNextAlarm(ms);
    }

    public Calendar GetNextValidDay(Calendar cal)
    {
        boolean isRepeating = IsRepeating();
        do
        {
            cal.add(Calendar.DAY_OF_WEEK, 1);
        } while (!IsValidDay(cal) && isRepeating);
        return cal;
    }

    public boolean IsValidDay( Calendar cal)
    {
        if(IsRepeating())
        {
            int day = cal.get(Calendar.DAY_OF_WEEK) - 1;
            return days[day];
        }
        else
            return true;
    }

    public boolean IsValidAlarm(Calendar cal)
    {
        Calendar today = Calendar.getInstance();
        return today.before(cal) && IsValidDay(cal);
    }

    public boolean IsExpectedToGoOffToday()
    {
        Date d = new Date(getNextAlarm());
        return Calendar.getInstance().before(d);
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public int getHour()
    {
        return hour;
    }

    public void setHour(int hour)
    {
        this.hour = hour;
    }

    public int getMinute()
    {
        return minute;
    }

    public void setMinute(int minute)
    {
        this.minute = minute;
    }

    public Uri getRingtonePath()
    {
        if(ringtonePath == null)
            return AlarmDataManager.defaultAlarmData.getRingtonePath();
        return ringtonePath;
    }

    public void setRingtonePath(Uri ringtonePath)
    {
        this.ringtonePath = ringtonePath;
    }

    public int getStartVolume()
    {
        if(startVolume == null)
            return AlarmDataManager.defaultAlarmData.getStartVolume();
        return startVolume;
    }

    public void setStartVolume(int startVolume)
    {
        this.startVolume = startVolume;
    }

    public int getEndVolume()
    {
        if(endVolume == null)
            return AlarmDataManager.defaultAlarmData.getEndVolume();
        return endVolume;
    }

    public void setEndVolume(int endVolume)
    {
        this.endVolume = endVolume;
    }

    public int getVolumeLerpLength()
    {
        if(volumeLerpLength == null)
            return AlarmDataManager.defaultAlarmData.getVolumeLerpLength();
        return volumeLerpLength;
    }

    public void setVolumeLerpLength(int volumeLerpLength)
    {
        this.volumeLerpLength = volumeLerpLength;
    }

    public int getStartIntensity()
    {
        if(startIntensity == null)
            return AlarmDataManager.defaultAlarmData.getStartIntensity();
        return startIntensity;
    }

    public void setStartIntensity(int startIntensity)
    {
        this.startIntensity = startIntensity;
    }

    public int getEndIntensity()
    {
        if(endIntensity == null)
            return AlarmDataManager.defaultAlarmData.getEndIntensity();
        return endIntensity;
    }

    public void setEndIntensity(int endIntensity)
    {
        this.endIntensity = endIntensity;
    }

    public int getVibrateLerpLength()
    {
        if(vibrateLerpLength == null)
            return AlarmDataManager.defaultAlarmData.getVibrateLerpLength();
        return vibrateLerpLength;
    }

    public void setVibrateLerpLength(int vibrateLerpLength)
    {
        this.vibrateLerpLength = vibrateLerpLength;
    }

    public boolean IsRepeating()
    {
        if(days != null)
        {
            for(boolean day : days)
            {
                if(day)
                    return true;
            }
        }
        return false;
    }

    public long getNextAlarm()
    {
        return nextAlarm;
    }

    public void setNextAlarm(long nextAlarm)
    {
        this.nextAlarm = nextAlarm;
    }

    public boolean ShouldSkipNext()
    {
        return skipNext;
    }

    public void setSkipNext(boolean skipNext)
    {
        this.skipNext = skipNext;
    }

    public int[] getSnoozeTimes()
    {
        return snoozeTimes;
    }

    public void setSnoozeTimes(int[] snoozeTimes)
    {
        this.snoozeTimes = snoozeTimes;
    }
}
