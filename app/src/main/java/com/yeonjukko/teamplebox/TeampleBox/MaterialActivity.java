package com.yeonjukko.teamplebox.TeampleBox;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.TeampleBox.Attendance.AttendanceFragment;
import com.yeonjukko.teamplebox.TeampleBox.Calendar.CalendarFragment;
import com.yeonjukko.teamplebox.TeampleBox.Newsfeed.NewsfeedFragment;
import com.yeonjukko.teamplebox.TeampleBox.TeamInfo.TeamInfoActivity;
import com.yeonjukko.teamplebox.libs.AndroidSessionManager;
import com.yeonjukko.teamplebox.libs.ImageDownloadManager;
import com.yeonjukko.teamplebox.signUp.LoginActivity;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;

public class MaterialActivity extends MaterialNavigationDrawer {
    MaterialAccount account;

    @Override
    public void init(Bundle savedInstanceState) {
        AndroidSessionManager sessionManager = AndroidSessionManager.getInstance();
        Log.d("session", AndroidSessionManager.getInstance().getName() + "in Material");

        account = new MaterialAccount(getResources(),
                sessionManager.getName(),
                sessionManager.getEmail(),
                null, null);
        addAccount(account);


        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                final Bitmap myBitmap = ImageDownloadManager.downloadImage(intent.getStringExtra("myimage"));
                final Bitmap gBitmap = ImageDownloadManager.downloadImage(intent.getStringExtra("gimage"));

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        account.setPhoto(myBitmap);
                        account.setBackground(gBitmap);

                        //notifyAccountDataChanged();
                    }

                });
            }
        }).start();


        addSection(newSection("뉴스피드", R.drawable.cast_ic_notification_on, new NewsfeedFragment()));

        MaterialSection teamSection = newSection("팀원정보", R.drawable.ic_supervisor_account_white_24dp, new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection section) {
                Intent intent = new Intent(MaterialActivity.this, TeamInfoActivity.class);
                intent.putExtra("gid", getIntent().getStringExtra("gid"));
                startActivity(intent);
            }
        });

        addSection(teamSection);
        addSection(newSection("캘린더", R.drawable.ic_insert_invitation_black_24dp, new CalendarFragment()));
        addSection(newSection("출석체크", R.drawable.ic_location_on_black_24dp, new AttendanceFragment()));
        addBottomSection(newSection("로그아웃", R.drawable.ic_lock_outline_black_24dp, new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection section) {

                new AlertDialog.Builder(MaterialActivity.this)
                        .setTitle("로그아웃")
                        .setMessage("팀플박스를 종료하시겠습니까?")
                        .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AndroidSessionManager.deleteSession(MaterialActivity.this);
                                System.exit(0);
                            }
                        })
                        .setNegativeButton("취소!",null).show();

            }
        }));

        Intent intent = getIntent();
        if (intent.getStringExtra("group_calendar_id") != null) {
            setDefaultSectionLoaded(3);
        }

        this.disableLearningPattern();


    }

}
