package com.yeonjukko.teamplebox.TeampleBox.Attendance;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.adapter.RecycleAttendanceListAdapter;
import com.yeonjukko.teamplebox.libs.AndroidDBManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;

import at.grabner.circleprogress.TextMode;

/**
 * Created by yeonjukko on 15. 11. 1..
 */

public class page_2 extends android.support.v4.app.Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_attendance_2, container, false);
        final RecyclerView groupListView = (RecyclerView) linearLayout.findViewById(R.id.recyclerView);
        groupListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> query = new HashMap<>();
                String gid = getActivity().getIntent().getStringExtra("gid");
                query.put("group_id", gid);
                final JSONObject result = AndroidDBManager.groupAttendanceReadNow(query);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) {
                            Toast.makeText(getActivity(), "네트워크를 확인하세요.", Toast.LENGTH_SHORT).show();

                        } else {
                            if (!(Boolean) result.get("error")) {
                                if ((Boolean) result.get("success")) {
                                    if ((Boolean) result.get("isAttendance")) {
                                        //출석체크할 일정이 있을 때
                                        JSONArray datas = (JSONArray) result.get("contents");
                                        RecycleAttendanceListAdapter adapter = new RecycleAttendanceListAdapter(datas, getActivity(), page_2.this);
                                        groupListView.setAdapter(adapter);
                                    } else {
                                        //출석없을때

                                    }


                                } else {
                                    Toast.makeText(getActivity(), "출석 로딩에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getActivity(), "에러가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

            }
        }).start();

        return linearLayout;
    }
}