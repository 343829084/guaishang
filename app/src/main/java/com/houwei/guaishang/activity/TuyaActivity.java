package com.houwei.guaishang.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.houwei.guaishang.R;
import com.houwei.guaishang.event.OpreratePhotoEvent;
import com.houwei.guaishang.util.FileUtils;
import com.houwei.guaishang.view.AdvancedDoodleView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import cn.hzw.doodle.DoodleOnTouchGestureListener;
import cn.hzw.doodle.DoodlePen;
import cn.hzw.doodle.DoodleShape;
import cn.hzw.doodle.DoodleTouchDetector;
import cn.hzw.doodle.DoodleView;
import cn.hzw.doodle.IDoodleListener;
import cn.hzw.doodle.core.IDoodle;

public class TuyaActivity extends Activity implements View.OnClickListener {

    private String url;

    private ImageView close,sure;
//    private AdvancedDoodleView advancedDoodleView;
    private Bitmap bitmap;
    private DoodleView doodleView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tuya_activity);
        close = (ImageView) findViewById(R.id.close);
        sure = (ImageView) findViewById(R.id.sure);


        close.setOnClickListener(this);
        sure.setOnClickListener(this);
        if (getIntent() != null){
            url = getIntent().getStringExtra("url");
        }
        if (url.startsWith("file://")){
            url = url.replace("file://","");
        }
        bitmap = BitmapFactory.decodeFile(url);

        doodleView = new DoodleView(this, bitmap, new IDoodleListener() {
            @Override
            public void onSaved(IDoodle doodle, Bitmap doodleBitmap, Runnable callback) {
                String path = FileUtils.getTempDirPath() +File.separator+"tuya"+System.currentTimeMillis()+".jpg";
                FileUtils.saveBitmap(doodleBitmap,path,1);
                EventBus.getDefault().post(new OpreratePhotoEvent(path));
                finish();
            }

            @Override
            public void onReady(IDoodle doodle) {
                doodle.setSize(5* doodle.getUnitSize());
            }
        });

        // step 2
        DoodleOnTouchGestureListener touchGestureListener = new DoodleOnTouchGestureListener(doodleView, null);
        DoodleTouchDetector touchDetector = new DoodleTouchDetector(this, touchGestureListener);
        doodleView.setDefaultTouchDetector(touchDetector);

        // step 3
        doodleView.setPen(DoodlePen.BRUSH);
        doodleView.setShape(DoodleShape.HAND_WRITE);

        // step 4
        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        container.addView(doodleView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sure:
               doodleView.save();
                break;
            case R.id.close:
                finish();
                break;
        }
    }
}
