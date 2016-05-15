package com.yeonjukko.teamplebox.TeampleBox.Calendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;
import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.libs.AndroidDBManager;
import com.yeonjukko.teamplebox.libs.AndroidSessionManager;
import com.yeonjukko.teamplebox.libs.AndroidUrlManager;
import com.yeonjukko.teamplebox.libs.ImageDownloadManager;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import de.hdodenhof.circleimageview.CircleImageView;

public class CalendarFragment extends Fragment {
    private static final int MAP_CODE = 3;
    MaterialCalendarView widget;
    private static final DateFormat FORMATTER = SimpleDateFormat.getDateInstance();
    AndroidSessionManager sessionManager;
    Date startTime = new Date(System.currentTimeMillis());
    Date stopTime = new Date(System.currentTimeMillis() + 1000 * 60 * 60);

    JSONArray datas;
    double latitude;
    double longitude;
    String location;
    ListView listViewCalendar;
    EventDecorator eventDecorator;
    ArrayAdapter<String> listAdapter;
    String gid;
    TextView footerT;
    Button onLoadMapButton;
    EditText myLocation;

    CheckBox checkBoxAttend;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_calendar, container, false);
        sessionManager = AndroidSessionManager.getInstance();
        if (sessionManager == null) {
            Toast.makeText(getActivity(), "잘못된 접근입니다.", Toast.LENGTH_SHORT).show();
            return rootView;
        }

        //캘린더 셋팅
        widget = (MaterialCalendarView) rootView.findViewById(R.id.calendarView);
        widget.setArrowColor(getResources().getColor(R.color.pink));
        widget.setOnDateChangedListener(onClickDate);


        //세부 일정 리스트 셋팅
        listViewCalendar = (ListView) rootView.findViewById(R.id.listViewCalendar);
        listAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        listViewCalendar.removeFooterView(listViewCalendar);
        footerT = new TextView(getActivity());
        footerT.setText("일정 추가");
        footerT.setGravity(Gravity.CENTER_HORIZONTAL);
        footerT.setPadding(10, 10, 10, 10);
        listViewCalendar.addFooterView(footerT);

        gid = getActivity().getIntent().getStringExtra("gid");
        startCalendarContentThread();
        HashMap<String, String> query = new HashMap<>();


        return rootView;
    }

    private void startCalendarContentThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> query = new HashMap<String, String>();
                query.put("group_id", gid);
                final JSONObject result = AndroidDBManager.groupCalendarRead(query);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result != null) {
                            if (!(Boolean) result.get("error")) {
                                if ((Boolean) result.get("success")) {
                                    datas = (JSONArray) result.get("contents");

                                    ArrayList<CalendarDay> calendarC = new ArrayList<CalendarDay>();

                                    for (int i = 0; i < datas.size(); i++) {
                                        JSONObject data = (JSONObject) datas.get(i);
                                        String title = (String) data.get("title");
                                        Double longitude = (Double) data.get("longitude");
                                        Double latitude = (Double) data.get("latitude");
                                        String regName = (String) data.get("reg_name");
                                        Date regDate = new Date((long) data.get("reg_date"));
                                        Date startDate = new Date((long) data.get("start_date"));
                                        Date endDate = new Date((long) data.get("end_date"));
                                        String regImg = (String) data.get("user_image");

                                        CalendarDay startDateC = CalendarDay.from(startDate);
                                        calendarC.add(startDateC);

                                    }
                                    //캘린더에 일정 추가 eventDecorator에 이벤트 저장
                                    eventDecorator = new EventDecorator(getResources().getColor(R.color.pink), calendarC);
                                    widget.addDecorator(eventDecorator);
                                    CalendarDay selectedDate = widget.getSelectedDate();
                                    if (selectedDate != null) {
                                        //make버튼으로 업데이트 했을 때만 리스트 업데이트
                                        startListUpdates(selectedDate);
                                    }


                                } else {
                                    Toast.makeText(getActivity(), "캘린더 로드를 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(getActivity(), "에러가 발생했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "네트워크를 확인하세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void startListUpdates(final CalendarDay selectedDate) {
        //계속 리스트에 추가 방
        listAdapter.clear();

        final JSONArray tmpArray = new JSONArray();
        for (int i = 0; i < datas.size(); i++) {

            JSONObject data = (JSONObject) datas.get(i);
            CalendarDay startDateC = CalendarDay.from(new Date((long) data.get("start_date")));

            if (selectedDate.equals(startDateC)) {
                tmpArray.add(data);
                String title = (String) data.get("title");

                listAdapter.add(title);


            }

        }
        footerT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertAddCalendar();
            }
        });
        listViewCalendar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                View detailView = getActivity().getLayoutInflater().inflate(R.layout.custom_dialog_calendar_details, null, false);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(detailView);
                builder.setTitle(selectedDate.getYear() + "년" + selectedDate.getMonth() + "월" + selectedDate.getDate() + "일 일정 ");

                TextView tvTitle = (TextView) detailView.findViewById(R.id.tv_name);
                TextView tvStartTime = (TextView) detailView.findViewById(R.id.tv_start_time);
                TextView tvStopTime = (TextView) detailView.findViewById(R.id.tv_stop_time);
                TextView tvRegName = (TextView) detailView.findViewById(R.id.tv_reg_name);
                TextView tvRegDate = (TextView) detailView.findViewById(R.id.tv_reg_date);
                final CircleImageView circleImageView = (CircleImageView) detailView.findViewById(R.id.imageViewProfile);
                LinearLayout mayLayout = (LinearLayout) detailView.findViewById(R.id.mapLayout);


                JSONObject result = (JSONObject) tmpArray.get(position);
                String title = (String) result.get("title");
                Double longitude = (Double) result.get("longitude");
                Double latitude = (Double) result.get("latitude");
                String regName = (String) result.get("reg_name");
                Date regDate = new Date((long) result.get("reg_date"));
                Date startDate = new Date((long) result.get("start_date"));
                Date endDate = new Date((long) result.get("end_date"));
                final String regImg = (String) result.get("user_image");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap myBitmap = ImageDownloadManager.downloadImage(regImg);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                circleImageView.setImageBitmap(myBitmap);
                            }
                        });

                    }
                }).start();

                MapView daumMapView = new MapView(getActivity());

                daumMapView.setDaumMapApiKey(MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY);
                MapPOIItem mapPOIItem = new MapPOIItem();
                mapPOIItem.setMapPoint(MapPoint.mapPointWithGeoCoord(latitude,longitude));
                mapPOIItem.setMarkerType(MapPOIItem.MarkerType.RedPin);
                daumMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude,longitude),true);
                daumMapView.addPOIItem(mapPOIItem);
                mayLayout.addView(daumMapView);

                tvTitle.setText(title);

                SimpleDateFormat transFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm");
                tvStartTime.setText(transFormat.format(startDate));
                tvStopTime.setText(transFormat.format(endDate));
                tvRegDate.setText(transFormat.format(regDate));
                tvRegName.setText(regName);


                builder.show();
            }

        });

        listViewCalendar.setAdapter(listAdapter);


    }

    private void showAlertAddCalendar() {
        //선택한 년월일 판단
        final CalendarDay selectedDate = widget.getSelectedDate();
        final int year = selectedDate.getYear();
        final int month = selectedDate.getMonth();
        final int day = selectedDate.getDay();

        //startTime, stopTime 선택한 날짜로 년월일 초기화
        startTime.setYear(year - 1900);
        startTime.setMonth(month);
        startTime.setDate(day);


        stopTime.setYear(year - 1900);
        stopTime.setMonth(month);
        stopTime.setDate(day);

        //현재시간판단
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String minuteTime = "" + minute;
        if (minute < 10) {
            minuteTime = "0" + minute;
        }
        //요일 판단
        String dayOfWeekend = "";
        int dayOfWeek = selectedDate.getCalendar().get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek == 1) {
            dayOfWeekend = "(일)";
        } else if (dayOfWeek == 2) {
            dayOfWeekend = "(월)";
        } else if (dayOfWeek == 3) {
            dayOfWeekend = "(화)";
        } else if (dayOfWeek == 4) {
            dayOfWeekend = "(수)";
        } else if (dayOfWeek == 5) {
            dayOfWeekend = "(목)";
        } else if (dayOfWeek == 6) {
            dayOfWeekend = "(금)";
        } else if (dayOfWeek == 7) {
            dayOfWeekend = "(토)";
        }


        //오전오후판단

        final String apm;
        if (calendar.get(Calendar.AM_PM) == 0) {
            apm = "오전";
        } else {
            apm = "오후";
            hour -= 12;
        }

        String dayStr = year + "년 " + (month + 1) + "월 " + day + "일 " + dayOfWeekend;

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_dialog_meeting_add, null);


        //AlertDialog dialog = new AlertDialog();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(dayStr + " 일정 추가");
        builder.setView(view);
        final Button startDateButton = (Button) view.findViewById(R.id.btn_start_date);
        final Button startTimeButton = (Button) view.findViewById(R.id.btn_start_time);
        final Button stopDateButton = (Button) view.findViewById(R.id.btn_stop_date);
        final Button stopTimeButton = (Button) view.findViewById(R.id.btn_stop_time);
        final EditText editTextTitle = (EditText) view.findViewById(R.id.editTextMeetName);
        myLocation = (EditText) view.findViewById(R.id.myLocation);
        final Button makeMeetButton = (Button) view.findViewById(R.id.btn_meet_make);
        onLoadMapButton = (Button) view.findViewById(R.id.findLocation);
        checkBoxAttend = (CheckBox) view.findViewById(R.id.isAttendance);

        startDateButton.setText(dayStr);
        stopDateButton.setText(dayStr);
        startTimeButton.setText(apm + " " + hour + ":" + minuteTime);
        stopTimeButton.setText(apm + " " + (hour + 1) + ":" + minuteTime);


        startTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final TimePicker picker = new TimePicker(getActivity());
                builder.setTitle("시작 시간 설정");
                builder.setView(picker);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        String apm;
                        int hour = picker.getCurrentHour();
                        int min = picker.getCurrentMinute();


                        String minTime = "" + min;
                        if (min < 10) {
                            minTime = "0" + min;
                        }

                        String monthText = (month + 1) + "";
                        if ((month + 1) < 10) {
                            monthText = "0" + (month + 1);
                        }

                        String dayText = day + "";
                        if (day < 10) {
                            dayText = "0" + day;
                        }

                        String from = year + "-" + monthText + "-" + dayText + " " + hour + ":" + minTime + ":" + "00";
                        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            startTime = transFormat.parse(from);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (hour < 12) {
                            apm = "오전";

                        } else {
                            apm = "오후";
                            hour -= 12;
                        }


                        startTimeButton.setText(apm + " " + hour + ":" + minTime);
                        stopTimeButton.setText(apm + " " + (hour + 1) + ":" + minTime);
                    }
                });
                builder.show();

            }
        });

        stopTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final TimePicker picker = new TimePicker(getActivity());
                builder.setTitle("종료 시간 설정");
                builder.setView(picker);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        String apm;
                        int hour = picker.getCurrentHour();
                        int min = picker.getCurrentMinute();


                        String minTime = "" + min;
                        if (min < 10) {
                            minTime = "0" + min;
                        }

                        String monthText = (month + 1) + "";
                        if ((month + 1) < 10) {
                            monthText = "0" + (month + 1);
                        }

                        String dayText = day + "";
                        if (day < 10) {
                            dayText = "0" + day;
                        }

                        String from = year + "-" + monthText + "-" + dayText + " " + hour + ":" + minTime + ":" + "00";
                        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            stopTime = transFormat.parse(from);
                            Log.d("test", stopTime.toString() + "stopTime changed");
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (hour < 12) {
                            apm = "오전";

                        } else {
                            apm = "오후";
                            hour -= 12;
                        }


                        stopTimeButton.setText(apm + " " + hour + ":" + minTime);
                    }
                });
                builder.show();

            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        onLoadMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EventsDemoActivity.class);
                startActivityForResult(intent, MAP_CODE);

            }
        });


        makeMeetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextTitle.getText().toString().equals("")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            HashMap<String, String> query = new HashMap<String, String>();
                            query.put("group_id", gid);
                            query.put("content", " ");
                            query.put("title", editTextTitle.getText().toString());
                            query.put("reg_user", AndroidSessionManager.getInstance().getEmail());
                            query.put("start_date", startTime.getTime() + "");
                            query.put("end_date", stopTime.getTime() + "");
                            query.put("latitude", latitude + "");
                            query.put("longitude", longitude + "");
                            query.put("is_attendance", checkBoxAttend.isChecked() + "");
                            if (location != null) {
                                query.put("location_name", location);
                            } else {
                                query.put("location_name", myLocation.getText().toString());
                            }
                            final JSONObject result = AndroidDBManager.groupCalendarAdd(query);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (result != null) {
                                        if (!(Boolean) result.get("error")) {
                                            if ((Boolean) result.get("success")) {
                                                startCalendarContentThread();


                                                alertDialog.dismiss();

                                            } else {
                                                Toast.makeText(getActivity(), "캘린더 추가를 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                            }

                                        } else {
                                            Toast.makeText(getActivity(), "에러가 발생했습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(getActivity(), "네트워크를 확인하세요.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    }).start();

                } else {
                    Toast.makeText(getActivity(), "제목을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });


    }

    private OnDateSelectedListener onClickDate = new OnDateSelectedListener() {
        @Override
        public void onDateSelected(MaterialCalendarView widget, CalendarDay date, boolean selected) {


            if (eventDecorator.dates.contains(date)) {
                startListUpdates(date);
            } else {
                showAlertAddCalendar();
            }


        }
    };

    private String getSelectedDatesString() {
        CalendarDay date = widget.getSelectedDate();
        if (date == null) {
            return "No Selection";
        }
        return FORMATTER.format(date.getDate());
    }

    public class EventDecorator implements DayViewDecorator {

        private final int color;
        private final HashSet<CalendarDay> dates;

        public EventDecorator(int color, Collection<CalendarDay> dates) {
            this.color = color;
            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(5, color));
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                latitude = data.getDoubleExtra("latitude", 0.0);
                longitude = data.getDoubleExtra("longitude", 0.0);
                location = data.getStringExtra("location");

                myLocation.setText(location);

                Log.d("test", latitude + " " + longitude + " " + location);
            }
        }
    }
}

