package com.androida.adcarousel;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Patterns;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.androida.adcarousel.model.AdEntity;
import com.androida.adcarousel.utils.GlideUtil;
import com.androida.adcarousel.utils.JsonUtil;
import com.androida.adcarousel.utils.VolleyUtils;
import com.androida.adcarousel.widget.AutoScrollViewPager;
import com.androida.adcarousel.widget.CustomPageTransformer;
import com.androida.adcarousel.widget.MyFragmentPagerAdapter;
import com.androida.adcarousel.widget.CustomPageTransformer.TransformType;
import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

/**
 * Play videos or images cyclically
 *
 */
public class AdView extends RelativeLayout implements CacheListener {
	private final static String TAG = AdView.class.getSimpleName();
	private static final int AUTO_SCROLL_DURATION_FACTOR = 5;
	private static final int SCROLL_DURATION_FACTOR = 5;
	private static final int TRANSFORM_TYPE_RANDOM = -1;
	private static final int TRANSFORM_TYPE_NONE = -2;

	private static String sdCard = Environment.getExternalStorageDirectory()
			.getAbsolutePath();
	private AutoScrollViewPager mViewPager;
	private ImageView mSingleIV;
	private VideoView mVideoView;
	private List<AdEntity> mDataList; // data source
	private AdEntity singleAd; // single data item

	private ProgressBar pb;

	protected boolean isTransformRandom = false; // random transform for viewpager

	private File curVideoFile;

	private WeakReference<FragmentActivity> mActivity;
	private long interval = 3 * 1000;
	private int transformType = TRANSFORM_TYPE_NONE;

	// temp solution for rightly display while adding multi adview to a layout
	private static final int[] IDS = { R.id.vp1, R.id.vp2, R.id.vp3, R.id.vp4,
			R.id.vp5, R.id.vp6, R.id.vp7, R.id.vp8, R.id.vp9, R.id.vp10,
			R.id.vp11, R.id.vp12, R.id.vp13, R.id.vp14, R.id.vp15, R.id.vp16,
			R.id.vp17, R.id.vp18, R.id.vp19, R.id.vp20, R.id.vp21, R.id.vp22,
			R.id.vp23, R.id.vp24, R.id.vp25, R.id.vp26, R.id.vp27, R.id.vp28,
			R.id.vp29, R.id.vp30 };
	private static int index = 0;

	public static HttpProxyCacheServer proxy;

	private static final String SDCARD = sdCard;

	public static String LOCAL_VIDEO_PATH = SDCARD + "/adcarousel/video";

	private int curPage = 0;

	private OnAdClickListener mListener;

	private boolean isLocalExist = false;

	private boolean infiniteScroll = true;

	private ScaleType imageScaleType = ScaleType.FIT_XY;

	private GlideUtil glideUtil;

	private GestureDetectorCompat mDetector;
	private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new SimpleOnGestureListener() {
		public boolean onSingleTapUp(MotionEvent e) {
			if (mDataList != null && mDataList.size() > curPage) {
				AdEntity ad = mDataList.get(curPage);
				if (mListener != null) {
					mListener.onAdClick(ad);
				}
			}

			if (singleAd != null) {
				if (mListener != null) {
					mListener.onAdClick(singleAd);
				}
			}
			return true;
		};

	};

	public interface OnAdClickListener {
		void onAdClick(AdEntity ad);
	}

	public void setOnAdClickListener(OnAdClickListener listener) {
		this.mListener = listener;
	}

	public interface OnPageSelectedListener {
		void onPageSelected(int position);
	}

	private OnPageSelectedListener mOnPageSelectedListener;

	public void setOnPageSelectedListener(OnPageSelectedListener mOnPageSelectedListener) {
		this.mOnPageSelectedListener = mOnPageSelectedListener;
	}

	public AdView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public AdView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AdView(Context context) {
		super(context);
	}

	public void initAdView(FragmentActivity activity) {
		mActivity = new WeakReference<FragmentActivity>(activity);

		init();
	}

	private void init() {
		FragmentActivity activity = mActivity.get();
		if (activity == null) {
			return;
		}
		inflate(activity, R.layout.adview, this);
		mViewPager = (AutoScrollViewPager) findViewById(R.id.mViewPager);
		mViewPager.setId(IDS[index++ % IDS.length]);
		mSingleIV = (ImageView) findViewById(R.id.singleIV);
		mVideoView = (VideoView) findViewById(R.id.videoView);
		pb = (ProgressBar) findViewById(R.id.progressBar);

		mDetector = new GestureDetectorCompat(activity,
				mSimpleOnGestureListener);

		glideUtil = new GlideUtil(activity);
		String packageName = activity.getPackageName();
//		LOCAL_VIDEO_PATH = SDCARD + "/" + packageName + "/video";
		File f = new File(LOCAL_VIDEO_PATH);
		if (!f.exists()) {
			f.mkdirs();
		}

		// support for d-pad navigation
		mViewPager.setFocusable(true);
		mViewPager.setFocusableInTouchMode(true);
		mViewPager.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

		proxy = VolleyUtils.getProxy(getContext());
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		mDetector.onTouchEvent(ev);
		mViewPager.dispatchTouchEvent(ev);
		return true;
	}

	/**
	 *
	 * @param infiniteScroll
	 */
	public void setInfiniteScroll(boolean infiniteScroll) {
		this.infiniteScroll = infiniteScroll;
	}

	/**
	 * load data via a url address
	 * 
	 * @param url
	 */
	public void loadAdList(String url) {
		if (mActivity == null || mActivity.get() == null) {
			return;
		}
		Log.i(TAG, "loadAdList  url: " + url);
		requestAdList(url);
	}

	/** Volley request */
	private void requestAdList(String url) {
		if (mActivity == null) {
			return;
		}

		FragmentActivity activity = mActivity.get();
		if (activity == null) {
			return;
		}

		String tag_json_obj = "json_obj_req";

		showProgressBar();

		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET, url,
				null, new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						dismissProgressBar();
						initDataWithJSON(response);
					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						VolleyLog.d(TAG, "Error: " + error.getMessage());
						// hide the progress dialog
						dismissProgressBar();
					}
				});

		// Adding request to request queue
		VolleyUtils.getInstance(activity).addToRequestQueue(jsonObjReq,
				tag_json_obj);
	}

	/** convert json to list */
	private void initDataWithJSON(JSONObject json) {
		try {
			int code = json.getInt("code");
			json.getString("msg");
			JSONArray data = json.getJSONArray("data");
			if (data.length() == 0) {
				return;
			}
			if (code == 1 && data != null) {
				mDataList = JsonUtil.createJsonToListBean(data.toString(),
						AdEntity.class);
				loadViewPager(mDataList);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void loadAdListByJsonArray(JSONArray data) {
		mDataList = JsonUtil.createJsonToListBean(data.toString(),
				AdEntity.class);
		loadViewPager(mDataList);
	}

	public void loadAdList(List<AdEntity> data) {
		mDataList = data;
		loadViewPager(data);
	}

	/**
	 * set viewpager transform type
	 * 
	 * @param type
	 */
	public void setPageTransformer(int type) {
		if (type == TRANSFORM_TYPE_RANDOM) {
			isTransformRandom = true;
			return;
		}
		TransformType[] tt = TransformType.values();
		type %= tt.length;
		CustomPageTransformer pt = new CustomPageTransformer(tt[type]);
		mViewPager.setPageTransformer(true, pt);
	}

	public void loadSingleAdImage(AdEntity ad) {
		this.singleAd = ad;
		String url = ad.getAdUrl();
		// Log.d(TAG, "ad url: " + url);
		mSingleIV.bringToFront(); // either is ok
		mSingleIV.setBackgroundColor(Color.TRANSPARENT);
		mSingleIV.setScaleType(ScaleType.FIT_XY);
		mVideoView.setVisibility(View.GONE);

		glideUtil.loadImage(url, mSingleIV, null);
	}

	private void loadViewPager(List<AdEntity> data) {
		if (mActivity == null) {
			return;
		}
		FragmentActivity activity = mActivity.get();
		if (activity == null) {
			return;
		}

		if (data.size() == 1) { // single ad entity
			AdEntity item = data.get(0);
			int type = item.getType();
			final String url = item.getAdUrl();
			boolean isVideo = type == AdEntity.TYPE_VIDEO
					&& !TextUtils.isEmpty(url);
			if (isVideo) {
				if (mVideoView == null) {
					mVideoView = (VideoView) findViewById(R.id.videoView);
				}
				String coverUrl = item.getImageUrl();
				mSingleIV.bringToFront();
				glideUtil.loadImage(coverUrl, mSingleIV, null);

				setVideoListeners();

				mViewPager.setVisibility(View.GONE);

			} else {
				mSingleIV.bringToFront();
				mSingleIV.setBackgroundColor(Color.TRANSPARENT);
				mSingleIV.setScaleType(ScaleType.FIT_XY);
				mVideoView.setVisibility(View.GONE);

				glideUtil.loadImage(url, mSingleIV, null);
			}
			return;
		}
		// if we have a data source that container more than one data item, we set up view pager
		setPagerAdapter(new MyFragmentPagerAdapter(
				activity.getSupportFragmentManager(), data, infiniteScroll, imageScaleType));
		Log.d(TAG, "invoke method loadViewPager");
	}

	private void setupVideo(final String url) {
		boolean isUrlValidate = checkUrlValidate(url);
		if (!isUrlValidate) {
			return;
		}

		if (mActivity == null) {
			return;
		}
		FragmentActivity activity = mActivity.get();
		if (activity == null) {
			return;
		}

		String name = url.substring(url.lastIndexOf("/") + 1, url.length());
		curVideoFile = new File(LOCAL_VIDEO_PATH, name);
		isLocalExist = curVideoFile.exists() && curVideoFile.length() > 0;

		if (isLocalExist) {
			final String videoPath = new File(LOCAL_VIDEO_PATH, name)
					.getAbsolutePath();
			Log.i(TAG, "local file path is " + videoPath);
			mVideoView.setVideoPath(videoPath);
			mVideoView.start();
		} else {
//			downloadVideo(url);
//			Log.i(TAG, "start download video, url: " + url);
            proxy.registerCacheListener(this, url);
            String proxyUrl = proxy.getProxyUrl(url);
			mVideoView.setVideoPath(proxyUrl);
			mVideoView.start();
		}
	}

	private void setVideoListeners() {
		mVideoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.setLooping(true);
				mSingleIV.setVisibility(View.GONE);
				mVideoView.bringToFront();
			}
		});
		mVideoView.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.e(TAG, "error occurring while playing video, will replay, what: " + what + ", extra: "
						+ extra);
				if (isLocalExist) {
					Log.i(TAG, "going to delete file");
					boolean success = curVideoFile.delete();
					Log.i(TAG, "delete file " + success);
					// uncomment the next line to restart activity
//					refreshActivity();
				}
				// return true if we consumed this error, or we will be
				// displayed an error dialog saying the video can't be played
				return true;
			}
		});
	}

	/**
	 * check whether this url is validate or not
	 * 
	 * @param url
	 * @return
	 */
	private boolean checkUrlValidate(final String url) {
		boolean valid = false;
		boolean sourceAvailable = false;
		valid = URLUtil.isValidUrl(url)
				&& Patterns.WEB_URL.matcher(url).matches();
		try {
			sourceAvailable = VolleyUtils.readSourceAvailable(new URL(url));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sourceAvailable && valid;
	}

	public void setPagerAdapter(FragmentPagerAdapter adapter) {
		mViewPager.setAdapter(adapter);
		// set stay duration for each page
		setInterval(interval);
		setAutoScrollDurationFactor(AUTO_SCROLL_DURATION_FACTOR);

		mViewPager.addOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				int size = mDataList.size();
				curPage = position % size;

				if (isTransformRandom) {
					setRandomTransformer();
				} else { // various settings by data source
					AdEntity ad = mDataList.get(curPage);
					String interval = ad.getInterval();
					if (!TextUtils.isEmpty(interval)) {
						double intervald = Double.parseDouble(interval);
						long intervall = (long) (intervald * 1000);
						if (intervall >= 1) {
							setInterval(intervall);
						}
					}

					String transformType = ad.getAnimationType();
					if (!TextUtils.isEmpty(transformType)) {
						TransformType tt = null;
						try {
							tt = TransformType.valueOf(transformType);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (tt != null) {
							CustomPageTransformer pt = new CustomPageTransformer(
									tt);
							mViewPager.setPageTransformer(true, pt);
						}
					}
				}
				if (mOnPageSelectedListener != null) {
					mOnPageSelectedListener.onPageSelected(position);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	public void pause() {
		if (mVideoView != null && mVideoView.isPlaying()) {
			mVideoView.pause();
		}
		mViewPager.stopAutoScroll();
	}

	public void resume() {
		if (mVideoView != null && !mVideoView.isPlaying()) {
			mVideoView.start();
		}
		mViewPager.startAutoScroll();
	}

	public void stop() {
		if (mVideoView != null && mVideoView.isPlaying()) {
			if (proxy != null) {
				proxy.shutdown();
			}
			mVideoView.stopPlayback();
		}
		mViewPager.stopAutoScroll();
	}

	public void destroy() {
		mViewPager.stopAutoScroll();
		mViewPager.removeAllViews();
	}

	private void showProgressBar() {
		pb.setVisibility(View.VISIBLE);
	}

	private void dismissProgressBar() {
		pb.setVisibility(View.GONE);
	}

	private void setRandomTransformer() {
		Random r = new Random();
		int type = r.nextInt(4);
		TransformType[] tt = TransformType.values();
		CustomPageTransformer pt = new CustomPageTransformer(tt[type]);
		mViewPager.setPageTransformer(true, pt);
	}

	public void setImageScaleType(ScaleType imageScaleType) {
		this.imageScaleType = imageScaleType;
	}

	public AdEntity getCurAdEntity() {
		AdEntity entity = null;
		if (mDataList != null) {
			entity = mDataList.get(curPage);
		}
		return entity;
	}

	public AutoScrollViewPager getViewPager() {
		return mViewPager;
	}

	/**
	 * restart activity
	 */
	public void refreshActivity() {
		if (mActivity == null) {
			return;
		}
		FragmentActivity activity = mActivity.get();
		if (activity == null) {
			return;
		}
		Intent intent = activity.getIntent();
		activity.finish();
		activity.startActivity(intent);
		activity.overridePendingTransition(0, 0);
	}

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
//        Log.d(TAG, String.format("onCacheAvailable. percents: %d, file: %s, url: %s", percentsAvailable, cacheFile, url));
    }

	/**
	 * AutoScrollViewPager Proxy Methods
	 */

	/**
	 * start auto scroll, first scroll delay time is {@link #getInterval()}
	 */
	public void startAutoScroll() {
		mViewPager.startAutoScroll();
	}

	/**
	 * start auto scroll
	 * 
	 * @param delayTimeInMills
	 *            first scroll delay time
	 */
	public void startAutoScroll(int delayTimeInMills) {
		mViewPager.startAutoScroll(delayTimeInMills);
	}

	/**
	 * stop auto scroll
	 */
	public void stopAutoScroll() {
		mViewPager.stopAutoScroll();
	}

	public boolean isAutoScroll() {
		return mViewPager.isAutoScroll();
	}

	public void setAutoScrollEnabled(boolean allowed) {
		mViewPager.setAutoScrollEnabled(allowed);
	}

	public synchronized void setCurrentItem(int position) {
		int count = mDataList.size();
		position = position < 0 ? count + position % count : position;
		mViewPager.setCurrentItem(position);
	}

	public synchronized int nextPage() {
		curPage++;
		if (!infiniteScroll) {
			if (curPage >= getPageSize()) {
				curPage = getPageSize() - 1;
			}
		}
		mViewPager.setCurrentItem(curPage);
		return curPage;
	}

	public synchronized int previousPage() {
		curPage--;
		if (curPage < 0) {
			curPage = 0;
		}
		mViewPager.setCurrentItem(curPage);
		return curPage;
	}

	public int getPageSize() {
		return mDataList == null ? 0 : mDataList.size();
	}

	public void setScrollDurationFactor(double factor) {
		mViewPager.setScrollDurationFactor(factor);
	}

	/**
	 * set the factor by which the duration of sliding animation will change
	 * while swiping
	 */
	public void setSwipeScrollDurationFactor(double scrollFactor) {
		mViewPager.setSwipeScrollDurationFactor(scrollFactor);
	}

	/**
	 * set the factor by which the duration of sliding animation will change
	 * while auto scrolling
	 */
	public void setAutoScrollDurationFactor(double scrollFactor) {
		mViewPager.setAutoScrollDurationFactor(scrollFactor);
	}

	/**
	 * scroll only once
	 */
	public void scrollOnce() {
		mViewPager.scrollOnce();
	}

	/**
	 * get auto scroll time in milliseconds, default is
	 *
	 * @return the interval
	 */
	public long getInterval() {
		return mViewPager.getInterval();
	}

	/**
	 * set auto scroll time in milliseconds, default is
	 *
	 * @param interval
	 *            the interval to set
	 */
	public void setInterval(long interval) {
		this.interval = interval;
		mViewPager.setInterval(interval);
	}

	/**
	 * get auto scroll direction
	 * 
	 */
	public int getDirection() {
		return mViewPager.getDirection();
	}

	/**
	 * set auto scroll direction
	 * 
	 * @param direction
	 */
	public void setDirection(int direction) {
		mViewPager.setDirection(direction);
	}

	/**
	 * whether automatic cycle when auto scroll reaching the last or first item,
	 * default is true
	 * 
	 * @return the isCycle
	 */
	public boolean isCycle() {
		return mViewPager.isCycle();
	}

	/**
	 * set whether automatic cycle when auto scroll reaching the last or first
	 * item, default is true
	 * 
	 * @param isCycle
	 *            the isCycle to set
	 */
	public void setCycle(boolean isCycle) {
		mViewPager.setCycle(isCycle);
	}

	/**
	 * whether stop auto scroll when touching, default is true
	 * 
	 * @return the stopScrollWhenTouch
	 */
	public boolean isStopScrollWhenTouch() {
		return mViewPager.isStopScrollWhenTouch();
	}

	/**
	 * set whether stop auto scroll when touching, default is true
	 * 
	 * @param stopScrollWhenTouch
	 */
	public void setStopScrollWhenTouch(boolean stopScrollWhenTouch) {
		mViewPager.setStopScrollWhenTouch(stopScrollWhenTouch);
	}

	/**
	 * get how to process when sliding at the last or first item
	 * 
	 * @return the slideBorderMode {@link AutoScrollViewPager#SLIDE_BORDER_MODE_NONE},
	 *         {@link AutoScrollViewPager#SLIDE_BORDER_MODE_TO_PARENT},
	 *         {@link AutoScrollViewPager#SLIDE_BORDER_MODE_CYCLE}, default is
	 *         {@link AutoScrollViewPager#SLIDE_BORDER_MODE_NONE}
	 */
	public int getSlideBorderMode() {
		return mViewPager.getSlideBorderMode();
	}

	/**
	 * set how to process when sliding at the last or first item
	 * 
	 * @param slideBorderMode
	 *            {@link AutoScrollViewPager#SLIDE_BORDER_MODE_NONE},
	 *            {@link AutoScrollViewPager#SLIDE_BORDER_MODE_TO_PARENT},
	 *            {@link AutoScrollViewPager#SLIDE_BORDER_MODE_CYCLE}, default is
	 *            {@link AutoScrollViewPager#SLIDE_BORDER_MODE_NONE}
	 */
	public void setSlideBorderMode(int slideBorderMode) {
		mViewPager.setSlideBorderMode(slideBorderMode);
	}

	/**
	 * whether animating when auto scroll at the last or first item, default is
	 * true
	 * 
	 * @return
	 */
	public boolean isBorderAnimation() {
		return mViewPager.isBorderAnimation();
	}

	/**
	 * set whether animating when auto scroll at the last or first item, default
	 * is true
	 * 
	 * @param isBorderAnimation
	 */
	public void setBorderAnimation(boolean isBorderAnimation) {
		mViewPager.setBorderAnimation(isBorderAnimation);
	}

}
