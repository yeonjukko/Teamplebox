package com.yeonjukko.teamplebox.libs;

import android.net.Uri;

import java.util.HashMap;

/**
 * Created by yeonjukko on 15. 10. 31..
 */
public class AndroidUrlManager {
    String tmpurl = "&center=127.1141382,37.3599968&level=3&w=320&h=320&maptype=default&markers=127.1141382,37.3599968&key=발급키&uri=등록디렉토리";
    public static String DEFAULT_URL = "http://openapi.naver.com/map/getStaticMap?version=1.0&crs=EPSG:4326&maptype=default&level=13&w=480&h=320&";

    public static String returnUrl(HashMap<String,String> query){
        return DEFAULT_URL+makeQuery(query);
    }
    private static String makeQuery(HashMap<String, String> query) {
        StringBuffer buffer = new StringBuffer();
        for (String key : query.keySet()) {
            buffer.append(key);
            buffer.append('=');
            buffer.append(Uri.encode(query.get(key)));
            buffer.append('&');
        }

        buffer.append("key=0d97978c579a44e64ff069490f411e8e");
        buffer.append("&uri=somalunak.cafe24.com:7533/teamplebox");


        return buffer.toString();

    }


}
