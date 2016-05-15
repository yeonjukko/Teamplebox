package com.yeonjukko.teamplebox.signUp;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.yeonjukko.teamplebox.MyTeampleBox.MyActivity;
import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.TeampleBox.Attendance.AttendanceFragment;
import com.yeonjukko.teamplebox.TeampleBox.MaterialActivity;
import com.yeonjukko.teamplebox.TeampleBox.Newsfeed.CommentActivity;
import com.yeonjukko.teamplebox.libs.AndroidDBManager;
import com.yeonjukko.teamplebox.libs.AndroidSessionManager;

import org.json.simple.JSONObject;

import java.util.HashMap;

import gcm.play.android.samples.com.gcmquickstart.QuickstartPreferences;
import gcm.play.android.samples.com.gcmquickstart.RegistrationIntentService;

public class IntroActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private SharedPreferences mSPref;
    private SharedPreferences.Editor mSPeditor;
    private LoginThread mLoginThread;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        mSPref = getSharedPreferences("login", MODE_PRIVATE);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        } else {
            Log.d("test", "not service");
            startLoading();
        }

        /**gcm Broadcast*/
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                startLoading();
            }

        };


    }

    private void startLoading() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (isLoginChecked()) {
                    if (isInviteGroup()) {
                        String gid = getIntent().getData().getQueryParameter("gid");
                        mSPeditor = mSPref.edit();
                        mSPeditor.putString("gid", gid);
                        mSPeditor.commit();
                    } else if (isClickNotification()) {
                        String groupPostId = getIntent().getStringExtra("group_post_id");
                        mSPeditor = mSPref.edit();
                        mSPeditor.putString("group_post_id", groupPostId);
                        mSPeditor.commit();
                    }

                    mLoginThread = new LoginThread();
                    mLoginThread.start();

                } else {
                    if (getIntent().getStringExtra("email") != null) {
                        //로그인을 실행했다면
                        mLoginThread = new LoginThread();
                        mLoginThread.start();
                    } else {
                        Intent intent2Login = new Intent(IntroActivity.this, LoginActivity.class);

                        if (isInviteGroup()) {
                            String gid = getIntent().getData().getQueryParameter("gid");
                            intent2Login.putExtra("gid", gid);
                        }
                        if (isClickNotification()) {
                            String groupPostId = getIntent().getStringExtra("group_post_id");
                            intent2Login.putExtra("group_post_id", groupPostId);
                        }

                        startActivity(intent2Login);
                        finish();
                    }


                }


            }
        }

                , 2000);
    }


    private boolean isInviteGroup() {
        //intro로 invite인텐트를 가져오는가?
        if (getIntent() != null) {
            Uri uri = getIntent().getData();
            if (uri != null) {
                return true;
            }
        }

        return false;
    }

    private boolean isClickNotification() {
        if (getIntent() != null) {

            if (getIntent().getCategories() != null) {
                for (String tmp : getIntent().getCategories()) {
                    if (tmp.equals("clickNotification")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }



    private boolean isLoginChecked() {
        if (mSPref.getBoolean("isAutoLoginChecked", false)) {
            return true;
        }
        return false;
    }

    class LoginThread extends Thread {
        @Override
        public void run() {
            final Intent intent = getIntent();
            String email = intent.getStringExtra("email");
            String passwd = intent.getStringExtra("passwd");

            final Boolean isAutoLoginChecked = intent.getBooleanExtra("isAutoLoginChecked", false);
            //자동 로그인 체크 시
            if (mSPref.getString("email", null) != null) {
                email = mSPref.getString("email", null);
                passwd = mSPref.getString("passwd", null);
            }

            final String finalEmail = email;
            final String finalPasswd = passwd;
            String token = mSPref.getString("token", null);


            HashMap<String, String> query = new HashMap<>();
            query.put("email", email);
            query.put("passwd", passwd);
            query.put("token", token);

            final JSONObject result = AndroidDBManager.login(query);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (!(Boolean) result.get("error")) {
                        if ((Boolean) result.get("success")) {
                            Intent intentMy = new Intent(IntroActivity.this, MyActivity.class);
                            intentMy.putExtra("email", finalEmail);
                            intentMy.putExtra("passwd", finalPasswd);

                            Toast.makeText(IntroActivity.this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                            AndroidSessionManager.setSession(result, IntroActivity.this);

                            if (isAutoLoginChecked) {
                                mSPeditor = mSPref.edit();
                                mSPeditor.putString("email", finalEmail);
                                mSPeditor.putString("passwd", finalPasswd);
                                mSPeditor.putBoolean("isAutoLoginChecked", isAutoLoginChecked);
                                mSPeditor.commit();
                            }

                            if (intent.getStringExtra("gid") != null) {
                                //일반 로그인 시 초대받았다면
                                String gid = intent.getStringExtra("gid");
                                intentMy.putExtra("gid", gid);
                                startActivity(intentMy);
                            } else if (mSPref.getString("gid", null) != null) {
                                //자동 로그인 시 초대받았다면
                                mSPeditor = mSPref.edit();
                                String gid = mSPref.getString("gid", null);
                                intentMy.putExtra("gid", gid);
                                mSPeditor.remove("gid");
                                mSPeditor.commit();
                                startActivity(intentMy);
                            } else {
                                startActivity(intentMy);
                            }


                            if (isClickNotification()) {
                                if (intent.getStringExtra("group_post_id") != null) {
                                    String groupPostId = intent.getStringExtra("group_post_id");
                                    Intent intent2Comment = new Intent(IntroActivity.this, CommentActivity.class);
                                    intent2Comment.putExtra("group_post_id", groupPostId);
                                    startActivity(intent2Comment);
                                }

                            }



                            finish();


                        } else {
                            Toast.makeText(IntroActivity.this, "아이디와 비밀번호를 확인하세요", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(IntroActivity.this, "에러가 발생하였습니다", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private boolean checkPlayServices() {
        //해당 핸드폰이 Google Play 서비스 이용가능한 폰인가 판별
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            }
            return false;

        }
        return true;

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }


}
