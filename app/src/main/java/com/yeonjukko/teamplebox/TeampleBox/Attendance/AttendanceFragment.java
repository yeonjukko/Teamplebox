package com.yeonjukko.teamplebox.TeampleBox.Attendance;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.adapter.RecycleAttendanceListAdapter;
import com.yeonjukko.teamplebox.libs.AndroidDBManager;
import com.yeonjukko.teamplebox.libs.AndroidSessionManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class AttendanceFragment extends android.support.v4.app.Fragment {
    AndroidSessionManager sessionManager;
    int MAX_PAGE = 2;
    Fragment cur_fragment = new Fragment();
    Adapter adapter;
    View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_attendance, container, false);
            sessionManager = AndroidSessionManager.getInstance();
            if (sessionManager == null) {
                Toast.makeText(getActivity(), "잘못된 접근입니다.", Toast.LENGTH_SHORT).show();
                return rootView;
            }

            Log.d("test", "Attendance onCreateView");
            ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
            if (adapter == null) {
                adapter = new Adapter(getActivity().getSupportFragmentManager());
                viewPager.setAdapter(adapter);
            }
        }
        return rootView;
    }

    private class Adapter extends FragmentPagerAdapter {
        public Adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position < 0 || MAX_PAGE <= position) {

                return null;
            }
            switch (position) {
                case 0:
                    cur_fragment = new page_1();
                    Log.d("test", position + "attendanceFragment");
                    break;
                case 1:
                    cur_fragment = new page_2();
                    break;

            }
            return cur_fragment;
        }

        @Override
        public int getCount() {
            return MAX_PAGE;
        }
    }


}
