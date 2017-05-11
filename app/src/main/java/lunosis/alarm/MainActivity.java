package lunosis.alarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;

import lunosis.alarm.editor.AlarmEditor;

public class MainActivity extends AppCompatActivity {

    AlarmDataAdapter adapter;
    AlarmDataManager adm;

    public final int ALARM_DATA_EDITOR = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        adm = ((AlarmApplication)getApplication()).alarmDataManager;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        adapter = new AlarmDataAdapter(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmApplication app = (AlarmApplication)getApplication();
                if(adapter != null)
                {
                    Calendar cal = Calendar.getInstance();
                    AlarmData data = new AlarmData(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
                    adm.Add(data);
                    adapter.notifyItemChanged(adm.Size()-1);
                    adm.SaveAlarms();
                    adm.SetAlarm(data);
                }
            }
        });

        RecyclerView view = ((RecyclerView)findViewById(R.id.stringlist));
        view.setAdapter(adapter);
        view.setLayoutManager(new LinearLayoutManager(this));
    }

    public void StartAlarmEditor(int index)
    {
        Intent i = new Intent(this, AlarmEditor.class);
        i.putExtra("Index", index);
        startActivityForResult(i, ALARM_DATA_EDITOR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            if(requestCode == ALARM_DATA_EDITOR)
            {
                AlarmData alarm = (AlarmData) data.getSerializableExtra("alarmData");
                int index = data.getIntExtra("index", -1);
                adm.Set(index, alarm);
                adm.CancelAlarm(alarm);
                adm.SetAlarm(alarm);
                adm.SaveAlarms();
                adapter.notifyItemChanged(index);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}