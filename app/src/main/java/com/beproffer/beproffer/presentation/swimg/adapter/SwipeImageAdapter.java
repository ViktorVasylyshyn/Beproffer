package com.beproffer.beproffer.presentation.swimg.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.SwipeImageItem;

import java.util.List;

public class SwipeImageAdapter extends ArrayAdapter<SwipeImageItem> {

    public SwipeImageAdapter(Context context, int resourceId, List<SwipeImageItem> items) {
        super(context, resourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        SwipeImageItem swipeImageItem = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.swipe_image_item, parent, false);
        }
        ImageView swipeImage = convertView.findViewById(R.id.swipe_image);

        Glide.with(getContext()).load(swipeImageItem.getUrl()).into(swipeImage);

        return convertView;
    }
}
