package com.florianmski.airportcodes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.florianmski.airportcodes.data.models.Airport;
import com.florianmski.spongeframework.adapters.RecyclerAdapter;
import com.florianmski.spongeframework.ui.widgets.Placeholder;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ListAirportAdapter extends RecyclerAdapter<Airport, ListAirportAdapter.ViewHolder>
{
    private Placeholder placeholder;

    public ListAirportAdapter(Context context, List<Airport> data, OnItemClickListener listener)
    {
        super(context, data, listener);
        placeholder = new Placeholder(context.getResources().getColor(R.color.primary), context.getResources().getColor(R.color.primaryDark));
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position)
    {
        return getItem2(position).id.hashCode();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_airport, parent, false);
        return new ViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position)
    {
        Airport a = getItem2(position);
        viewHolder.tv.setText(a.code);
        Picasso.with(context).load(a.imageCard).placeholder(placeholder.getDrawable()).into(viewHolder.iv);
    }

    public static class ViewHolder extends RecyclerAdapter.ViewHolder
    {
        public ImageView iv;
        public TextView tv;

        public ViewHolder(View itemView, OnItemClickListener listener)
        {
            super(itemView, listener);

            iv = (ImageView) itemView.findViewById(R.id.imageView);
            tv = (TextView) itemView.findViewById(R.id.textViewCode);
        }
    }
}
