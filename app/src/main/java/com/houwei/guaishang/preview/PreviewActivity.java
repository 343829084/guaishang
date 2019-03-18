package com.houwei.guaishang.preview;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.houwei.guaishang.R;
import com.houwei.guaishang.activity.BaseActivity;
import com.houwei.guaishang.tools.ToastUtils;
import com.houwei.guaishang.util.FileUtils;
import com.houwei.guaishang.widget.PsiDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.annotation.Nullable;

import me.relex.photodraweeview.OnPhotoTapListener;
import me.relex.photodraweeview.PhotoDraweeView;

/**
 * Created by lenovo on 2018/4/21.
 */

public class PreviewActivity extends BaseActivity {

    private MultiTouchViewPager VPager;
    protected ArrayList<String> mUrlList = new ArrayList<>();//图片的URL列表
    protected int mLocation = 0;//刚开始的时候指向第几页
    private final ArrayList<PhotoDraweeView> mPhotoList = new ArrayList<>();//viewpager的每个页面
    private PsiDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        VPager = (MultiTouchViewPager) findViewById(R.id.view_pager);
        parseIntent();
//        ImageLoader.getInstance().displayImage(mUrlList.get(0), VPager);
        initPhotoViews();
        if (mUrlList.size() > 0) {
            loadImage();//初始化viewpager并加载图片
        }
    }

    private void parseIntent(){
        mUrlList = getIntent().getStringArrayListExtra("list");
        mLocation = getIntent().getIntExtra("location",0);
    }

    /*初始化views*/
    private void initPhotoViews() {
        if (mUrlList.size() <= 0)
            return;
        mPhotoList.clear();
        for (int i = 0; i < mUrlList.size(); i++) {
            PhotoDraweeView photoview = new PhotoDraweeView(this);
            mPhotoList.add(i, photoview);
        }
    }
    private void downLoadImage(){
        Uri uri  = Uri.parse(mUrlList.get(mLocation));
        ImageRequest imageRequest =ImageRequestBuilder.
                newBuilderWithSource(uri).
                setProgressiveRenderingEnabled(true).
                build();
        DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline()
                .fetchDecodedImage(imageRequest, PreviewActivity.this);

        dataSource.subscribe(new BaseBitmapDataSubscriber() {
            @Override
            protected void onNewResultImpl(@Nullable Bitmap bitmap) {
                final String filepath = FileUtils.getTempDirPath();
                final String fileName = System.currentTimeMillis()+".jpg";
                final String path = filepath+File.separator+fileName;
                savaImage(bitmap,path);
//               FileUtils.saveBitmap(bitmap, path, 1, new FileUtils.SaveImageCallBack() {
//                   @Override
//                   public void finish() {
//                       try {
//                           MediaStore.Images.Media.insertImage(getContentResolver(), path, fileName, null);
//                           sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, FileUtils.toUri(PreviewActivity.this,path)));
//                       } catch (FileNotFoundException e) {
//                           e.printStackTrace();
//                       }
//                   }
//               });
                if (dialog != null){
                    dialog.dismiss();
                }
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                ToastUtils.toastForShort(PreviewActivity.this,"保存失败");
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        },CallerThreadExecutor.getInstance());
    }


    /**
     * 保存位图到本地
     * @param bitmap
     * @param path   本地路径
     * @return void
     */
    public void savaImage(Bitmap bitmap, String path) {
        File file = new File(path);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        sendBroadcast(intent);
        FileOutputStream fileOutputStream = null;
        String filePhth;
        String fileName;
        //文件夹不存在，则创建它
        if (!file.exists()) {
            file.mkdir();
        }
        try {
            filePhth=path + "/" + System.currentTimeMillis() + ".png";
            fileName=System.currentTimeMillis()+"";
            File file1=new File(filePhth);
            fileOutputStream = new FileOutputStream(file1.getPath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
            //图片路径
//            MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                    filePhth,fileName , null);
            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "", "");
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, FileUtils.toUri(this,file1)));
            Log.d("aaa",file1.getAbsolutePath()+"-----"+path);
            ToastUtils.toastForShort(this,"保存成功");
//            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
        } catch (Exception e) {
            ToastUtils.toastForShort(this,"保存失败");
            e.printStackTrace();
        }
    }
    private void loadImage() {
        MyPageAdapter adapter = new MyPageAdapter(mUrlList);
        VPager.setAdapter(adapter);
        //设置PageChangeListener一定要在setAdapter之后
        VPager.addOnPageChangeListener(mPageChangeListener);
        VPager.setPageMargin(10);
        VPager.setCurrentItem(mLocation, false);
    }
    private final ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {

        public void onPageSelected(int position) {//当进入一个页面后，更换标题，加载图片
            mLocation = position;
            initPhotoIfNeed(mPhotoList.get(position), position);
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int arg0) {
        }
    };
    class MyPageAdapter extends PagerAdapter {

        private final int size;

        public MyPageAdapter(ArrayList<String> list) {
            size = list == null ? 0 : list.size();
        }

        public int getCount() {
            return size;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
           final PhotoDraweeView photoDraweeView = mPhotoList.get(position);
            PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
            Uri heighResUri = null;
            if (mUrlList.size() >= position && mUrlList.get(position) != null) {
                heighResUri = Uri.parse(mUrlList.get(position));
            }
//        controller.setUri(ImageRequest.fromUri(""));
            if (heighResUri != null) {
                controller.setImageRequest(ImageRequest.fromUri(heighResUri));
            }

            controller.setOldController(photoDraweeView.getController());
            controller.setControllerListener(new FormaxFrescoBaseControllerListener<ImageInfo>(heighResUri) {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                    super.onFinalImageSet(id, imageInfo, animatable);
                    if (imageInfo == null) {
                        return;
                    }
                    photoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                }
            });
            photoDraweeView.setController(controller.build());

            //加载刚进来时对应的图片
            if (mLocation == position)
                initPhotoIfNeed(photoDraweeView, position);

            container.addView(photoDraweeView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            photoDraweeView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    dialog = new PsiDialog(PreviewActivity.this,"保存图片？");
                    dialog.setOnButtonClickListener(new PsiDialog.OnButtonClickListener() {
                        @Override
                        public void onNegativeButtonClick(View view) {
                           downLoadImage();
                        }

                        @Override
                        public void onPositiveButtonClick(View view) {
                            ToastUtils.toastForShort(PreviewActivity.this,"取消");
                        }
                    },"确定","取消");
                    dialog.show();
                    return false;
                }
            });
            return photoDraweeView;
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }


    /*加载图片*/
    private void initPhotoIfNeed(final PhotoDraweeView photoDraweeView, int position) {
        if (photoDraweeView == null || photoDraweeView.getController() != null)//当已经有Controller后就不要管了
            return;
        //设置进度条
//        photoDraweeView.getHierarchy().setProgressBarImage(new CircleProgressDrawable());
        //开始加载图片
        PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
        Uri heighResUri = null;
        if (mUrlList.size() >= position && mUrlList.get(position) != null) {
            heighResUri = Uri.parse(mUrlList.get(position));
        }
//        controller.setUri(ImageRequest.fromUri(""));
        if (heighResUri != null) {
            controller.setImageRequest(ImageRequest.fromUri(heighResUri));
        }

        controller.setOldController(photoDraweeView.getController());
        controller.setControllerListener(new FormaxFrescoBaseControllerListener<ImageInfo>(heighResUri) {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                super.onFinalImageSet(id, imageInfo, animatable);
                if (imageInfo == null) {
                    return;
                }
                photoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());

            }
        });



        photoDraweeView.setController(controller.build());
    }

}
