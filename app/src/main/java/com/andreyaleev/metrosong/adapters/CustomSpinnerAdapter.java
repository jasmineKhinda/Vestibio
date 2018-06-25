package com.andreyaleev.metrosong.adapters;

/**
 * Created by jkhinda on 24/06/18.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andreyaleev.metrosong.R;

import java.util.List;

import pntanasis.android.metronome.Beats;

/**
 * Created by jasmine on 08/02/18.
 */

public class CustomSpinnerAdapter extends ArrayAdapter<Beats> {
    LayoutInflater inflater;
    List<Beats> spinnerItems;

    public CustomSpinnerAdapter(Context applicationContext, int resource, List<Beats> spinnerItems) {
        super(applicationContext, resource, spinnerItems);
        this.spinnerItems = spinnerItems;
        inflater = (LayoutInflater.from(applicationContext));
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.custom_spinner_layout, null);
        TextView type = (TextView) view.findViewById(R.id.spinner_item_text);
        type.setText(spinnerItems.get(i).toString());
        return view;
    }
}