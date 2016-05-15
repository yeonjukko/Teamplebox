package com.yeonjukko.teamplebox.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.libs.ImageDownloadManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by yeonjukko on 15. 11. 4..
 */

public class RecycleTeamInfoListAdapter extends RecyclerView.Adapter<RecycleTeamInfoListAdapter.ViewHolder> {

    private Activity activity;
    private JSONArray data;


    public RecycleTeamInfoListAdapter(JSONArray data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public RecycleTeamInfoListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                    int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_team_info_adapter, parent, false);

        // create ViewHolder

        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        Log.d("test"," onCreateViewHolder()");
        return viewHolder;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData
        Log.d("test", data.toJSONString());

        if(position==0){
            viewHolder.mImageViewMedal.setVisibility(View.VISIBLE);
            viewHolder.mImageViewMedal.setImageResource(R.drawable.gold_medal);
        }else if(position==1){
            viewHolder.mImageViewMedal.setVisibility(View.VISIBLE);
            viewHolder.mImageViewMedal.setImageResource(R.drawable.silver_medal);
        }else if(position==2){
            viewHolder.mImageViewMedal.setVisibility(View.VISIBLE);
            viewHolder.mImageViewMedal.setImageResource(R.drawable.bronze_medal);
        }else{
            viewHolder.mImageViewMedal.setVisibility(View.INVISIBLE);
        }

        final JSONObject data = (JSONObject) this.data.get(position);
        if (data.get("name") != null) {
            viewHolder.mTextViewName.setText((String) data.get("name"));
        }
        if (data.get("email") != null) {

            viewHolder.mTextViewEmail.setText((String) data.get("email"));
        }
        if (data.get("image") != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Bitmap myBitmap = ImageDownloadManager.downloadImage((String) data.get("image"));
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewHolder.mImageViewThumbs.setImageBitmap(myBitmap);
                        }
                    });
                }
            }).start();

        }
        if (data.get("point") != null) {

            viewHolder.mTextViewPoint.setText((String) data.get("point"));
        }

    }


    // inner class to hold a reference to each item of RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView mImageViewThumbs;
        public TextView mTextViewName;
        public TextView mTextViewEmail;
        public TextView mTextViewPoint;
        public ImageView mImageViewMedal;


        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            mImageViewThumbs = (CircleImageView) itemLayoutView.findViewById(R.id.img_circle);
            mTextViewName = (TextView) itemLayoutView.findViewById(R.id.tv_name);
            mTextViewEmail = (TextView) itemLayoutView.findViewById(R.id.tv_email);
            mTextViewPoint = (TextView) itemLayoutView.findViewById(R.id.tv_point);
            mImageViewMedal = (ImageView)itemLayoutView.findViewById(R.id.img_medal);
        }
    }


    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return data.size();
    }


    public JSONArray getData() {
        return data;
    }
}