package com.ogc.testrecorder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class listCustomAdapter extends ArrayAdapter<listItem> {

    LayoutInflater layoutInflater;

    public listCustomAdapter(Context context, int resource, List<listItem> items){
        super(context, resource, items);
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        final ViewHolder viewHolder;

        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.main_listview_layout, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        listItem item = getItem(position);

        if(item != null){
            viewHolder.titleTextView.setText(item.bookTitle);
        }

        return convertView;
    }

    private class ViewHolder{
        TextView titleTextView;
        public ViewHolder(View view){
            titleTextView = (TextView)view.findViewById(R.id.titleTextView);
        }
    }
}
