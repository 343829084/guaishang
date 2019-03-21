package com.houwei.guaishang.manager;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.NotificationCompat;

import com.baidu.mapapi.SDKInitializer;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.fanjun.keeplive.KeepLive;
import com.fanjun.keeplive.config.ForegroundNotification;
import com.fanjun.keeplive.config.ForegroundNotificationClickListener;
import com.fanjun.keeplive.config.KeepLiveService;
import com.houwei.guaishang.R;
import com.houwei.guaishang.activity.MainActivity;
import com.houwei.guaishang.tools.ApplicationProvider;
import com.mob.MobSDK;
import com.mob.tools.proguard.ProtectedMemberKeeper;
import com.umeng.analytics.MobclickAgent;


import cn.beecloud.BeeCloud;
import cn.jpush.android.api.JPushInterface;

public class ITopicApplication extends MultiDexApplication implements ProtectedMemberKeeper {

	private static Application application;

	// 定位功能想关的Manager管理类
	private MyLocationManager locationManager;
	// 其他一些杂七杂八的Manager管理类，现在是只有Imageloader
	private OtherManager otherManage;
	// 处理当前登录的用户相关的信息
	private MyUserBeanManager myUserBeanManager;
	// 处理用户关注他人的管理器
	private FollowManager followManager;
	// 处理首页上的动态相关的，比如点赞评论
	private HomeManager homeManager;
	// 处理推送来的穿透消息（cmd消息）
	private ChatManager chatManager;
	// 处理版本更新问题
	private VersionManager versionManager;
	// 环信相关的类库
	private HuanXinManager huanXinManager;
	// 表情
	private FaceManager faceManager;
	private boolean hadInit; // 如果为false
								// 说明Application以及被系统回收了，需要重新初始化一遍所有的Manager


	@Override
	public void onCreate() {
		super.onCreate();
		MobSDK.init(this,"15b5b9e067b56","7b60e80917dd1d9b1f90223b02215b9b");
		application = this;
		//极光初始化
		JPushInterface.setDebugMode(true);
		JPushInterface.init(this);

//		Stetho.initializeWithDefaults(this);
		ApplicationProvider.init(this);

//		okgo初始化
//		OkGo.getInstance().init(this);
		keepAlive();
////

//		PublicStaticData.myShareSDK= new ShareSDK();
//		PublicStaticData.myShareSDK.initSDK(getApplicationContext());
		SDKInitializer.initialize(getApplicationContext());//百度地图
		Fresco.initialize(this);
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

	public static Context getContext(){
		return application;
	}

	public void checkInit() {
		if (!hadInit) {
			myUserBeanManager = new MyUserBeanManager(this);
			locationManager = new MyLocationManager(this);
			otherManage = new OtherManager(this);
			followManager = new FollowManager(this);
			homeManager = new HomeManager(this);
			versionManager = new VersionManager(this);
			huanXinManager = new HuanXinManager(this);
			chatManager = new ChatManager(this);
			faceManager = new FaceManager(this);
			otherManage.initOther();
			faceManager.initFaceMap();
			myUserBeanManager.checkUserInfo();
			homeManager.resetPaidTopicPhotoArray();
			huanXinManager.loadAllConversations();
//			短信

//友盟
			MobclickAgent.setDebugMode(true);
			MobclickAgent.setCatchUncaughtExceptions(true); //是否需要错误统计功能
			MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
//			MobclickAgent.updateOnlineConfig(this);
//            Log.d("CCC",UmengUtil.getDeviceInfo(this));
			BeeCloud.setAppIdAndSecret("375dfba1-50cf-419d-8f95-a0933f92ce0b",
	                "00b0dc19-571b-44cd-9563-cc8811f93a15");
			hadInit = true;
		}
	}

	public OtherManager getOtherManage() {
		return otherManage;
	}

	public MyLocationManager getLocationManager() {
		return locationManager;
	}

	public MyUserBeanManager getMyUserBeanManager() {
		return myUserBeanManager;
	}

	public FollowManager getFollowManager() {
		return followManager;
	}

	public HomeManager getHomeManager() {
		return homeManager;
	}

	public ChatManager getChatManager() {
		return chatManager;
	}

	public VersionManager getVersionManager() {
		return versionManager;
	}

	public HuanXinManager getHuanXinManager() {
		return huanXinManager;
	}

	public FaceManager getFaceManager() {
		return faceManager;
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}



	private void keepAlive(){
		ForegroundNotification foregroundNotification = new ForegroundNotification("怪商抢单 -管件抢单神器！","", R.mipmap.logo,
				new ForegroundNotificationClickListener() {

					@Override
					public void foregroundNotificationClick(Context context, Intent intent) {
						Intent intent1 = new Intent(context,MainActivity.class);
						context.startActivity(intent1);
					}
				});
		KeepLive.startWork(this, KeepLive.RunMode.ROGUE, foregroundNotification,
				new KeepLiveService() {

					@Override
					public void onWorking() {

					}

					@Override
					public void onStop() {

					}
				}
		);
	}
}