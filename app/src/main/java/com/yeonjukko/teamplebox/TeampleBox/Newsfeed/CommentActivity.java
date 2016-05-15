package com.yeonjukko.teamplebox.TeampleBox.Newsfeed;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.libs.AndroidDBManager;
import com.yeonjukko.teamplebox.libs.AndroidSessionManager;
import com.yeonjukko.teamplebox.libs.ImageDownloadManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {
    private AndroidSessionManager sessionManager;

    private String group_post_id;


    private CircleImageView mWriterCommentCircleImageView;
    private TextView mtextViewWriterCommentName;
    private TextView mtextViewWriterCommentTime;
    private TextView mtextViewWriterCommentContent;
    private TextView mtextViewWriterCommentEmail;
    private TextView mTextViewCount;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText mEditTextCommentWrite;
    private Button mButtonCommentWrite;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        sessionManager = AndroidSessionManager.getInstance();
        if (sessionManager == null) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show();
            return;
            //finish();
        }
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_newsfeed_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startLoadingComments();
            }

        });

        setLayout();
        startLoadingComments();


    }

    private void onClickCommentSend() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (mEditTextCommentWrite.getText().toString().equals("")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CommentActivity.this, "댓글을 입력하세요.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                } else {
                    HashMap<String, String> query = new HashMap<>();
                    query.put("group_post_id", group_post_id);
                    query.put("email", AndroidSessionManager.getInstance().getEmail());
                    query.put("content", mEditTextCommentWrite.getText().toString());
                    final JSONObject result = AndroidDBManager.groupCommentWrite(query);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result == null) {
                                //인터넷 연결을 끊겼을때 처리
                                Toast.makeText(CommentActivity.this, "인터넷 연결이 끊겼습니다", Toast.LENGTH_SHORT).show();
                            } else {

                                if (!(Boolean) result.get("error")) {
                                    if ((Boolean) result.get("success")) {

                                        Toast.makeText(CommentActivity.this, "댓글이 정상적으로 저장되었습니다", Toast.LENGTH_SHORT).show();
                                        mEditTextCommentWrite.setText("");
                                        startLoadingComments();

                                    } else {
                                        Toast.makeText(CommentActivity.this, "댓글을 작성하지 못하였습니다", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(CommentActivity.this, "오류가 발생하였습니다", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }


            }
        }).start();
    }

    private void setLayout() {
        //swipe내의 레이아웃
        mWriterCommentCircleImageView = (CircleImageView) findViewById(R.id.mWriterCommentCircleImageView);
        mtextViewWriterCommentName = (TextView) findViewById(R.id.mtextViewWriterCommentName);
        mtextViewWriterCommentTime = (TextView) findViewById(R.id.mtextViewWriterCommentTime);
        mtextViewWriterCommentContent = (TextView) findViewById(R.id.mtextViewWriterCommentContent);
        mtextViewWriterCommentEmail = (TextView) findViewById(R.id.mtextViewWriterCommentEmail);
        mTextViewCount = (TextView) findViewById(R.id.commentCount);

        //댓글 레이아웃
        mButtonCommentWrite = (Button) findViewById(R.id.mButtonWriteComment);
        mEditTextCommentWrite = (EditText) findViewById(R.id.mEditTextWriteComment);


        //Intent from RecycleNewsfeedAdapter
        Intent intent = getIntent();
        group_post_id = intent.getStringExtra("group_post_id");


        //댓글작성 버튼을 눌렀을 때
        mButtonCommentWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCommentSend();
            }
        });


    }

    private void startLoadingComments() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> query = new HashMap<String, String>();
                query.put("group_post_id", group_post_id);
                final JSONObject result = AndroidDBManager.groupCommentRead(query);
                Log.d("postid", group_post_id);

                final JSONArray resultContent = (JSONArray) result.get("contents");
                final long reg_date = (long) result.get("reg_date");
                final String user_name = (String) result.get("user_name");
                final String reg_user_email = (String) result.get("reg_user_email");
                final String post_content = (String) result.get("post_content");
                final String writer_image = (String) result.get("writer_image");


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mtextViewWriterCommentName.setText(user_name);
                        mtextViewWriterCommentTime.setText(AndroidSessionManager.countUpdateTime(reg_date));
                        mtextViewWriterCommentContent.setText(post_content);
                        mtextViewWriterCommentEmail.setText(reg_user_email);
                        new Thread() {
                            @Override
                            public void run() {
                                final Bitmap myBitmap = ImageDownloadManager.downloadImage(writer_image);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mWriterCommentCircleImageView.setImageBitmap(myBitmap);
                                    }
                                });
                            }
                        }.start();


                        LinearLayout commentLayout = (LinearLayout) findViewById(R.id.commentLayout);
                        commentLayout.removeAllViews();
                        //뷰를 불러오기전에 레이아웃을 비워줌
                        for (int i = 0; i < resultContent.size(); i++) {

                            final JSONObject data = (JSONObject) resultContent.get(i);

                            View view = getLayoutInflater().inflate(R.layout.activity_comment_adapter, null);
                            commentLayout.addView(view);
                            //불러온 뷰를 추가

                            final CircleImageView mCircleImageView = (CircleImageView) view.findViewById(R.id.mCircleImageView);
                            TextView mtextViewName = (TextView) view.findViewById(R.id.mtextViewName);
                            TextView mtextViewComment = (TextView) view.findViewById(R.id.mtextViewComment);
                            TextView mtextViewCommentTime = (TextView) view.findViewById(R.id.mtextViewCommentTime);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    final Bitmap myBitmap = ImageDownloadManager.downloadImage((String) data.get("image_url"));
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mCircleImageView.setImageBitmap(myBitmap);
                                        }
                                    });

                                }
                            }).start();

                            mtextViewName.setText((String) data.get("user_name"));
                            mtextViewComment.setText((String) data.get("comment_content"));
                            mtextViewCommentTime.setText(AndroidSessionManager.countUpdateTime((long) data.get("reg_date")));
                            //String pid = (String)data.get("group_post_comment_id");
                            //코멘트 지울때

                        }

                        mTextViewCount.setText(resultContent.size() + "");

                        swipeRefreshLayout.setRefreshing(false);


                    }
                });


            }
        }
        ).start();
    }


}
