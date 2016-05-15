package com.yeonjukko.teamplebox.libs;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.simple.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yeonju on 2015-09-19.
 */

public class AndroidSessionManager extends JSONObject {
    private static AndroidSessionManager instance;
    private AndroidSessionManager() {
    }

    public static AndroidSessionManager getInstance() {
        return instance;
    }

    public static void setSession(HashMap<String, Object> session, Activity activity) {

        instance = new AndroidSessionManager();


        for (String tmp : session.keySet()) {
            instance.put(tmp, session.get(tmp));
            Log.d("session", session.get(tmp).toString());
        }

        SharedPreferences mSPef;
        SharedPreferences.Editor mSPefEditor;

        mSPef = activity.getSharedPreferences("email", Context.MODE_PRIVATE);
        mSPefEditor = mSPef.edit();

        mSPefEditor.putString("email", instance.getEmail());
        mSPefEditor.commit();
        Log.d("session", "setsesson ok");

    }

    public static void deleteSession(Activity activity) {
        SharedPreferences mSPef;
        SharedPreferences.Editor mSPefEditor;
        mSPef = activity.getSharedPreferences("login", Context.MODE_PRIVATE);
        mSPefEditor = mSPef.edit();
        mSPefEditor.clear();
        mSPefEditor.commit();

        instance = null;
    }

    public String getName() {
        if (get("name") != null) {
            return (String) get("name");
        } else {
            return null;
        }
    }


    public String getImgUrl() {
        if (get("image_url") != null) {
            return (String) get("image_url");
        } else {
            return null;
        }
    }

    public String getEmail() {
        if (get("email") != null) {
            return (String) get("email");
        } else {
            return null;
        }
    }

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    //업데이트 시간 표시하기
    public static String countUpdateTime(long reg_date) {
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(reg_date);
        Calendar ctoday = Calendar.getInstance();
        if (cdate.get(Calendar.YEAR) == ctoday.get(Calendar.YEAR)) {
            if (cdate.get(Calendar.MONTH) == ctoday.get(Calendar.MONTH)) {
                if (cdate.get(Calendar.DATE) == ctoday.get(Calendar.DATE)) {
                    if (cdate.get(Calendar.HOUR_OF_DAY) == ctoday.get(Calendar.HOUR_OF_DAY)) {
                        int min = ctoday.get(Calendar.MINUTE) - cdate.get(Calendar.MINUTE);
                        String regdate = min + "m";
                        return regdate;
                    } else {
                        int hour = ctoday.get(Calendar.HOUR_OF_DAY) - cdate.get(Calendar.HOUR_OF_DAY);
                        String regdate = hour + "h";
                        return regdate;
                    }
                }
            }
        }
        int year = cdate.get(Calendar.YEAR);
        int month = cdate.get(Calendar.MONTH);
        int date = cdate.get(Calendar.DATE);
        String regdate = year + "." + month + "." + date;
        return regdate;

    }

}
