package com.houwei.guaishang.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.houwei.guaishang.R;
import com.houwei.guaishang.event.LoginSuccessEvent;
import com.houwei.guaishang.event.OpreratePhotoEvent;
import com.houwei.guaishang.util.FileUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smackx.packet.StreamInitiation;

import java.io.File;

import cn.hzw.doodle.DoodleColor;
import cn.hzw.doodle.DoodleOnTouchGestureListener;
import cn.hzw.doodle.DoodleShape;
import cn.hzw.doodle.DoodleTouchDetector;
import cn.hzw.doodle.DoodleView;
import cn.hzw.doodle.IDoodleListener;
import cn.hzw.doodle.core.IDoodle;
import cn.hzw.doodle.core.IDoodleItem;
import cn.hzw.doodle.core.IDoodlePen;

/**
 * Mosaic effect
 */
public class MosaicActivity extends Activity implements View.OnClickListener {
    private DoodleView doodleView;

    private ImageView close,sure;
    private String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mosaic);
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }

        close = (ImageView) findViewById(R.id.close);
        sure = (ImageView) findViewById(R.id.sure);

        close.setOnClickListener(this);
        sure.setOnClickListener(this);
        // step 1

        if (getIntent() != null){
            url = getIntent().getStringExtra("url");
        }
        Bitmap bitmap = BitmapFactory.decodeFile(url);

        doodleView = new DoodleView(this, bitmap, new IDoodleListener() {
            @Override
            public void onSaved(IDoodle doodle, Bitmap doodleBitmap, Runnable callback) {
                String path = FileUtils.getTempDirPath() +File.separator+"mosaic"+System.currentTimeMillis()+".jpg";
                FileUtils.saveBitmap(doodleBitmap,path,1);
                EventBus.getDefault().post(new OpreratePhotoEvent(path));
                finish();
            }

            @Override
            public void onReady(IDoodle doodle) {
                doodle.setSize(30 * doodle.getUnitSize());
            }
        });

        // step 2
        DoodleOnTouchGestureListener touchGestureListener = new DoodleOnTouchGestureListener(doodleView, null);
        DoodleTouchDetector touchDetector = new DoodleTouchDetector(this, touchGestureListener);
        doodleView.setDefaultTouchDetector(touchDetector);

        // step 3
        doodleView.setPen(new MosaicPen());
        doodleView.setShape(DoodleShape.HAND_WRITE);
        findViewById(R.id.btn_mosaic_x3).performClick(); // see setMosaicLevel(View view)

        // step 4
        ViewGroup container = (ViewGroup) findViewById(R.id.doodle_container);
        container.addView(doodleView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }

    private DoodleColor getMosaicColor(int level) {
        Matrix matrix = new Matrix();
        matrix.setScale(1f / level, 1f / level);
        Bitmap mosaicBitmap = Bitmap.createBitmap(doodleView.getBitmap(),
                0, 0, doodleView.getBitmap().getWidth(), doodleView.getBitmap().getHeight(), matrix, true);
        matrix.reset();
        matrix.setScale(level, level);
        DoodleColor doodleColor = new DoodleColor(mosaicBitmap, matrix);
//        doodleColor.setLevel(level);
        return doodleColor;
    }

    public void setMosaicLevel(View view) {
        if (view.getId() == R.id.btn_mosaic_x1) {
            doodleView.setColor(getMosaicColor(5));
        } else if (view.getId() == R.id.btn_mosaic_x2) {
            doodleView.setColor(getMosaicColor(20));
        } else if (view.getId() == R.id.btn_mosaic_x3) {
            doodleView.setColor(getMosaicColor(50));
        }
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

    /*
     Though setting a new pen here is not necessary, the design-based specification should do this.
     虽然这里设置新的画笔不是必要的，但是基于设计的规范应该这样做。马赛克画笔在概念上不同于其他画笔，
     */
    private static class MosaicPen implements IDoodlePen {
        @Override
        public void config(IDoodleItem doodleItem, Paint paint) {
        }

        @Override
        public void drawHelpers(Canvas canvas, IDoodle doodle) {
        }

        @Override
        public IDoodlePen copy() {
            return this;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void loginSuccess(LoginSuccessEvent event) {
    }
}
