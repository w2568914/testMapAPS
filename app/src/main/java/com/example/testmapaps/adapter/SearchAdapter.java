package com.example.testmapaps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.testmapaps.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.*;

public class SearchAdapter extends BaseAdapter {

    private List<HashMap<String,String>> addressData;
    private LayoutInflater layoutInflater;

    public SearchAdapter(Context context) {
        layoutInflater=LayoutInflater.from(context);
        addressData=new ArrayList<HashMap<String, String>>() ;


    }

    @Override
    public int getCount() {
        return addressData.size();
    }

    @Override
    public Object getItem(int position) {
        return addressData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;

        return convertView;

    }
}
