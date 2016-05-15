package com.yeonjukko.teamplebox.TeampleBox.Attendance;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.adapter.RecycleAttendanceListAdapter;
import com.yeonjukko.teamplebox.libs.AndroidDBManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;

/**
 * Created by yeonjukko on 15. 11. 1..
 */
public class page_1 extends Fragment implements CircleProgressView.OnProgressChangedListener {

    private static final int FLAG_STOP_ALARM_ID = 357358;
    SharedPreferences mSPref;
    SharedPreferences.Editor mSPrefEditor;
    Boolean mShowUnit = true;
    String serviceName = "com.yeonjukko.teamplebox.TeampleBox.Attendance.AttendanceService";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_attendance_1, container, false);
        final TextView meetName = (TextView) linearLayout.findViewById(R.id.tv_reg_name);
        final TextView startTime = (TextView) linearLayout.findViewById(R.id.tv_start_time);
        final TextView stopTime = (TextView) linearLayout.findViewById(R.id.tv_stop_time);
        final TextView location = (TextView) linearLayout.findViewById(R.id.tv_addr);
        final Button stopService = (Button) linearLayout.findViewById(R.id.stop_service);
        // final Button startAttendService = (Button) linearLayout.findViewById(R.id.start_attend);
        final CircleProgressView circleView = (CircleProgressView) linearLayout.findViewById(R.id.circleView);
        circleView.setOnProgressChangedListener(this);

        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AttendanceStopService.class);
                getActivity().startService(intent);
            }
        });


        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> query = new HashMap<>();
                final String gid = getActivity().getIntent().getStringExtra("gid");
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


                                        //startAttendService.setEnabled(true);
                                        meetName.setText((String) result.get("title"));
                                        SimpleDateFormat firstFormat = new SimpleDateFormat("MM월 dd일 HH:mm:ss");
                                        final SimpleDateFormat secondFormat = new SimpleDateFormat("HH:mm");
                                        startTime.setText(firstFormat.format((long) result.get("start_date")));
                                        stopTime.setText(secondFormat.format((long) result.get("end_date")));
                                        location.setText((String) result.get("location_name"));
                                        final double latitude = (double) result.get("latitude");
                                        final double longitude = (double) result.get("longitude");
                                        final String calendarId = (String) result.get("calendar_id");

                                        circleView.setSeekModeEnabled(true);
                                        circleView.setShowTextWhileSpinning(true);
                                        circleView.setOnTouchListener(new View.OnTouchListener() {

                                            @Override
                                            public boolean onTouch(View v, MotionEvent event) {
                                                //startAttendService.setVisibility(View.GONE);
                                                if (event.getAction() == MotionEvent.ACTION_UP) {
                                                    if (!isServiceRunning(serviceName)) {
                                                        mSPref = getActivity().getSharedPreferences("attendance", Context.MODE_PRIVATE);
                                                        mSPrefEditor = mSPref.edit();
                                                        mSPrefEditor.putString("latitude", latitude + "");
                                                        mSPrefEditor.putString("longitude", longitude + "");
                                                        mSPrefEditor.putString("calendar_id", calendarId);
                                                        mSPrefEditor.putString("group_id",gid);
                                                        mSPrefEditor.commit();

                                                        Intent intent = new Intent(getActivity(), AttendanceService.class);
                                                        getActivity().startService(intent);

                                                        Intent intent2 = new Intent(getActivity(), AttendanceStopService.class);
                                                        AlarmManager stopAlarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                                                        //Log.d("test", secondFormat.format((long) result.get("end_date")) + "테스트");

                                                        PendingIntent sender = PendingIntent.getService(getActivity(), FLAG_STOP_ALARM_ID, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
                                                        Log.d("test", (long) result.get("end_date") + "");
                                                        stopAlarm.set(AlarmManager.RTC_WAKEUP, (long) result.get("end_date"), sender);

                                                    }

                                                    circleView.spin();
                                                    circleView.stopSpinning();
                                                    circleView.setAutoTextSize(true);
                                                    circleView.setTextMode(TextMode.PERCENT);
                                                    circleView.setValueAnimated(calculatePercent((long) result.get("start_date"), (long) result.get("end_date")));
                                                    Log.d("test", calculatePercent((long) result.get("start_date"), (long) result.get("end_date")) + "");

                                                }
                                                return true;

                                            }
                                        });


                                    } else {
                                        //출석없을때
                                        circleView.setText("표시할 출석체크가 없습니다.");
                                        circleView.setAutoTextSize(true);
                                        circleView.setTextMode(TextMode.TEXT);
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

    public float calculatePercent(long start, long stop) {
        long curr = System.currentTimeMillis();
        return ((curr - start) * 100 / (stop - start));

    }

    @Override
    public void onProgressChanged(float value) {
    }

    public Boolean isServiceRunning(String serviceName) {
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(getActivity().ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {

            if (serviceName.equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

