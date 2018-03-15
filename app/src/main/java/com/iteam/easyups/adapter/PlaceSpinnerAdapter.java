package com.iteam.easyups.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.iteam.easyups.model.Place;

import java.util.List;

public class PlaceSpinnerAdapter extends ArrayAdapter {

    private List<Place> places;

    public PlaceSpinnerAdapter(Context context, int textViewResourceId, List<Place> places) {
        super(context, textViewResourceId, places);
        this.places = places;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView label = (TextView) view;
        label.setText(places.get(position).getName());
        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView label = (TextView) view;
        label.setText(places.get(position).getName());
        if (position == 0)
        {
            label.setTextColor(Color.GRAY);
        }
        else
        {
            label.setTextColor(Color.BLACK);
        }
        return label;
    }

    @Override
    public boolean isEnabled(int position){
        return true;
    }

    @Override
    public int getCount(){
        return places.size();
    }

    @Override
    public Place getItem(int position){
        return places.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

}
