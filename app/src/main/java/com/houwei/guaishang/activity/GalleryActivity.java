package com.houwei.guaishang.activity;


import java.io.File;
import java.util.ArrayList;

import com.houwei.guaishang.R;
import com.houwei.guaishang.event.LoginSuccessEvent;
import com.houwei.guaishang.event.ReFreshPhotoEvent;
import com.houwei.guaishang.util.FileUtils;
import com.houwei.guaishang.view.gallery.HackyViewPager;
import com.yalantis.ucrop.UCrop;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

//查看大图activity
public class GalleryActivity extends FragmentActivity implements View.OnClickListener {
	private static final String STATE_POSITION = "STATE_POSITION";
	public static final String EXTRA_IMAGE_INDEX = "image_index";
	public static final String EXTRA_IMAGE_URLS = "image_urls";

	private HackyViewPager mPager;
	private int pagerPosition;

	private TextView cut,tuya,shuiyin;
	private ArrayList<String> urls;
	private ImagePagerAdapter mAdapter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);
		if (!EventBus.getDefault().isRegistered(this)){
			EventBus.getDefault().register(this);
		}
		cut = (TextView) findViewById(R.id.cut);
		tuya = (TextView) findViewById(R.id.tuya);
		shuiyin = (TextView) findViewById(R.id.shuiyin);

		pagerPosition = getIntent().getIntExtra(EXTRA_IMAGE_INDEX, 0);
		urls = (ArrayList<String>) getIntent().getSerializableExtra(EXTRA_IMAGE_URLS);


		mPager = (HackyViewPager) findViewById(R.id.pager);
		mAdapter = new ImagePagerAdapter(
				getSupportFragmentManager(), urls);
		mPager.setAdapter(mAdapter);
//		TextView indicator = (TextView) findViewById(R.id.indicator);
//
//		CharSequence text = getString(R.string.viewpager_indicator, 1, mPager
//				.getAdapter().getCount());
//		indicator.setText(text);
		// 更新下标
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				pagerPosition = arg0;
//				CharSequence text = getString(R.string.viewpager_indicator,
//						arg0 + 1, mPager.getAdapter().getCount());
//				indicator.setText(text);
			}

		});
		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}

		mPager.setCurrentItem(pagerPosition);

		cut.setOnClickListener(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, mPager.getCurrentItem());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ReFreshPhotoEvent photoEvent = new ReFreshPhotoEvent();
		photoEvent.setUrls(urls);
		EventBus.getDefault().post(photoEvent);
		if (EventBus.getDefault().isRegistered(this)){
			EventBus.getDefault().unregister(this);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.cut:
				String destinationFileName = FileUtils.getTempDirPath()+ File.separator +"SampleCropImage"+System.currentTimeMillis()+".jpg";
				File file = new File(destinationFileName);
				UCrop.of(Uri.parse(urls.get(pagerPosition)), Uri.fromFile(file))
						.withAspectRatio(16, 9)
						.withMaxResultSize(500, 500)
						.start(this);
				break;
			case R.id.shuiyin:

				break;
			case R.id.tuya:

				break;
		}
	}

	private class ImagePagerAdapter extends FragmentStatePagerAdapter {

		public 	ArrayList<String>  fileList;

		public ImagePagerAdapter(FragmentManager fm, ArrayList<String> fileList) {
			super(fm);
			this.fileList = fileList;
		}

		@Override
		public int getCount() {
			return fileList == null ? 0 : fileList.size();
		}

		@Override
		public Fragment getItem(int position) {
			String url = fileList.get(position);
			return ImageDetailFragment.newInstance(url);
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
			final Uri resultUri = UCrop.getOutput(data);
			Log.d("lei",resultUri.toString());
			urls.set(pagerPosition,resultUri.toString());
			mAdapter.notifyDataSetChanged();
		} else if (resultCode == UCrop.RESULT_ERROR) {
			final Throwable cropError = UCrop.getError(data);
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void loginSuccess(LoginSuccessEvent event) {
	}
}