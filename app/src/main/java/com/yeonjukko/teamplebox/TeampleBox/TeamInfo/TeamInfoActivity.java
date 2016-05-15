package com.yeonjukko.teamplebox.TeampleBox.TeamInfo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.adapter.RecycleNewsfeedAdapter;
import com.yeonjukko.teamplebox.adapter.RecycleTeamInfoListAdapter;
import com.yeonjukko.teamplebox.libs.AndroidDBManager;
import com.yeonjukko.teamplebox.libs.ImageDownloadManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class TeamInfoActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    CollapsingToolbarLayout collapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        final ImageView header = (ImageView) findViewById(R.id.header);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.listView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(TeamInfoActivity.this, 2, GridLayoutManager.VERTICAL, false);

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0)
                    return 2;
                else
                    return 1;
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                HashMap<String, String> query = new HashMap<String, String>();
                query.put("group_id", intent.getStringExtra("gid"));

                final JSONObject result = AndroidDBManager.groupMemberList(query);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) {
                            Toast.makeText(TeamInfoActivity.this, "네트워크를 확인하세요", Toast.LENGTH_SHORT).show();
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (result == null) {
                                        //인터넷 연결을 끊겼을때 처리
                                        Toast.makeText(TeamInfoActivity.this, "인터넷 연결이 끊겼습니다", Toast.LENGTH_SHORT).show();
                                    } else {

                                        if (!(Boolean) result.get("error")) {
                                            if ((Boolean) result.get("success")) {

                                                Toast.makeText(TeamInfoActivity.this, "정상적으로 로딩되었습니다", Toast.LENGTH_SHORT).show();
                                                JSONArray datas = (JSONArray) result.get("contents");

                                                RecycleTeamInfoListAdapter adapter = new RecycleTeamInfoListAdapter(datas, TeamInfoActivity.this);
                                                recyclerView.setAdapter(adapter);
                                                recyclerView.setHasFixedSize(true);

                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        final Bitmap myBitmap = ImageDownloadManager.downloadImage((String)result.get("group_image"));
                                                        Log.d("test",(String)result.get("group_image"));
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                header.setImageBitmap(myBitmap);

                                                            }
                                                        });

                                                    }
                                                }).start();

                                                String gname = (String) result.get("group_name");
                                                collapsingToolbar.setTitle(gname+" 팀정보");


                                            } else {
                                                Toast.makeText(TeamInfoActivity.this, "댓글을 작성하지 못하였습니다", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(TeamInfoActivity.this, "오류가 발생하였습니다", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                        }
                    }
                });

            }
        }).start();

    }
}
