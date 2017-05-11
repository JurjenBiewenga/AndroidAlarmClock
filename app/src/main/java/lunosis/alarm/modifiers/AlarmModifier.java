package lunosis.alarm.modifiers;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.io.Serializable;

import lunosis.alarm.AlarmData;

public abstract class AlarmModifier implements Serializable
{
    public abstract AlarmData Execute(AlarmData data);
    public abstract RecyclerView.ViewHolder OnCreateViewHolder(ViewGroup parent, int viewType);

    public abstract void OnBindViewHolder(final RecyclerView.ViewHolder holder, int position);
}
