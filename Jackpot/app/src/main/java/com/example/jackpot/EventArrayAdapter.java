package com.example.jackpot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class EventArrayAdapter extends ArrayAdapter<Event> {
    public EventArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.entrant_event_content, parent, false);
        } else {
            view = convertView;
        }

        Event event = getItem(position);
        ImageView eventPic = view.findViewById(R.id.eventPic);
        TextView eventName = view.findViewById(R.id.event_text);

        assert event != null;
//        eventPic.setImageResource(event.getPosterImage());
        eventName.setText(event.getTitle());
        return view;
    }
}
