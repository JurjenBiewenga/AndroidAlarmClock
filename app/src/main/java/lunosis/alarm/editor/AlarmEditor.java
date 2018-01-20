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
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TimePicker;

import lunosis.alarm.AlarmActivity;
import lunosis.alarm.AlarmApplication;
import lunosis.alarm.AlarmData;
import lunosis.alarm.AlarmDataManager;
import lunosis.alarm.R;

public class AlarmEditor extends AppCompatActivity
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

    Switch skipNextSwitch;

    AlarmDataManager adm;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_editor);

        //index = getIntent().getExtras().getInt("Index");
        AlarmApplication app = (AlarmApplication) getApplication();
        adm = app.alarmDataManager;
        //data = adm.Get(index);
        data = (AlarmData) getIntent().getExtras().getSerializable("AlarmData");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                NavUtils.navigateUpFromSameTask(AlarmEditor.this);
                //setResult(RESULT_CANCELED);
                //finish();
            }
        });
        toolbar.setTitle("Alarm Editor");
        setSupportActionBar(toolbar);

        picker = (TimePicker) findViewById(R.id.timePicker);
        picker.setHour(data.getHour());
        picker.setMinute(data.getMinute());

        vibrateLength = (EditText) findViewById(R.id.vibrateIncreaseOverTimeText);
        minVibrateStrength = (EditText) findViewById(R.id.minVibrateStrengthText);
        maxVibrateStrength = (EditText) findViewById(R.id.maxVibrateStrengthText);

        vibrateLength.setText(String.valueOf(data.getVibrateLerpLength()));
        minVibrateStrength.setText(String.valueOf(data.getStartIntensity()));
        maxVibrateStrength.setText(String.valueOf(data.getEndIntensity()));

        volumeLength = (EditText) findViewById(R.id.volumeIncreaseOverTimeText);
        minVolume = (EditText) findViewById(R.id.minVolumeText);
        maxVolume = (EditText) findViewById(R.id.maxVolumeText);
        volumeLength.setText(String.valueOf(data.getVolumeLerpLength()));
        minVolume.setText(String.valueOf(data.getStartVolume()));
        maxVolume.setText(String.valueOf(data.getEndVolume()));

        skipNextSwitch = (Switch) findViewById(R.id.skipNext);
        skipNextSwitch.setChecked(data.ShouldSkipNext());

        Button saveButton = (Button) findViewById(R.id.SaveButton);

        Button ringtoneButton= (Button)findViewById(R.id.ringtoneButton);

        Button testButton = (Button)findViewById(R.id.testButton);

        RecyclerView daySelector= (RecyclerView)findViewById(R.id.daySelector);
        daySelector.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dayAdapter = new DayAdapter(data.days);
        daySelector.setAdapter(dayAdapter);

        skipNextSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                data.setSkipNext(isChecked);
            }
        });

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
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private AlarmData SaveModificationsToAlarmData(AlarmData data)
    {
        data.setHour(picker.getHour());
        data.setMinute(picker.getMinute());

        String length = vibrateLength.getText().toString();
        if (!length.equals(""))
        {
            int vibrateLength = Integer.valueOf(length);
            if(vibrateLength != data.getVibrateLerpLength())
                data.setVibrateLerpLength(vibrateLength);
        }

        String minStrengthText = minVibrateStrength.getText().toString();
        if(!minStrengthText.equals(""))
        {
            int minStrength = Integer.valueOf(minStrengthText);
            if(minStrength != data.getStartIntensity())
                data.setStartIntensity(minStrength);
        }
        String maxStrengthText = maxVibrateStrength.getText().toString();
        if(!maxStrengthText.equals(""))
        {
            int maxStrength = Integer.valueOf(maxStrengthText);
            if(maxStrength!= data.getEndIntensity())
                data.setEndIntensity(maxStrength);
        }

        String volumeLerpLengthText = volumeLength.getText().toString();
        if (!volumeLerpLengthText.equals(""))
        {
            int volumeLerpLength = Integer.valueOf(volumeLerpLengthText);
            if(volumeLerpLength != data.getVolumeLerpLength())
            data.setVolumeLerpLength(volumeLerpLength);
        }

        String minVolText = minVolume.getText().toString();
        if (!minVolText.equals(""))
        {
            int minVol = Integer.valueOf(minVolText);
            if(minVol != data.getStartVolume())
            data.setStartVolume(minVol);
        }

        String maxVolText = maxVolume.getText().toString();
        if (!maxVolText.equals(""))
        {
            int maxVol = Integer.valueOf(maxVolText);
            if(maxVol != data.getEndVolume())
                data.setEndVolume(maxVol);
        }

        if(selectedRingtone != null)
            data.setRingtonePath(selectedRingtone);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.SaveButton) {
            AlarmApplication app = (AlarmApplication) getApplication();

            data = SaveModificationsToAlarmData(data);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("alarmData", data);
            resultIntent.putExtra("index", index);
            setResult(RESULT_OK, resultIntent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
