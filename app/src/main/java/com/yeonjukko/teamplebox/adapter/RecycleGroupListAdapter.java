package com.yeonjukko.teamplebox.adapter;

/**
 * Created by yeonju on 2015-09-19.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yeonjukko.teamplebox.MyTeampleBox.MyActivity;
import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.TeampleBox.MaterialActivity;
import com.yeonjukko.teamplebox.TeampleBox.Newsfeed.NewsfeedFragment;
import com.yeonjukko.teamplebox.libs.AndroidDBManager;
import com.yeonjukko.teamplebox.libs.AndroidSessionManager;
import com.yeonjukko.teamplebox.libs.ImageDownloadManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by MoonJR on 2015. 8. 27..
 */
public class RecycleGroupListAdapter extends RecyclerView.Adapter<RecycleGroupListAdapter.ViewHolder> {

    private Activity activity;
    private JSONArray data;


    public RecycleGroupListAdapter(JSONArray data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public RecycleGroupListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_list_adapter, parent, false);

        // create ViewHolder

        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData

        final JSONObject data = (JSONObject) this.data.get(position);
        if (data.get("group_name") != null) {
            viewHolder.mTextViewName.setText((String) data.get("group_name"));
        }
        if (data.get("group_image") != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Bitmap mybitmap = ImageDownloadManager.downloadImage((String) data.get("group_image"));
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewHolder.mImageViewThumbs.setImageBitmap(mybitmap);
                        }
                    });
                }
            }).start();

        }

        viewHolder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gid = (String) data.get("group_id");
                String gname = (String) data.get("group_name");
                String gimage = (String)data.get("group_image");
                Intent intent = new Intent(activity, MaterialActivity.class);
                intent.putExtra("gname", gname);
                intent.putExtra("gid", gid);
                intent.putExtra("gimage",gimage);
                intent.putExtra("myimage",AndroidSessionManager.getInstance().getImgUrl());
                activity.startActivity(intent);

            }
        });

        viewHolder.mLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Snackbar
                        .make(v, "\"" + data.get("group_name") + "\"그룹을 삭제하시겠습니까?", Snackbar.LENGTH_LONG)
                        .setAction("삭제", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        HashMap<String, String> query = new HashMap<String, String>();
                                        query.put("email", AndroidSessionManager.getInstance().getEmail());
                                        query.put("gid", (String) data.get("group_id"));
                                        final JSONObject result = AndroidDBManager.deleteGroup(query);
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (result != null) {
                                                    if (!(Boolean) result.get("error")) {
                                                        if ((Boolean) result.get("success")) {

                                                            Toast.makeText(activity, "그룹을 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                                                            ((MyActivity) activity).startGroupLoadThread();
                                                        } else {
                                                            Toast.makeText(activity, "데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();

                                                        }

                                                    } else {
                                                        Toast.makeText(activity, "에러가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        });

                                    }
                                }).start();

                            }
                        }).show();

                return true;
            }
        });

    }

    // inner class to hold a reference to each item of RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout mLinearLayout;
        public CircleImageView mImageViewThumbs;
        public TextView mTextViewName;


        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            mLinearLayout = (LinearLayout) itemLayoutView.findViewById(R.id.layout_group);
            mImageViewThumbs = (CircleImageView) itemLayoutView.findViewById(R.id.groupCircleImageView);
            mTextViewName = (TextView) itemLayoutView.findViewById(R.id.textViewGroupName);
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