package com.yeonjukko.teamplebox.libs;

/**
 * Created by yeonju on 2015-09-19.
 */

import android.net.Uri;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by MoonJR on 2015. 9. 3..
 */
public class AndroidDBManager {

    public static final String DEFAULT_URL = "http://somalunak.cafe24.com:7533/teamplebox/";

    public static JSONObject login(HashMap<String, String> query) {
        return (JSONObject) connectDB("login.jsp", query);
    }

    public static JSONObject signUp(HashMap<String, String> query) {
        return (JSONObject) connectDB("signup.jsp", query);
    }

    public static JSONObject inviteGroup(HashMap<String, String> query) {
        return (JSONObject) connectDB("inviteGroup.jsp", query);
    }

    public static JSONObject makeGroup(HashMap<String, String> query) {
        return (JSONObject) connectDB("makeGroup.jsp", query);
    }

    public static JSONArray searchGroup(HashMap<String, String> query) {
        return (JSONArray) connectDB("searchGroup.jsp", query);
    }

    public static JSONObject deleteGroup(HashMap<String, String> query) {
        return (JSONObject) connectDB("deleteGroup.jsp", query);
    }

    public static JSONArray groupPostRead(HashMap<String, String> query) {
        try {
            JSONObject obj = (JSONObject) connectDB("groupPostRead.jsp", query);
            //Log.d("test", obj.toJSONString());
            return (JSONArray) obj.get("contents");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject groupPostWrite(HashMap<String, String> query) {
        return (JSONObject) connectDB("groupPostWrite.jsp", query);
    }

    public static JSONObject groupCommentRead(HashMap<String, String> query) {
        return (JSONObject) connectDB("groupCommentRead.jsp", query);
    }

    public static JSONObject groupCommentWrite(HashMap<String, String> query) {
        return (JSONObject) connectDB("groupCommentWrite.jsp", query);
    }

    public static JSONObject groupCalendarAdd(HashMap<String, String> query) {
        return (JSONObject) connectDB("groupCalendarAdd.jsp", query);
    }

    public static JSONObject groupCalendarRead(HashMap<String, String> query) {
        return (JSONObject) connectDB("groupCalendarRead.jsp", query);
    }

    public static JSONObject groupAttendance(HashMap<String, String> query) {
        return (JSONObject) connectDB("groupAttendance.jsp", query);
    }

    public static JSONObject groupAttendanceReadNow(HashMap<String, String> query) {
        return (JSONObject) connectDB("groupAttendanceReadNow.jsp", query);
    }

    public static JSONObject groupPostItAdd(HashMap<String, String> query) {
        return (JSONObject) connectDB("groupPostItAdd.jsp", query);
    }

    public static JSONObject groupMemberList(HashMap<String, String> query) {
        return (JSONObject) connectDB("groupMemberList.jsp", query);
    }



    private static Object connectDB(String flag, HashMap<String, String> query) {
        try {
            URL url = new URL(makeUrl(flag));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.connect();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            writer.write(makeQuery(query));
            writer.close();

            return JSONValue.parse(new InputStreamReader(connection.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String makeUrl(String flag) {
        return DEFAULT_URL + flag;
    }


    private static String makeQuery(HashMap<String, String> query) {
        StringBuffer buffer = new StringBuffer();
        for (String key : query.keySet()) {
            buffer.append(key);
            buffer.append('=');
            buffer.append(Uri.encode(query.get(key)));
            buffer.append('&');
        }

        buffer.append("key=yeonjukko");


        return buffer.toString();

    }


}