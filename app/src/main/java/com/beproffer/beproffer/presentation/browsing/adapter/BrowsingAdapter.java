package com.beproffer.beproffer.presentation.browsing.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.beproffer.beproffer.data.models.BrowsingItem;
import com.bumptech.glide.Glide;
import com.beproffer.beproffer.R;

import java.util.List;

public class BrowsingAdapter extends ArrayAdapter<BrowsingItem> {

    public BrowsingAdapter(Context context, int resourceId, List<BrowsingItem> items) {
        super(context, resourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        BrowsingItem browsingItem = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.browsing_item, parent, false);
        }
        ImageView browsingImage = convertView.findViewById(R.id.browsing_image);

        Glide.with(getContext()).load(browsingItem.getUrl()).into(browsingImage);

        return convertView;
    }
}
