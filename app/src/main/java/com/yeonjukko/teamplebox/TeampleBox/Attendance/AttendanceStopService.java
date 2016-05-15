package com.yeonjukko.teamplebox.TeampleBox.Attendance;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AttendanceStopService extends Service {
    private AlarmManager alarmManager;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.i("service", "Attendstop onCreate");
        super.onCreate();

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        alarmManager.cancel(AttendanceService.sender);
        Intent intent1 = new Intent(AttendanceStopService.this, AttendanceService.class);
        stopService(intent1);


        Log.i("service", "Attendstop onStartCommand");
        stopSelf();
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        Log.i("service", "Attendstop onDestoy");
        super.onDestroy();
    }
}
