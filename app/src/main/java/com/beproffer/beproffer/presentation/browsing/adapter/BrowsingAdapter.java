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

import static com.beproffer.beproffer.util.OsVersionInfo.hasLollipop;

public class BrowsingAdapter extends ArrayAdapter<BrowsingItem> {

    public BrowsingAdapter(Context context, int resourceId, List<BrowsingItem> items) {
        super(context, resourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        BrowsingItem browsingItem = getItem(position);

        int layoutId;
        /*потому что  не смог я настроить нормально отображения картинок на ос меньше 21. чем больше
        делаешь радиус углов тем больше изображение этим сдвигается вцентр, тоесть углы не
        хотят обрезаться поетому на апи 19 будет без округления углов*/
        if (hasLollipop()) {
            layoutId = R.layout.browsing_item21h;
        } else {
            layoutId = R.layout.browsing_item19;
        }

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
        }
        ImageView browsingImage = convertView.findViewById(R.id.browsing_image);

        Glide.with(getContext()).load(browsingItem.getUrl()).into(browsingImage);

        return convertView;
    }
}
