package com.example.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter for displaying bed list. Handles displaying red overlay and disabling click for
 * unavailable beds
 */
public class BedsAdapter extends ArrayAdapter<Bed> {
    public BedsAdapter(Context context) {
        super(context, R.layout.bed);
    }

    // TODO implement caching with ViewHolder
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Bed bed = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.bed, null);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.listBedName);
        TextView tvNum = (TextView) convertView.findViewById(R.id.listBedNum);
        // Populate the data into the template view using the data object

        tvName.setText(bed.Name);
        tvNum.setText(bed.Number);

        /* Set red overlay for non-available beds--it was necessary to explicitly set the color
           to transparent for the available beds. First 2 numbers are transparency 00 is transparent
           FF is opaque. */
        ImageView imgView = (ImageView) convertView.findViewById(R.id.redBedOverlay);
        if (bed.Status) {
            imgView.setBackgroundColor(Color.parseColor("#00FF0000"));
        } else {
            imgView.setBackgroundColor(Color.parseColor("#80FF0000"));
        }


        // Return the completed view to render on screen
        return convertView;
    }

    // allows us to use isEnabled to specify which beds are enabled
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    // return bed status, beds with status false are disabled, i.e., can't be clicked b/c they are separators
    @Override
    public boolean isEnabled(int position) {
        return getItem(position).Status;
    }
}
