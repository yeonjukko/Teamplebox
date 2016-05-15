package com.yeonjukko.teamplebox.signUp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.yeonjukko.teamplebox.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);
        Log.d("test", "login");

        final EditText etEmail = (EditText) findViewById(R.id.editTextLoginEmail);
        final EditText etPasswd = (EditText) findViewById(R.id.editTextLoginPasswd);
        Button btLogin = (Button) findViewById(R.id.buttonLogin);
        Button btSignUp = (Button) findViewById(R.id.buttonLogin2SignUp);
        final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBoxLogin);


        btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,
                        SignUpActivity.class));
                finish();
            }
        });

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String Email = etEmail.getText().toString();
                final String Passwd = etPasswd.getText().toString();
                final boolean isChecked = checkBox.isChecked();

                if (Email.equals("")) {
                    etEmail.setError("이메일을 입력해주세요");

                    return;
                }
                if (Passwd.equals("")) {
                    etPasswd.setError("비밀번호를 입력해주세요");
                    return;
                }


                Intent intentIntro2Login = getIntent();
                String gid = intentIntro2Login.getStringExtra("gid");
                String groupPostId = intentIntro2Login.getStringExtra("group_post_id");

                Intent intent = new Intent(LoginActivity.this, IntroActivity.class);
                intent.putExtra("email", Email);
                intent.putExtra("passwd", Passwd);
                intent.putExtra("gid", gid);
                if (groupPostId != null) {
                    intent.putExtra("group_post_id", groupPostId);
                    intent.addCategory("clickNotification");
                }
                intent.putExtra("isAutoLoginChecked", isChecked);
                startActivity(intent);


                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
}
