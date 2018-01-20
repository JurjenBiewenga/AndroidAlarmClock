package lunosis.alarm;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AlarmDataAdapter extends RecyclerView.Adapter<AlarmDataAdapter.ViewHolder> {

    private Context context;
    private AlarmDataManager adm;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView time;
        public Switch enabledSwitch;

        public ViewHolder(View itemView) {
            super(itemView);
            time = (TextView) itemView.findViewById(R.id.timeText);
            enabledSwitch = (Switch)itemView.findViewById(R.id.enabled);
        }
    }

    public AlarmDataAdapter(Context c) {
        context = c;
        adm = ((AlarmApplication)c.getApplicationContext()).alarmDataManager;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.alarm_data_item, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final AlarmData alarm = adm.Get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date d= new Date(0,0,0, alarm.getHour(), alarm.getMinute(), 0);
        holder.time.setText(sdf.format(d));
        holder.enabledSwitch.setChecked(alarm.isEnabled());

        holder.enabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @TargetApi(Build.VERSION_CODES.N)
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                adm.Get(holder.getAdapterPosition()).setEnabled(isChecked);
                if(isChecked)
                    adm.SetAlarm(alarm);
                else
                    adm.CancelAlarm(alarm);
                adm.SaveAlarms();
            }
        });
        holder.itemView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)context).StartAlarmEditor(adm.Get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return adm.Size();
    }
}

