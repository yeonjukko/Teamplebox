package com.yeonjukko.teamplebox.MyTeampleBox;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.libs.AndroidDBManager;
import com.yeonjukko.teamplebox.libs.AndroidSessionManager;
import com.yeonjukko.teamplebox.libs.BackPressCloseHandler;
import com.yeonjukko.teamplebox.libs.MultipartUtility;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class MakeBoxActivity extends AppCompatActivity implements OnPageChangeListener {

    private ViewPager mViewPagerAddGroup;
    //group add
    protected static final int REQUEST_CODE_IMAGE = 13;
    protected static final int REQUEST_CODE_CAMERA = 24;
    protected static final int REQUEST_CODE_DEFAULT_IMAGE = 42;
    private BackPressCloseHandler backPressCloseHandler;
    private ArrayAdapter<String> adapter;
    private Uri mImageCaptureUri;
    private ImageView imgTeampleImg;
    private AlertDialog.Builder alertBuilder;
    String FileName;
    private ImageView mImageViewPhone, mImageViewTablet, mImageViewPC;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_teamplebox);

        mImageViewPhone = (ImageView) findViewById(R.id.imageViewPhone);
        mImageViewTablet = (ImageView) findViewById(R.id.imageViewTablet);
        mImageViewPC = (ImageView) findViewById(R.id.imageViewPC);


        mViewPagerAddGroup = (ViewPager) findViewById(R.id.viewPagerAddGroup);
        mViewPagerAddGroup.setAdapter(new YeonjukkoViewPagerAdapter(getSupportFragmentManager()));

        mViewPagerAddGroup.setOnPageChangeListener(this);

        findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.buttonConfirm).setEnabled(false);


    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        if (position == 0) {
            mImageViewPhone.setScaleX(1 + positionOffset);
            mImageViewPhone.setScaleY(1 + positionOffset);

            mImageViewPhone.setScrollX((int) (-1 * positionOffsetPixels * 0.2));


            mImageViewTablet.setScrollX(positionOffsetPixels);
            mImageViewPC.setScrollX(positionOffsetPixels);
        } else if (position == 1) {
            mImageViewPhone.setAlpha(1 - positionOffset);
        } else if (position == 2) {

        } else {

        }


    }

    @Override
    public void onPageSelected(int position) {
        if (position == 2) {
            findViewById(R.id.buttonConfirm).setEnabled(true);
        } else {
            findViewById(R.id.buttonConfirm).setEnabled(false);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.d("test", "hello1");
        new Thread() {
            @Override
            public void run() {
                Log.d("test", "hello1");
                Log.d("test", resultCode + " " + requestCode);

                if (requestCode == REQUEST_CODE_IMAGE) {
                    if (resultCode == RESULT_OK)
                        try {
                            Log.d("test", "hello1");
                            File file = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");

                            Bitmap image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                            final Bitmap sizingBmp = sizingBitmap(image_bitmap);
                            FileOutputStream fos = new FileOutputStream(file);
                            sizingBmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.close();
                            Log.d("test", "hello2");

                            MultipartUtility multipartUtility = new MultipartUtility(AndroidDBManager.DEFAULT_URL + "uploadGroup.jsp", "UTF-8");
                            multipartUtility.addFilePart("image", file);
                            final JSONObject result = multipartUtility.finish();
                            Log.d("test", "hello3");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (result != null) {
                                        if (!(boolean) result.get("error")) {
                                            if ((boolean) result.get("isSuccess")) {
                                                FileName = (String) result.get("fileName");
                                                imgTeampleImg.setImageBitmap(sizingBmp);
                                            } else {
                                                Toast.makeText(MakeBoxActivity.this, "파일을 업로드하지 못했습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(MakeBoxActivity.this, "오류 발생", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(MakeBoxActivity.this, "null", Toast.LENGTH_SHORT).show();
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

                            MultipartUtility multipartUtility = new MultipartUtility(AndroidDBManager.DEFAULT_URL + "uploadGroup.jsp", "UTF-8");
                            multipartUtility.addFilePart("image", file);
                            final JSONObject result = multipartUtility.finish();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!(boolean) result.get("error")) {
                                        if ((boolean) result.get("isSuccess")) {
                                            FileName = (String) result.get("fileName");
                                            imgTeampleImg.setImageBitmap(sizingBmp);
                                        } else {
                                            Toast.makeText(MakeBoxActivity.this, "파일을 업로드하지 못했습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(MakeBoxActivity.this, "오류 발생", Toast.LENGTH_SHORT).show();
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


    class ViewPagerTabFragment extends Fragment {

        int layout;

        public ViewPagerTabFragment(int layout) {
            this.layout = layout;
        }


        @Nullable
        @Override

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(layout, container, false);
        }
    }

    class YeonjukkoViewPagerAdapter extends FragmentStatePagerAdapter {


        private Fragment[] fragments;
        private int[] layoutId = {R.layout.fragment_view_pager_1, R.layout.fragment_view_pager_2, R.layout.custom_dialog_group_add};

        public YeonjukkoViewPagerAdapter(FragmentManager manager) {
            super(manager);
            this.fragments = new ViewPagerTabFragment[layoutId.length];
        }

        @Override
        public Fragment getItem(int position) {
            // 해당하는 page의 Fragment를 생성합니다.
            if (fragments[position] == null) {

                if (position != 2) {
                    fragments[position] = new ViewPagerTabFragment(layoutId[position]);
                } else {
                    return new MakeGroupFragment();
                }
            }
            return fragments[position];
        }

        @Override
        public int getCount() {
            return layoutId.length;
        }


    }

    class MakeGroupFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.custom_dialog_group_add, container, false);

            TextView tvTextLength = (TextView) view.findViewById(R.id.textLength);
            final EditText etGroupName = (EditText) view.findViewById(R.id.teampleName);
            imgTeampleImg = (ImageView) view.findViewById(R.id.imageViewGroupProfile);

            //그룹 이미지 추가생성
            imgTeampleImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter = new ArrayAdapter<>(MakeBoxActivity.this, android.R.layout.simple_list_item_1);
                    adapter.add("사진 앨범");
                    adapter.add("카메라");
                    adapter.add("기본 이미지");

                    alertBuilder = new AlertDialog.Builder(MakeBoxActivity.this);
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
                                    getActivity().startActivityForResult(intent, REQUEST_CODE_IMAGE);

                                    break;

                                case 1:
                                    // Toast.makeText(ProfileActivity.this, "카메라",
                                    // toast.LENGTH_SHORT).show();
                                    Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    String url2 = "temp_" + System.currentTimeMillis() + ".jpg";
                                    mImageCaptureUri = Uri.fromFile(new File(getExternalCacheDir(), url2));
                                    intent2.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                                    getActivity().startActivityForResult(intent2, REQUEST_CODE_CAMERA);

                                    break;

                                case 2:
                                    imgTeampleImg.setImageDrawable(getResources().getDrawable(R.drawable.img_default_profile));
                                    Intent intent3 = new Intent();
                                    getActivity().startActivityForResult(intent3, REQUEST_CODE_DEFAULT_IMAGE);
                                    break;
                            }

                        }
                    });

                    alertBuilder.show();
                }
            });

            findViewById(R.id.buttonConfirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (etGroupName.getText().toString().equals("")) {
                        Toast.makeText(MakeBoxActivity.this, "팀플명을 입력하세요", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    EditText etTeampleName = (EditText) view.findViewById(R.id.teampleName);
                    final String value = etTeampleName.getText().toString();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HashMap<String, String> query = new HashMap<String, String>();
                            query.put("gname", value);
                            query.put("email", AndroidSessionManager.getInstance().getEmail());
                            Log.d("test", FileName.toString());

                            query.put("group_image", AndroidDBManager.DEFAULT_URL + FileName);
                            final JSONObject result = AndroidDBManager.makeGroup(query);

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    if (!(Boolean) result.get("error")) {
                                        if ((Boolean) result.get("success")) {
                                            Toast.makeText(MakeBoxActivity.this,
                                                    value + " 그룹이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                                            finish();

                                        } else {
                                            Toast.makeText(MakeBoxActivity.this,
                                                    "그룹 생성에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(MakeBoxActivity.this,
                                                "에러발생", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });


                        }
                    }).start();
                }
            });

            final TextView finalTvTextLength = tvTextLength;
            etGroupName.addTextChangedListener(new TextWatcher() {
                String strCur;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    strCur = s.toString();
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 15) {
                        etGroupName.setText(strCur);
                        etGroupName.setSelection(start);
                    } else {
                        finalTvTextLength.setText(String.valueOf(s.length()));

                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });


            return view;
        }
    }


}



