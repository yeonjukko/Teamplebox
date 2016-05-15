package com.yeonjukko.teamplebox.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.TeampleBox.Newsfeed.CommentActivity;
import com.yeonjukko.teamplebox.TeampleBox.Newsfeed.NewsfeedFragment;
import com.yeonjukko.teamplebox.libs.AndroidDBManager;
import com.yeonjukko.teamplebox.libs.AndroidSessionManager;
import com.yeonjukko.teamplebox.libs.ImageDownloadManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by yeonju on 2015-09-29.
 */
public class RecycleNewsfeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private Activity activity;
    private Fragment fragment;
    private JSONArray datas;
    private String gid;
    private AndroidSessionManager sessionManager;

    public RecycleNewsfeedAdapter(JSONArray datas, Activity activity, String gid, Fragment fragment) {
        this.datas = datas;
        this.gid = gid;
        this.activity = activity;
        this.fragment = fragment;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        if (viewType == TYPE_ITEM) {
            // create a new view
            View itemLayoutView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.newsfeed_adapter, parent, false);
            // create ViewHolder
            ViewHolder viewHolder = new ViewHolder(itemLayoutView);
            return viewHolder;
        } else if (viewType == TYPE_HEADER) {
            View itemLayoutView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.newsfeed_header_adapter, parent, false);
            // create ViewHolder
            ViewHolderHeader viewHolder = new ViewHolderHeader(itemLayoutView);
            return viewHolder;
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData
        //꾸밀 곳

        if (holder instanceof ViewHolder) {
            final ViewHolder viewHolder = (ViewHolder) holder;
            final JSONObject data = (JSONObject) datas.get(datas.size() - position);
            if (data.get("reg_date") != null) {
                viewHolder.mTextViewTime.setText(AndroidSessionManager.countUpdateTime((long) data.get("reg_date")));
            }
            if (data.get("user_name") != null) {
                viewHolder.mTextViewName.setText((String) data.get("user_name"));
            }
            if (data.get("post_content") != null) {
                viewHolder.mTextViewContent.setText((String) data.get("post_content"));
            }
            if (data.get("writer_image") != null) {
                new Thread() {
                    @Override
                    public void run() {
                        final Bitmap myBitmap = ImageDownloadManager.downloadImage((String) data.get("writer_image"));
                        if (myBitmap != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    viewHolder.mImageViewThumbs.setImageBitmap(myBitmap);
                                }
                            });

                        }//null일때


                    }
                }.start();

            }

            if (data.get("is_postit") != null) {
                if ((Boolean) data.get("is_postit")) {
                    viewHolder.imageViewPostIt.setImageResource(R.drawable.ic_visible_postit_converted);
                } else {
                    viewHolder.imageViewPostIt.setImageResource(R.drawable.ic_invisible_postit_converted);

                }


            }

            viewHolder.imageViewPostIt.setAlpha(0.7f);
            viewHolder.imageViewPostIt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("test", "click");

                    final ProgressDialog dialog = new ProgressDialog(activity);
                    dialog.setTitle("알림");
                    dialog.setMessage("포스트잇 등록 중..");
                    dialog.setCancelable(false);
                    dialog.show();
                    //포스트잇 추가 삭제
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String pid = (String) data.get("group_post_id");
                            String email = sessionManager.getEmail();


                            HashMap<String, String> query = new HashMap<String, String>();
                            query.put("group_post_id", pid);
                            query.put("email", email);
                            final JSONObject result = AndroidDBManager.groupPostItAdd(query);
                            Log.d("test", result.toJSONString());

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    dialog.dismiss(); //진행 dialog 소멸

                                    if (result != null) {
                                        if (!(Boolean) result.get("error")) {
                                            if ((Boolean) result.get("success")) {
                                                if ((Boolean) result.get("postit")) {
                                                    //포스트잍되있는 상태라면
                                                    viewHolder.imageViewPostIt.setImageResource(R.drawable.ic_visible_postit_converted);

                                                } else {
                                                    //invisible
                                                    viewHolder.imageViewPostIt.setImageResource(R.drawable.ic_invisible_postit_converted);
                                                }
                                            } else {
                                                Toast.makeText(activity, "포스트잇 업데이트에 성공하지 못했습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(activity, "에러가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(activity, "네트워크를 확인하세요.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        }
                    }).start();


                }
            });

            if (data.get("comment_count") != null) {

                if ((long) data.get("comment_count") == 0) {
                    viewHolder.mLinearLayoutNewsfeedCommentLayout.setVisibility(View.GONE);
                } else {
                    viewHolder.mTextViewCount.setText((long) data.get("comment_count") + "");
                }
            }


            viewHolder.mLinearLayoutNewsfeed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, CommentActivity.class);
                    intent.putExtra("reg_date", (long) data.get("reg_date"));
                    //작성시간
                    intent.putExtra("user_name", data.get("user_name").toString());
                    //본인이름
                    intent.putExtra("post_content", data.get("post_content").toString());
                    //작성내용
                    intent.putExtra("writer_image", data.get("writer_image").toString());
                    //작성자
                    intent.putExtra("reg_user_email", data.get("reg_user").toString());
                    //작성자 이름
                    intent.putExtra("group_post_id", data.get("group_post_id").toString());
                    //그룹포스트아이디

                    /* 댓글이 0일 때 commentActivity에서
                        if (data.get("comment_count") == 0) {
                        intent.putExtra("comment_count_null",false);
                    }*/


                    activity.startActivity(intent);
                }
            });

        } else if (holder instanceof ViewHolderHeader) {
            final ViewHolderHeader viewHolder = (ViewHolderHeader) holder;
            //   final JSONObject data = (JSONObject) datas.get(datas.size() - position -1);
            sessionManager = AndroidSessionManager.getInstance();
            new Thread() {
                @Override
                public void run() {
                    final Bitmap myBitmap = ImageDownloadManager.downloadImage(sessionManager.getImgUrl());
                    if (myBitmap != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                viewHolder.mImageViewMyThumbs.setImageBitmap(myBitmap);
                            }
                        });
                    }

                }
            }.start();

            //업로드 버튼
            viewHolder.mButtonUpload.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v) {
                    if ((viewHolder.mEditTextContents.getText().toString()).equals("")) {
                        Toast.makeText(activity, "내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HashMap<String, String> query = new HashMap<String, String>();
                            String contents = viewHolder.mEditTextContents.getText().toString();

                            query.put("content", contents);
                            query.put("gid", gid);
                            query.put("email", AndroidSessionManager.getInstance().getEmail());
                            AndroidDBManager.groupPostWrite(query);
                            ((NewsfeedFragment) fragment).startLoadNewsFeedThread();
                        }
                    }).start();
                }
            });


        }


    }


    // inner class to hold a reference to each item of RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView mImageViewThumbs;
        public TextView mTextViewName;
        public TextView mTextViewTime;
        public TextView mTextViewContent;
        public TextView mTextViewCount;
        public LinearLayout mLinearLayoutNewsfeed;
        public LinearLayout mLinearLayoutNewsfeedCommentLayout;
        public ImageView imageViewPostIt;
        public FrameLayout totalLayout;


        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            mImageViewThumbs = (CircleImageView) itemLayoutView.findViewById(R.id.NewsfeedCircleImageView);
            mTextViewName = (TextView) itemLayoutView.findViewById(R.id.textViewNewsfeedName);
            mTextViewTime = (TextView) itemLayoutView.findViewById(R.id.textViewNewsfeedTime);
            mTextViewContent = (TextView) itemLayoutView.findViewById(R.id.textViewNewsfeedContent);
            mTextViewCount = (TextView) itemLayoutView.findViewById(R.id.commentCount);
            mLinearLayoutNewsfeed = (LinearLayout) itemView.findViewById(R.id.LinearLayoutNewsfeeds);
            mLinearLayoutNewsfeedCommentLayout = (LinearLayout) itemView.findViewById(R.id.newsfeedCommentLayout);
            imageViewPostIt = (ImageView) itemView.findViewById(R.id.img_post_it);
            totalLayout = (FrameLayout) itemView.findViewById(R.id.totalLayout);
        }
    }

    private static class ViewHolderHeader extends RecyclerView.ViewHolder {
        //public Button mButtonUpload;
        public EditText mEditTextContents;
        public CircleImageView mImageViewMyThumbs;
        public ImageView mButtonUpload;


        public ViewHolderHeader(View itemView) {
            super(itemView);
            //mButtonUpload = (Button) itemView.findViewById(R.id.buttonUploadNewsfeed);
            mButtonUpload = (ImageView)itemView.findViewById(R.id.buttonUploadNewsfeed);
            mEditTextContents = (EditText) itemView.findViewById(R.id.editTextUploadContent);
            mImageViewMyThumbs = (CircleImageView) itemView.findViewById(R.id.newsfeedHeaderCircleImageView);
        }


    }


    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return datas.size() + 1;
    }


    public JSONArray getData() {
        return datas;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }


}
