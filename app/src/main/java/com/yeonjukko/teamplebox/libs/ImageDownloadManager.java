package com.yeonjukko.teamplebox.libs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by yeonju on 2015-09-21.
 * 이미지 다운로드 후 리사이클뷰에 연결
 */
public class ImageDownloadManager {
    public static Bitmap downloadImage(String img_url) {
        Bitmap mybitmap=null;
        try {
            URL url = new URL(img_url);
            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            mybitmap = BitmapFactory.decodeStream(inputStream);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return mybitmap;

    }
}
