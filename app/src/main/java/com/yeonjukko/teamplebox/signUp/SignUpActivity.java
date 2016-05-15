package com.yeonjukko.teamplebox.signUp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.libs.AndroidDBManager;
import com.yeonjukko.teamplebox.libs.AndroidSessionManager;
import com.yeonjukko.teamplebox.libs.MultipartUtility;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {

    ProgressDialog dialog;
    EditText etName, etEmail, etPasswd, etPasswd2;
    protected static final int REQUEST_CODE_IMAGE = 0;
    protected static final int REQUEST_CODE_CAMERA = 1;
    protected static final int REQUEST_CODE_DEFAULT_IMAGE = 2;
    private ArrayAdapter<String> adapter;
    private AlertDialog.Builder alertBuilder;
    private CircleImageView imageViewProfile;
    private Uri mImageCaptureUri;
    String FileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etName = (EditText) findViewById(R.id.editTextName);
        etEmail = (EditText) findViewById(R.id.editTextEmail);
        etPasswd = (EditText) findViewById(R.id.editTextPasswd);
        etPasswd2 = (EditText) findViewById(R.id.editTextPasswdConfirm);
        Button bt = (Button) findViewById(R.id.buttonSignUp);
        imageViewProfile = (CircleImageView) findViewById(R.id.imageViewProfile);

        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String Email = etEmail.getText().toString();
                if(!hasFocus){
                    if(!AndroidSessionManager.isEmailValid(Email)){
                        etEmail.setError("이메일이 유효하지 않습니다.");
                    }

                }
            }
        });


        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter = new ArrayAdapter<String>(SignUpActivity.this, android.R.layout.simple_list_item_1);
                adapter.add("사진 앨범");
                adapter.add("카메라");
                adapter.add("기본 이미지");

                alertBuilder = new AlertDialog.Builder(SignUpActivity.this);
                alertBuilder.setTitle("프로필 사진을 선택하세요");
                alertBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                alertBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        switch (position) {
                            case 0:

                                // Toast.makeText(ProfileActivity.this, "사진앨범",
                                // Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                String url = "temp_" + System.currentTimeMillis() + ".jpg";
                                mImageCaptureUri = Uri.fromFile(new File(getExternalCacheDir(), url));
                                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                                startActivityForResult(intent, REQUEST_CODE_IMAGE);

                                break;

                            case 1:
                                // Toast.makeText(ProfileActivity.this, "카메라",
                                // toast.LENGTH_SHORT).show();
                                Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                String url2 = "temp_" + System.currentTimeMillis() + ".jpg";
                                mImageCaptureUri = Uri.fromFile(new File(getExternalCacheDir(), url2));
                                intent2.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                                startActivityForResult(intent2, REQUEST_CODE_CAMERA);

                                break;

                            case 2:
                                imageViewProfile.setImageDrawable(getResources().getDrawable(R.drawable.img_default_profile));
                                Intent intent3 = new Intent();
                                startActivityForResult(intent3, REQUEST_CODE_DEFAULT_IMAGE);
                                break;
                        }

                    }
                });

                alertBuilder.show();
            }
        });

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String Name = etName.getText().toString();
                final String Email = etEmail.getText().toString();
                final String Passwd = etPasswd.getText().toString();
                final String Passwd2 = etPasswd2.getText().toString();

                if(Name.equals("")){
                    etName.setError("이름을 입력해주세요.");
                    return;
                }
                if(Email.equals("")){
                    etEmail.setError("이메일을 입력해주세요.");
                    return;
                }
                if(Passwd.equals("")){
                    etPasswd.setError("비밀번호를 입력해주세요.");
                    return;
                }
                if(Passwd2.equals("")){
                    etPasswd2.setError("비밀번호 확인을 입력해주세요.");
                    return;
                }
                if(!AndroidSessionManager.isEmailValid(Email)){
                    etEmail.setError("이메일이 유효하지 않습니다.");
                    return;
                }


                if (!Passwd.equals(Passwd2)) {
                    Toast.makeText(SignUpActivity.this, "비밀번호를 확인하세요", Toast.LENGTH_SHORT).show();
                    return;
                }



                dialog = new ProgressDialog(SignUpActivity.this);
                dialog.setTitle("회원가입");
                dialog.setMessage("진행중..");
                dialog.setCancelable(false);
                dialog.show();

                new Thread() {
                    @Override
                    public void run() {


                        HashMap<String, String> query = new HashMap<String, String>();
                        query.put("email", Email);
                        query.put("passwd", Passwd);
                        query.put("name", Name);
                        query.put("user_image", AndroidDBManager.DEFAULT_URL + FileName);


                        final JSONObject result = (JSONObject) AndroidDBManager.signUp(query);

                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (!(Boolean) result.get("error")) {
                                    if ((Boolean) result.get("success")) {
                                        Toast.makeText(SignUpActivity.this,
                                                "회원가입에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SignUpActivity.this,
                                                LoginActivity.class));
                                        finish();

                                    } else {
                                        dialog.dismiss();
                                        Toast.makeText(SignUpActivity.this,
                                                "이미 이메일이 존재합니다", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                } else {
                                    Toast.makeText(SignUpActivity.this,
                                            "에러발생", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                    }

                }.start();


            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
        return true;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        new Thread() {
            @Override
            public void run() {
                if (requestCode == REQUEST_CODE_IMAGE) {
                    if (resultCode == RESULT_OK) try {
                        File file = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");

                        Bitmap image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                        final Bitmap sizingBmp = sizingBitmap(image_bitmap);
                        FileOutputStream fos = new FileOutputStream(file);
                        sizingBmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.close();

                        MultipartUtility multipartUtility = new MultipartUtility(AndroidDBManager.DEFAULT_URL + "upload.jsp", "UTF-8");
                        multipartUtility.addFilePart("image", file);
                        final JSONObject result = multipartUtility.finish();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!(boolean) result.get("error")) {
                                    if ((boolean) result.get("isSuccess")) {
                                        FileName = (String) result.get("fileName");
                                        imageViewProfile.setImageBitmap(sizingBmp);
                                    } else {
                                        Toast.makeText(SignUpActivity.this, "파일을 업로드하지 못했습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(SignUpActivity.this, "오류 발생", Toast.LENGTH_SHORT).show();
                                }
                            }

                        });

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (requestCode == REQUEST_CODE_CAMERA) {
                    if (resultCode == RESULT_OK)
                        try {
                            Bitmap image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageCaptureUri);
                            final Bitmap sizingBmp = sizingBitmap(image_bitmap);
                            File file = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
                            FileOutputStream fos = new FileOutputStream(file);
                            sizingBmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.close();

                            MultipartUtility multipartUtility = new MultipartUtility(AndroidDBManager.DEFAULT_URL + "upload.jsp", "UTF-8");
                            multipartUtility.addFilePart("image", file);
                            final JSONObject result = multipartUtility.finish();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!(boolean) result.get("error")) {
                                        if ((boolean) result.get("isSuccess")) {
                                            FileName = (String) result.get("fileName");
                                            imageViewProfile.setImageBitmap(sizingBmp);
                                        } else {
                                            Toast.makeText(SignUpActivity.this, "파일을 업로드하지 못했습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(SignUpActivity.this, "오류 발생", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                }

            }

        }.start();
    }

    public Bitmap sizingBitmap(Bitmap image_bitmap) {
        // Get size
        int viewHeight = 200;
        float width = image_bitmap.getWidth();
        float height = image_bitmap.getHeight();

// Calculate image's size by maintain the image's aspect ratio
        if (height > viewHeight) {
            float percente = (height / 100);
            float scale = (viewHeight / percente);
            width *= (scale / 100);
            height *= (scale / 100);
        }

// Resizing image
        Bitmap sizingBmp = Bitmap.createScaledBitmap(image_bitmap, (int) width, (int) height, true);
        return sizingBmp;
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