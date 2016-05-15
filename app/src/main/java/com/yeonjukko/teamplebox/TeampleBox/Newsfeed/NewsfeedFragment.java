package com.yeonjukko.teamplebox.TeampleBox.Newsfeed;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.adapter.RecycleNewsfeedAdapter;
import com.yeonjukko.teamplebox.libs.AndroidDBManager;
import com.yeonjukko.teamplebox.libs.AndroidSessionManager;

import org.json.simple.JSONArray;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewsfeedFragment extends Fragment{
    AndroidSessionManager sessionManager;
    RecyclerView recyclerView;
    CircleImageView mImageViewThumbs;
    SwipeRefreshLayout swipeRefreshLayout;
    FloatingActionButton btAddMember;
    String gid;
    String gname;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_newsfeed,container,false);
        sessionManager = AndroidSessionManager.getInstance();
        if (sessionManager == null) {
            Toast.makeText(getActivity(), "잘못된 접근입니다.", Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return rootView;
        }
        //Intent from MyActivity
        Intent intent = getActivity().getIntent();
        gid = intent.getStringExtra("gid");
        gname = intent.getStringExtra("gname");


        /*Intent intent2Calendar = new Intent(this, CalendarFragment.class);
        intent2Calendar.putExtra("gid",gid);
        startActivity(intent2Calendar);*/

        recyclerView = (RecyclerView) rootView.findViewById(R.id.listViewNewsfeed);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mImageViewThumbs = (CircleImageView) rootView.findViewById(R.id.NewsfeedCircleImageView);
        btAddMember = (FloatingActionButton) rootView.findViewById(R.id.floatingButtonNewsfeed);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.activity_newsfeed_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startLoadNewsFeedThread();
            }
        });


        startLoadNewsFeedThread();

        btAddMember.setOnClickListener(btAddMemberListener);
        return rootView;
    }



    private View.OnClickListener btAddMemberListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent msg = new Intent(Intent.ACTION_SEND);
            String url = "http://somalunak.cafe24.com:7533/teamplebox/inviteRedirect.jsp?gid=" + gid;
            //url에 gid를 추가하여 초대url 생성

            msg.addCategory(Intent.CATEGORY_DEFAULT);
            msg.putExtra(Intent.EXTRA_SUBJECT, "팀플박스 공유");
            //message의 타이틀 추가
            msg.putExtra(Intent.EXTRA_TEXT, AndroidSessionManager.getInstance().getName()
                    + "님이 " + gname + "그룹에 초대하셨습니다.\n"
                    + "url: " + url + "\n" + "위 url로 접속하셔서 팀플박스에 참가하세요~!");
            //intent에 담아보낼 message를 추가
            msg.setType("text/plain");
            startActivity(Intent.createChooser(msg, "초대하기"));
        }
    };

    public void startLoadNewsFeedThread() {
        new Thread() {
            @Override
            public void run() {

                HashMap<String, String> query = new HashMap<>();


                query.put("gid", gid);
                query.put("email",AndroidSessionManager.getInstance().getEmail());
                //Log.d("test", gid);

                final JSONArray result = AndroidDBManager.groupPostRead(query);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        RecycleNewsfeedAdapter adapter = new RecycleNewsfeedAdapter(result, getActivity(), gid,NewsfeedFragment.this);
                        recyclerView.setAdapter(adapter);
                        swipeRefreshLayout.setRefreshing(false);

                    }
                });

            }
        }.start();
    }


}
