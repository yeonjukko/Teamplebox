package com.yeonjukko.teamplebox.MyTeampleBox;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.adapter.RecycleGroupListAdapter;
import com.yeonjukko.teamplebox.libs.AndroidDBManager;
import com.yeonjukko.teamplebox.libs.AndroidSessionManager;
import com.yeonjukko.teamplebox.libs.BackPressCloseHandler;
import com.yeonjukko.teamplebox.libs.ImageDownloadManager;
import com.yeonjukko.teamplebox.libs.MultipartUtility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyActivity extends AppCompatActivity {

    private final static int MAKE_GROUP_CODE = 122;

    AndroidSessionManager sessionManager;
    RecyclerView recyclerView;
    CircleImageView myProfileCircleView;
    SwipeRefreshLayout swipeRefreshLayout;

    FloatingActionButton btAddGroup;

    //group add
    protected static final int REQUEST_CODE_IMAGE = 0;
    protected static final int REQUEST_CODE_CAMERA = 1;
    protected static final int REQUEST_CODE_DEFAULT_IMAGE = 2;
    private BackPressCloseHandler backPressCloseHandler;
    private ArrayAdapter<String> adapter;
    private Uri mImageCaptureUri;
    private ImageView imgTeampleImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        sessionManager = AndroidSessionManager.getInstance();

        backPressCloseHandler = new BackPressCloseHandler(this);
        if (sessionManager == null) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (getIntent() != null) {
            if (getIntent().getStringExtra("gid") != null) {

                final AlertDialog.Builder alert = new AlertDialog.Builder(MyActivity.this);
                alert.setTitle("초대그룹 추가하기");
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String gid = getIntent().getStringExtra("gid");
                        final String email = AndroidSessionManager.getInstance().getEmail();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                HashMap<String, String> query = new HashMap<String, String>();
                                query.put("gid", gid);
                                query.put("email", email);

                                final JSONObject result = AndroidDBManager.inviteGroup(query);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!(Boolean) result.get("error")) {
                                            if ((Boolean) result.get("success")) {
                                                Toast.makeText(MyActivity.this, "그룹 추가에 성공하였습니다.", Toast.LENGTH_LONG).show();
                                                startGroupLoadThread();

                                            } else {

                                                Toast.makeText(MyActivity.this, "그룹 추가를 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(MyActivity.this, "에러가 발생했습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                        }).start();


                    }
                });
                alert.create().show();


            }
        }
        recyclerView = (RecyclerView) findViewById(R.id.listView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        myProfileCircleView = (CircleImageView) findViewById(R.id.mycircleimageview);
        btAddGroup = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.floatingButton);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_my_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startGroupLoadThread();

            }

        });
        startGroupLoadThread();

        //팀플생성
        btAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MyActivity.this, MakeBoxActivity.class), MAKE_GROUP_CODE);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MAKE_GROUP_CODE) {
            startGroupLoadThread();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startGroupLoadThread() {
        new Thread() {
            @Override
            public void run() {
                final String Email = sessionManager.getEmail();
                final String ImgUrl = sessionManager.getImgUrl();
                final String Name = sessionManager.getName();
                final Bitmap myBitmap = ImageDownloadManager.downloadImage(ImgUrl);

                HashMap<String, String> query = new HashMap<>();
                query.put("email", Email);
                final JSONArray result = AndroidDBManager.searchGroup(query);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tvName = (TextView) findViewById(R.id.textViewUserName);
                        tvName.setText(Name);
                        myProfileCircleView.setImageBitmap(myBitmap);

                        RecycleGroupListAdapter adapter = new RecycleGroupListAdapter(result, MyActivity.this);
                        recyclerView.setAdapter(adapter);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });


            }
        }.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }
}