package hoahong.facebook.messenger.ui;

import hoahong.facebook.messenger.R;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Copyright (c) 2014 Pendulab Inc.
 * 111 N Chestnut St, Suite 200, Winston Salem, NC, 27101, U.S.A.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of
 * Pendulab Inc. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Pendulab.
 * Created by Vu Trong Hoa on 22/5/2015.
 */
public class ImageBrowserPickOneAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private int selectedPosition;
    private List<String> listImagesUris;
    public ImageBrowserPickOneAdapter (Context context, List<String> listImages) {
        this.context = context;
        this.listImagesUris = listImages;
        this.inflater = LayoutInflater.from(context);
        this.selectedPosition = -1;
    }

    @Override
    public int getCount() {
        return listImagesUris == null ? 0 : listImagesUris.size();
    }

    @Override
    public String getItem(int position) {
        return listImagesUris.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup arg2) {
        View view = convertView;
        ViewHolder holder = null;
        if (view == null) {
            view = inflater.inflate(
                    R.layout.attachment_gallery_images_item, null);
            holder = new ViewHolder();
            holder.checkStatus = (ImageView) view
                    .findViewById(R.id.imageCheckStatus);
            holder.imageItem = (SquaredImageView) view
                    .findViewById(R.id.image_item_in_gridview);
            holder.imageFrame = view.findViewById(R.id.photo_frame);
            view.setTag(holder);
        }else
            holder = (ViewHolder) view.getTag();
        // data
        final String imageUri = getItem(position);

        // this is for the border
        holder.imageFrame = view.findViewById(R.id.photo_frame);
        final ViewHolder finalHolder = holder;
        holder.checkStatus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!v.isSelected()) {
                    v.setSelected(true);
                    finalHolder.imageFrame
                            .setBackgroundResource(R.drawable.photoborder);
                    ((ImageView) v)
                            .setImageResource(R.drawable.selectphoto_small_active);
                    v.setBackgroundColor(0xff42d1f6);
                    selectedPosition = position;
                    ImageBrowserPickOneAdapter.this.notifyDataSetChanged();
                } else {
                    v.setSelected(false);
                    finalHolder.imageFrame.setBackgroundDrawable(null);
                    ((ImageView) v)
                            .setImageResource(R.drawable.selectphoto_small);
                    v.setBackgroundColor(0x501c1c1c);
                    selectedPosition = -1;
                }

            }
        });

        // displays the selected items
        if (position == selectedPosition) {
            holder.checkStatus
                    .setImageResource(R.drawable.selectphoto_small_active);
            holder.checkStatus.setBackgroundColor(0xff42d1f6);
            holder.imageFrame.setBackgroundResource(R.drawable.photoborder);
        } else {
            holder.checkStatus.setImageResource(R.drawable.selectphoto_small);
            holder.checkStatus.setBackgroundColor(0x501c1c1c);
            holder.imageFrame.setBackgroundDrawable(null);
        }

        // Trigger the download of the URL asynchronously into the image
        // view.
        Picasso.with(context) //
                .load("file://" + imageUri) // .placeholder(R.drawable.photoview_placeholder)
                .error(R.drawable.nophotos) //
                .fit().centerCrop() //
                .into(holder.imageItem);

        return view;
    }


    private class ViewHolder {
        public SquaredImageView imageItem;
        public ImageView checkStatus;
        public View imageFrame;
    }
}
