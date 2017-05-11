package lunosis.alarm.modifiers;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import lunosis.alarm.AlarmData;

public class SkipModifier extends AlarmModifier
{
    @Override
    public AlarmData Execute(AlarmData data)
    {
        data.skipNext = true;
        return data;
    }

    @Override
    public RecyclerView.ViewHolder OnCreateViewHolder(ViewGroup parent, int viewType)
    {
        return null;
    }

    @Override
    public void OnBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {

    }
}
