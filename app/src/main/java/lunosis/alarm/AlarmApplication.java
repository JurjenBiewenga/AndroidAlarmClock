package lunosis.alarm;

import android.app.Application;

public class AlarmApplication extends Application {
    public AlarmDataManager alarmDataManager;

    @Override
    public void onCreate()
    {
        super.onCreate();
        alarmDataManager = new AlarmDataManager(this);
    }
}
