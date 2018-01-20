package lunosis.alarm;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SnoozeOptionsAdapter extends RecyclerView.Adapter<SnoozeOptionsAdapter.ViewHolder> {


public static class ViewHolder extends RecyclerView.ViewHolder {

    public Button button;

    public ViewHolder(View itemView) {
        super(itemView);
        this.button = (Button) itemView.findViewById(R.id.snooze);
    }
}

    int[] snoozeOptions;
    AlarmActivity alarm;

    public SnoozeOptionsAdapter(int[] snoozeOptions, AlarmActivity alarmActivity) {
        this.snoozeOptions = snoozeOptions;
        alarm = alarmActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.snooze_item, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final int index = position;
        holder.button.setText(snoozeOptions[index]);
        holder.button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                alarm.StopAlarm();
                alarm.SnoozeAlarm(snoozeOptions[index]);
            }
        });
    }

    @Override
    public int getItemCount() {
        return snoozeOptions.length;
    }
}
