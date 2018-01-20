package lunosis.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.util.Timer;
import java.util.TimerTask;

import lunosis.alarm.helpers.MathHelper;
import lunosis.alarm.helpers.VibratorUtility;
import lunosis.alarm.receivers.AlarmReceiver;
public class AlarmActivity extends Activity
{
    MediaPlayer mp;
    int userVolume;
    AlarmData data;
    AlarmDataManager adm;
    Handler vibratorHandler;
    Timer volumeTimer;
    AudioManager am;
    int index;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm);

        AlarmApplication app = (AlarmApplication) getApplication();
        adm = app.alarmDataManager;

        try
        {
            Intent i = getIntent();
            Bundle b =i.getBundleExtra("bundle");
            data = (AlarmData) b.getSerializable("AlarmData");
            index = getIntent().getBundleExtra("bundle").getInt("index");
        }
        catch (Exception e)
        {
            Log.e("Alarm", Log.getStackTraceString(e));
            finish();
            return;
        }

        Button b = (Button) findViewById(R.id.StopButton);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.snoozeView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new SnoozeOptionsAdapter(data.getSnoozeTimes(), this));

        SetWindowFlags();

        vibratorHandler = new Handler();
        volumeTimer = new Timer();

        StartVibrator(data, vibratorHandler);

        PlayAlarmSound();

        b.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                StopAlarm();

                adm.CancelAlarm(data);
                if(mp != null)
                {
                    mp.stop();
                    mp.release();
                    mp = null;
                }
                if(data.IsRepeating())
                {
                    adm.SetAlarm(data);
                }
                else
                {
                    data = adm.Get(index);
                    data.setEnabled(false);
                    adm.Set(index, data);
                    adm.SaveAlarms();
                }
                finish();
            }
        });
    }

    public void StopAlarm()
    {
        vibratorHandler.removeCallbacksAndMessages(null);
        volumeTimer.cancel();
        volumeTimer.purge();

        if(am != null)
            am.setStreamVolume(AudioManager.STREAM_ALARM, userVolume, AudioManager.FLAG_PLAY_SOUND);

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.cancel();
    }

    public void SnoozeAlarm(int offset)
    {
        adm.SetAlarm(data, offset * 1000, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void PlayAlarmSound()
    {
        if(data.getStartVolume() == 0 && data.getEndVolume() == 0)
            return;

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        userVolume = am.getStreamVolume(AudioManager.STREAM_ALARM);
        am.setStreamVolume(AudioManager.STREAM_ALARM, am.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_PLAY_SOUND);

        if (data.getRingtonePath() == null)
            data.setRingtonePath(RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM));
        try
        {
            AudioAttributes.Builder attr = new AudioAttributes.Builder();
            attr.setUsage(AudioAttributes.USAGE_ALARM);
            attr.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
            attr.setLegacyStreamType(AudioManager.STREAM_ALARM);

            mp = MediaPlayer.create(getApplicationContext(), data.getRingtonePath(), null, attr.build(), 0);
            mp.setLooping(true);

            float volume = CalculateVolume(data.getStartVolume());
            mp.setVolume(volume, volume);
            mp.start();
        } catch (Exception e)
        {
            Log.e("Alarm", "Track info: " + mp.getTrackInfo()[0].toString());
            e.printStackTrace();
        }

        if (data.getStartVolume() != data.getEndVolume())
        {
            StartVolumeTimer();
        }
    }

    private void StartVolumeTimer()
    {
        volumeTimer.scheduleAtFixedRate(new TimerTask()
        {
            int time = 0;

            @Override
            public void run()
            {
                if (time >= data.getVolumeLerpLength())
                {
                    volumeTimer.cancel();
                    volumeTimer.purge();
                    return;
                }
                time += 1;
                float percentage = (float) time / (float) data.getVolumeLerpLength();
                float volume = MathHelper.lerp(data.getStartVolume(), data.getEndVolume(), percentage);
                float calculatedVolume = CalculateVolume(volume);
                mp.setVolume(calculatedVolume, calculatedVolume);
            }
        }, 0, 1000);
    }

    private float CalculateVolume(float volume)
    {
        return (float) (1 - (Math.log(100 - volume) / Math.log(100)));
    }

    private void SetWindowFlags()
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private void StartVibrator(final AlarmData data, Handler handler)
    {
        final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (data.getVibrateLerpLength() != 0)
        {
            long[] seq = VibratorUtility.getSequence(data.getStartIntensity(), data.getEndIntensity(), 1000, 1000, data.getVibrateLerpLength());

            v.vibrate(seq, 0);

            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    long[] sequence = VibratorUtility.getSequence(data.getEndIntensity(), data.getEndIntensity(), 1000, 1000, 2);
                    v.vibrate(sequence, 0);
                }
            }, data.getVibrateLerpLength() * 1000);
        } else
        {
            long[] sequence = VibratorUtility.getSequence(data.getEndIntensity(), data.getEndIntensity(), 1000, 1000, 2);
            v.vibrate(sequence, 0);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d("Alarm", "Closed alarm activity");
    }
}
