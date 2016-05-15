package com.yeonjukko.teamplebox.adapter;

/**
 * Created by yeonju on 2015-09-19.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yeonjukko.teamplebox.MyTeampleBox.MyActivity;
import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.TeampleBox.MaterialActivity;
import com.yeonjukko.teamplebox.libs.AndroidDBManager;
import com.yeonjukko.teamplebox.libs.AndroidSessionManager;
import com.yeonjukko.teamplebox.libs.ImageDownloadManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class RecycleAttendanceListAdapter extends RecyclerView.Adapter<RecycleAttendanceListAdapter.ViewHolder> {

    private Activity activity;
    private JSONArray data;
    private Fragment fragment;


    public RecycleAttendanceListAdapter(JSONArray data, Activity activity, Fragment fragment) {
        this.data = data;
        this.activity = activity;
        this.fragment = fragment;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public RecycleAttendanceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                      int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.attendance_list_adapter, parent, false);

        // create ViewHolder

        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData
        Log.d("test",data.toJSONString());

        final JSONObject data = (JSONObject) this.data.get(position);
        if (data.get("user_name") != null) {
            viewHolder.mTextViewName.setText((String) data.get("user_name"));
        }
        if (data.get("attendance_percent") != null) {

            viewHolder.mTextViewAttendance.setText((long) data.get("attendance_percent") + "%");
        }
        if (data.get("user_image") != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Bitmap myBitmap = ImageDownloadManager.downloadImage((String) data.get("user_image"));
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewHolder.mImageViewThumbs.setImageBitmap(myBitmap);
                        }
                    });
                }
            }).start();


        }


        viewHolder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //추가기능 : 상대방에게 출석 푸쉬하기
//                String gid = (String) data.get("group_id");
//                String gname = (String) data.get("group_name");
//                Intent intent = new Intent(activity, MaterialActivity.class);
//                intent.putExtra("gname", gname);
//                intent.putExtra("gid", gid);
//                activity.startActivity(intent);

            }
        });

    }

    // inner class to hold a reference to each item of RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout mLinearLayout;
        public CircleImageView mImageViewThumbs;
        public TextView mTextViewName;
        public TextView mTextViewAttendance;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            mLinearLayout = (LinearLayout) itemLayoutView.findViewById(R.id.layout);
            mImageViewThumbs = (CircleImageView) itemLayoutView.findViewById(R.id.mCircleImageView);
            mTextViewName = (TextView) itemLayoutView.findViewById(R.id.tv_name);
            mTextViewAttendance = (TextView) itemLayoutView.findViewById(R.id.tv_attend);
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