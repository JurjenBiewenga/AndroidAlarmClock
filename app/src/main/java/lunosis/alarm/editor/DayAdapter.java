package lunosis.alarm.editor;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import lunosis.alarm.AlarmData;
import lunosis.alarm.R;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.ViewHolder>
{
    public Boolean[] selectedDays;
    public int enabledColor = Color.GREEN;
    public int disabledColor = Color.GRAY;


    public DayAdapter(Boolean[] days)
    {
        selectedDays = days;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.day_item, parent, false);

        // Return a new holder instance
        DayAdapter.ViewHolder viewHolder = new DayAdapter.ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        holder.button.setText(AlarmData.weekdays[position]);
        holder.button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int position = holder.getAdapterPosition();
                selectedDays[position] = !selectedDays[position];
                if(selectedDays[position])
                    holder.button.setBackgroundColor(enabledColor);
                else
                    holder.button.setBackgroundColor(disabledColor);
            }
        });
        if(selectedDays[position])
            holder.button.setBackgroundColor(enabledColor);
        else
            holder.button.setBackgroundColor(disabledColor);
    }

    @Override
    public int getItemCount()
    {
        return selectedDays.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Button button;

        public ViewHolder(View itemView) {
            super(itemView);
            button = (Button) itemView.findViewById(R.id.dayButton);
        }
    }
}
