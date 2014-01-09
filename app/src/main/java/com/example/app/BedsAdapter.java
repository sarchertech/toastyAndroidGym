package com.example.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Adapter for displaying bed list
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
        // Return the completed view to render on screen
        return convertView;
    }
}
