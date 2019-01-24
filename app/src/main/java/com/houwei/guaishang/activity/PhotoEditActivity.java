package com.houwei.guaishang.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.houwei.guaishang.R;
import com.houwei.guaishang.event.OpreratePhotoEvent;
import com.houwei.guaishang.event.TakePhotoEvent;
import com.houwei.guaishang.util.FileUtils;
import com.yalantis.ucrop.UCrop;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PhotoEditActivity extends BaseActivity {

    @BindView(R.id.container)
    RelativeLayout container;
    @BindView(R.id.cut)
    TextView cut;
    @BindView(R.id.tuya)
    TextView tuya;
    @BindView(R.id.mosaic)
    TextView mosaic;
    //调用拍照，拍出来的原图的url
    private String camera_pic_path = "";
    private ImageDetailFragment imageDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_edit_activity);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        ButterKnife.bind(this);
        pickPhotoFromCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void pickPhotoFromCamera() {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            showErrorToast("请插入sd卡");
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String name = DateFormat.format("yyyyMMddhhmmss",
                Calendar.getInstance(Locale.CHINA))
                + ".jpg";

        File file = new File(BasePhotoActivity.SAVEPATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        camera_pic_path = BasePhotoActivity.SAVEPATH + "/" + name;
        File mCurrentPhotoFile = new File(camera_pic_path);
//		intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(mCurrentPhotoFile));
//		startActivityForResult(intent, BasePhotoActivity.PHOTO_CAMERA_WITH_DATA);
//
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(MediaStore.Images.Media.DATA, mCurrentPhotoFile.getAbsolutePath());
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, uri), BasePhotoActivity.PHOTO_CAMERA_WITH_DATA);
    }


    private void addFragment() {
        String url = camera_pic_path;
        if (!url.startsWith("file://")) {
            url = "file://" + url;
        }
        imageDetailFragment = ImageDetailFragment.newInstance(url);
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container, imageDetailFragment);
        fragmentTransaction.commit();
    }


    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case BasePhotoActivity.PHOTO_CAMERA_WITH_DATA://拍照成功
                    addFragment();
                    break;
                case UCrop.REQUEST_CROP:
                    final Uri resultUri = UCrop.getOutput(data);
                    camera_pic_path = resultUri.toString();
                    break;
            }
        }
    }

    @OnClick({R.id.cut, R.id.tuya, R.id.mosaic,R.id.sure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cut:
                String destinationFileName = FileUtils.getTempDirPath() + File.separator + "SampleCropImage" + System.currentTimeMillis() + ".jpg";
                File file = new File(destinationFileName);
                String tempUrl = camera_pic_path;
                if (!tempUrl.startsWith("file://")) {
                    tempUrl = "file://" + tempUrl;
                }
                UCrop uCrop = UCrop.of(Uri.parse(tempUrl), Uri.fromFile(file))
                        .withAspectRatio(16, 9)
                        .withMaxResultSize(500, 500);
                UCrop.Options options = new UCrop.Options();
                options.setShowCropGrid(false);
                uCrop.withOptions(options);
                uCrop.start(this);
                break;
            case R.id.tuya:
                Intent tuyaIntent = new Intent(this, TuyaActivity.class);
                String url = camera_pic_path;
                if (url.startsWith("file://")) {
                    url = url.replace("file://", "");
                }
                tuyaIntent.putExtra("url", url);
                startActivity(tuyaIntent);
                break;
            case R.id.mosaic:
                Intent intent = new Intent(this, MosaicActivity.class);
                if (camera_pic_path.startsWith("file://")) {
                    camera_pic_path = camera_pic_path.replace("file://", "");
                }
                intent.putExtra("url", camera_pic_path);
                startActivity(intent);
                break;
            case R.id.sure:
                TakePhotoEvent event = new TakePhotoEvent();
                String eventUrl = camera_pic_path;
                if (eventUrl.startsWith("file://")){
                    eventUrl = eventUrl.replace("file://","");
                }
                event.setUrl(eventUrl);
                EventBus.getDefault().post(event);
                finish();
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void opreratePhotoEvent(OpreratePhotoEvent event) {
        String url = event.getUrl();
        camera_pic_path = url;
        if (!url.startsWith("file://")) {
            url = "file://" + url;
        }
        if (imageDetailFragment != null) {
            imageDetailFragment.setImage(url);
        }
    }

}
