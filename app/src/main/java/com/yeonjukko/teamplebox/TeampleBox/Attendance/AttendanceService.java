package com.yeonjukko.teamplebox.TeampleBox.Attendance;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.libs.AndroidDBManager;
import com.yeonjukko.teamplebox.libs.AndroidSessionManager;
import com.yeonjukko.teamplebox.signUp.IntroActivity;

import org.json.simple.JSONObject;

import java.util.HashMap;


public class AttendanceService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Handler mHandler = new Handler();

    protected static final String TAG = "AttendenceService";
    private static final int FLAG_ALARM_ID = 357357;
    private LocationRequest mLocationRequest;
    private SharedPreferences mSPref;
    private int i = 0;
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    /**
     * Represents a geographical location.
     */
    public static PendingIntent sender;
    private Intent intentNew;
    private AlarmManager alarmManager;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.i("service", "Attend onCreate");
        super.onCreate();
        if (intentNew == null) {
            intentNew = new Intent(AttendanceService.this, AttendanceService.class);
        }

        if (sender == null) {
            sender = PendingIntent.getService(AttendanceService.this, FLAG_ALARM_ID, intentNew, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        if (alarmManager == null) {
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        }

        mSPref = getSharedPreferences("attendance", Context.MODE_PRIVATE);
        buildGoogleApiClient();
        createLocationRequest();
        sendNotification("출석체크 중 입니다.", "팀플박스", mSPref.getString("calendar_id", null),mSPref.getString("group_id",null), "clickAttendance");

    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mGoogleApiClient.connect();
        alarmManager.cancel(sender);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000*5, sender);
        Log.i("service", "Attend onStartCommand");
        Toast.makeText(this, "출석체크가 시작되었습니다.", Toast.LENGTH_SHORT).show();
        return Service.START_NOT_STICKY;

    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        Log.d("service", "Attend onConnected");
        startLocationUpdates();


    }

    @Override
    public void onLocationChanged(Location location) {
        //onConnect 다음 실행
        Log.d("service", "Attend onLocationChanged");
        //Toast.makeText(this, location.getLongitude() + " " + location.getLatitude(), Toast.LENGTH_SHORT).show();
        double longitude = Double.parseDouble(mSPref.getString("longitude", null));
        double latitude = Double.parseDouble(mSPref.getString("latitude", null));
        //double longitude = 126.9385362;
        //double latitude = 37.558346;
        String calendarId = mSPref.getString("calendar_id", null);

        Location lo = new Location("mygps");
        lo.setLongitude(longitude);
        lo.setLatitude(latitude);
        float distance = location.distanceTo(lo);

        if (distance <= 50) {
            //50m이내 이면 출석체크 인정
            final HashMap<String, String> query = new HashMap<>();
            query.put("calendar_id", calendarId);
            SharedPreferences emailPref = getSharedPreferences("email", MODE_PRIVATE);
            query.put("email", emailPref.getString("email", null));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final JSONObject result = AndroidDBManager.groupAttendance(query);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (result == null) {
                                Toast.makeText(AttendanceService.this, "네트워크를 확인하세요.", Toast.LENGTH_SHORT).show();
                            } else {
                                if (!(Boolean) result.get("error")) {
                                    if ((Boolean) result.get("success")) {

                                        //Toast.makeText(AttendanceService.this, "업데이트 성공." + "+" + i++, Toast.LENGTH_SHORT).show();

                                    } else {
                                        Toast.makeText(AttendanceService.this, "출석체크 업로드에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(AttendanceService.this, "에러가 발생하였습니다.", Toast.LENGTH_SHORT).show();

                                }
                            }
                        }
                    });


                }
            }).start();

        }
        //Log.d("test", location.distanceTo(lo) + "");


        stopLocationUpdates();
    }


    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

    }

    protected void createLocationRequest() {
        Log.d("service", "Attend createLocationRequest");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100000000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        Log.d("service", "Attend startLocationUpdates");
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        Log.i("service", "Attend onDestroy");
        stopLocationUpdates();
        super.onDestroy();

    }

    private void sendNotification(String message, String title, String groupCalendarId,String groupId, String category) {
        Intent intent = new Intent(this, IntroActivity.class);
        intent.addCategory(category);
        intent.putExtra("group_calendar_id", groupCalendarId);
        intent.putExtra("group_id",groupId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_icon_main)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(false)
                .setSound(defaultSoundUri)
                .setOngoing(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
