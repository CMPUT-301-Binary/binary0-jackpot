package com.example.jackpot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Locale;

public class EventArrayAdapter extends ArrayAdapter<Event> {
    private final int layoutResource;
    public EventArrayAdapter(Context context, ArrayList<Event> events, int layoutResource) {
        super(context, 0, events);
        this.layoutResource = layoutResource;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(this.layoutResource, parent, false);
        } else {
            view = convertView;
        }

        Event event = getItem(position);
        ImageView eventImage = view.findViewById(R.id.event_image);
        TextView eventCategory = view.findViewById(R.id.event_category);
        TextView eventName = view.findViewById(R.id.event_name);
        TextView eventPrice = view.findViewById(R.id.event_price);
        TextView eventSpots = view.findViewById(R.id.event_spots);
        TextView eventWaiting = view.findViewById(R.id.event_waiting);
        Button joinButton = view.findViewById(R.id.join_button);

        if (event != null) {
            // eventImage.setImageResource(event.getPosterImage());
            eventCategory.setText(event.getCategory());
            eventName.setText(event.getTitle());
            String price = event.getPrice().toString();
            if (price.equals("0.0")) {
                price = "Free";
            }
            eventPrice.setText(price);
            String spotsText = String.format(Locale.getDefault(), "%d spots", event.getCapacity());
            eventSpots.setText(spotsText);
            String waiting;
            if (event.getWaitingList() != null){
                waiting = String.format(Locale.getDefault(), "%d waiting", event.getWaitingList().size());
            }
            else {
                waiting = "0 waiting";
            }
            eventWaiting.setText(waiting);


        }




        return view;
    }
}
