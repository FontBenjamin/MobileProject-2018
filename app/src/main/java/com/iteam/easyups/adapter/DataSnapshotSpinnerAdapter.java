package com.iteam.easyups.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;

import java.util.List;

/**
 * Created by Marianna on 26/02/2018.
 */

public class DataSnapshotSpinnerAdapter extends ArrayAdapter<DataSnapshot> {

    private Context context;

    private List<DataSnapshot> values;

    public DataSnapshotSpinnerAdapter(Context context, int textViewResourceId,
                                      List<DataSnapshot> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount(){
        return values.size();
    }

    @Override
    public DataSnapshot getItem(int position){
        return values.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        TextView label =  ((TextView) convertView);
        label.setText(values.get(position).getKey());
        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        TextView label =   ((TextView) convertView);
        label.setText(values.get(position).getKey());

        return label;
    }


}
