package lunosis.alarm;

import android.app.Application;
import android.util.Log;

public class AlarmApplication extends Application {
    public AlarmDataManager alarmDataManager;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d("Alarm", "Application started");
        alarmDataManager = new AlarmDataManager(this);
    }
}
