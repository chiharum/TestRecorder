package com.ogc.testrecorder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class questionsListCustomAdapter extends ArrayAdapter<questionsListItem> {

    LayoutInflater layoutInflater;

    public questionsListCustomAdapter(Context context, int resource, List<questionsListItem> items){
        super(context, resource, items);
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        final ViewHolder viewHolder;

        if (convertView == null){
            convertView = layoutInflater.inflate(R.layout.list_listview_layout, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        questionsListItem item = getItem(position);

        if(item != null){
            viewHolder.questionNumberTextView.setText(String.valueOf(item.questionNumber));
            viewHolder.correctTimesTextView.setText(item.correctTimes + " / " + item.challengedTimes);
        }

        return convertView;
    }

    private class ViewHolder{
        TextView questionNumberTextView, correctTimesTextView;
        public ViewHolder(View view){
            questionNumberTextView = (TextView)view.findViewById(R.id.questionsListquestionNumberText);
            correctTimesTextView = (TextView)view.findViewById(R.id.questionListCorrectTimesText);
        }
    }
}
