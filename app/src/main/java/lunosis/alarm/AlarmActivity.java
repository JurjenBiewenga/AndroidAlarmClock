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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm);
        final int index = getIntent().getExtras().getInt("Index");

        AlarmApplication app = (AlarmApplication) getApplication();
        adm = app.alarmDataManager;

        for (int i = 0; i < adm.Size(); i++)
        {
            AlarmData alarmData = adm.Get(i);
            if (alarmData.id == index)
            {
                data = alarmData;
                break;
            }
        }

        if (data == null)
        {
            finish();
            return;
        }

        Button b = (Button) findViewById(R.id.StopButton);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        final Handler vibratorHandler = new Handler();
        final Timer volumeTimer = new Timer();

        StartVibrator(data, vibratorHandler);

        final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        userVolume = am.getStreamVolume(AudioManager.STREAM_ALARM);
        am.setStreamVolume(AudioManager.STREAM_ALARM, am.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_PLAY_SOUND);


        if (data.ringtonePath == null)
            data.ringtonePath = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
        try
        {
            AudioAttributes.Builder attr = new AudioAttributes.Builder();
            attr.setUsage(AudioAttributes.USAGE_ALARM);
            attr.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
            attr.setLegacyStreamType(AudioManager.STREAM_ALARM);

            mp = MediaPlayer.create(getApplicationContext(), data.ringtonePath, null, attr.build(), 0);
            mp.setLooping(true);

            final float volume = (float) (1 - (Math.log(100 - data.startVolume) / Math.log(100)));
            mp.setVolume(volume, volume);
            mp.start();
        } catch (Exception e)
        {
            Log.e("Alarm", "Track info: " + mp.getTrackInfo()[0].toString());
            e.printStackTrace();
        }

        if (data.startVolume != data.endVolume)
        {
            volumeTimer.scheduleAtFixedRate(new TimerTask()
            {
                int time = 0;

                @Override
                public void run()
                {
                    if (time >= data.volumeLerpLength)
                    {
                        volumeTimer.cancel();
                        volumeTimer.purge();
                        return;
                    }
                    time += 1;
                    float percentage = (float) time / (float) data.volumeLerpLength;
                    float volume = MathHelper.lerp(data.startVolume, data.endVolume, percentage);
                    float calculatedVolume = (float) (1 - (Math.log(100 - volume) / Math.log(100)));
                    mp.setVolume(calculatedVolume, calculatedVolume);
                }
            }, 0, 1000);
        }

        b.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlarmManager a = (AlarmManager) v.getContext().getSystemService(ALARM_SERVICE);
                Intent i = new Intent(v.getContext(), AlarmReceiver.class);
                PendingIntent p = PendingIntent.getBroadcast(v.getContext(), index, i, 0);
                vibratorHandler.removeCallbacksAndMessages(null);
                volumeTimer.cancel();
                volumeTimer.purge();

                am.setStreamVolume(AudioManager.STREAM_ALARM, userVolume, AudioManager.FLAG_PLAY_SOUND);

                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.cancel();

                if (p != null)
                    a.cancel(p);
                mp.stop();
                mp.release();
                mp = null;
                adm.SetAlarm(data);
                finish();
            }
        });
    }

    private void StartVibrator(final AlarmData data, Handler handler)
    {
        final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (data.vibrateLerpLength != 0)
        {
            long[] seq = VibratorUtility.getSequence(data.startIntensity, data.endIntensity, 1000, 1000, data.vibrateLerpLength);

            v.vibrate(seq, 0);

            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    long[] sequence = VibratorUtility.getSequence(data.endIntensity, data.endIntensity, 1000, 1000, 2);
                    v.vibrate(sequence, 0);
                }
            }, data.vibrateLerpLength * 1000);
        } else
        {
            long[] sequence = VibratorUtility.getSequence(data.endIntensity, data.endIntensity, 1000, 1000, 2);
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
