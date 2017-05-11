package lunosis.alarm.editor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import lunosis.alarm.AlarmActivity;
import lunosis.alarm.AlarmApplication;
import lunosis.alarm.AlarmData;
import lunosis.alarm.AlarmDataManager;
import lunosis.alarm.R;

public class AlarmEditor extends Activity
{
    Uri selectedRingtone = null;
    int index;
    TimePicker picker;
    AlarmData data;
    DayAdapter dayAdapter;

    EditText vibrateLength;
    EditText minVibrateStrength;
    EditText maxVibrateStrength;
    EditText volumeLength;
    EditText minVolume;
    EditText maxVolume;

    AlarmDataManager adm;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_editor);

        index = getIntent().getExtras().getInt("Index");
        AlarmApplication app = (AlarmApplication) getApplication();
        adm = app.alarmDataManager;
        data = adm.Get(index);

        picker = (TimePicker) findViewById(R.id.timePicker);
        picker.setHour(data.hour);
        picker.setMinute(data.minute);

        vibrateLength = (EditText) findViewById(R.id.vibrateIncreaseOverTimeText);
        minVibrateStrength = (EditText) findViewById(R.id.minVibrateStrengthText);
        maxVibrateStrength = (EditText) findViewById(R.id.maxVibrateStrengthText);

        vibrateLength.setText(String.valueOf(data.vibrateLerpLength));
        minVibrateStrength.setText(String.valueOf(data.startIntensity));
        maxVibrateStrength.setText(String.valueOf(data.endIntensity));

        volumeLength = (EditText) findViewById(R.id.volumeIncreaseOverTimeText);
        minVolume = (EditText) findViewById(R.id.minVolumeText);
        maxVolume = (EditText) findViewById(R.id.maxVolumeText);
        volumeLength.setText(String.valueOf(data.volumeLerpLength));
        minVolume.setText(String.valueOf(data.startVolume));
        maxVolume.setText(String.valueOf(data.endVolume));

        Button saveButton = (Button) findViewById(R.id.SaveButton);

        Button ringtoneButton= (Button)findViewById(R.id.ringtoneButton);

        Button testButton = (Button)findViewById(R.id.testButton);

        RecyclerView daySelector= (RecyclerView)findViewById(R.id.daySelector);
        daySelector.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dayAdapter = new DayAdapter(data.days);
        daySelector.setAdapter(dayAdapter);

        testButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(v.getContext(), AlarmActivity.class);
                i.putExtra("Index", data.id);
                v.getContext().startActivity(i);
            }
        });

        ringtoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data = SaveModificationsToAlarmData(data);
                Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
                startActivityForResult(i, 0);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmApplication app = (AlarmApplication) getApplication();

                data = SaveModificationsToAlarmData(data);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("alarmData", data);
                resultIntent.putExtra("index", index);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private AlarmData SaveModificationsToAlarmData(AlarmData data)
    {
        data.hour = picker.getHour();
        data.minute = picker.getMinute();

        String length = vibrateLength.getText().toString();
        if (!length.equals(""))
            data.vibrateLerpLength = Integer.valueOf(length);
        String minStrength = minVibrateStrength.getText().toString();
        if(!minStrength.equals(""))
            data.startIntensity = Integer.valueOf(minStrength);
        String maxStrength = maxVibrateStrength.getText().toString();
        if(!maxStrength.equals(""))
            data.endIntensity = Integer.valueOf(maxStrength);

        String volume = volumeLength.getText().toString();
        if (!volume.equals(""))
            data.volumeLerpLength = Integer.valueOf(volume);

        String minVol = minVolume.getText().toString();
        if (!minVol.equals(""))
            data.startVolume = Integer.valueOf(minVol);

        String maxVol = maxVolume.getText().toString();
        if (!maxVol.equals(""))
            data.endVolume = Integer.valueOf(maxVol);

        if(selectedRingtone != null)
            data.ringtonePath = selectedRingtone;
        else
            selectedRingtone= RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_ALL);

        data.days = dayAdapter.selectedDays;
        return data;
    }

    private static Uri getRingtoneUriFromPath(Context context, String path) {
        Uri ringtonesUri = MediaStore.Audio.Media.getContentUriForPath(path);
        Cursor ringtoneCursor = context.getContentResolver().query(
                ringtonesUri, null,
                MediaStore.Audio.Media.DATA + "='" + path + "'", null, null);

        assert ringtoneCursor != null;
        ringtoneCursor.moveToFirst();

        long id = ringtoneCursor.getLong(ringtoneCursor
                .getColumnIndex(MediaStore.Audio.Media._ID));
        ringtoneCursor.close();

        if (!ringtonesUri.toString().endsWith(String.valueOf(id))) {
            return Uri.parse(ringtonesUri + "/" + id);
        }
        return ringtonesUri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            String path = uri.toString();
            if (path.startsWith("content://")) {
                try {
                    selectedRingtone = getRingtoneUriFromPath(getApplicationContext(), path);
                    Log.d("Alarm", selectedRingtone.toString());
                } catch (Exception e) {
                }
            }
            else
                selectedRingtone = uri;
        }
    }
}
