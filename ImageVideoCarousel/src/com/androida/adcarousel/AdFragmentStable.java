package com.androida.adcarousel;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.androida.adcarousel.model.AdEntity;
import com.androida.adcarousel.utils.GlideUtil;
import com.androida.adcarousel.utils.VolleyUtils;
import com.androida.adcarousel.widget.AutoScrollViewPager;
import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;

import java.io.File;

public class AdFragmentStable extends Fragment implements CacheListener {
    private static final String TAG = "AdFragmentStable";
	private Context context;
	private View contentView;
	private VideoView videoView;
	private ImageView coverIV;

	private AdEntity item;
	private AutoScrollViewPager mViewPager;

	private GlideUtil glideUtil;
	protected boolean isVisible;
	private HttpProxyCacheServer proxy;
	private boolean isLocalExist = false;

	private File videoFile;

	private static final String LOCAL_VIDEO_PATH = AdView.LOCAL_VIDEO_PATH;

	private int videoWidth, videoHeight;
	private int videoPositionX, videoPositionY;
	private ScaleType imageScaleType = ScaleType.FIT_XY;

	private boolean videoBackgroud = true;

	public void setVideoWidth(int videoWidth) {
		this.videoWidth = videoWidth;
	}

	public void setVideoHeight(int videoHeight) {
		this.videoHeight = videoHeight;
	}

	public void setVideoPositionX(int videoPositionX) {
		this.videoPositionX = videoPositionX;
	}

	public void setVideoPositionY(int videoPositionY) {
		this.videoPositionY = videoPositionY;
	}

	public void setImageScaleType(ScaleType imageScaleType) {
		this.imageScaleType = imageScaleType;
	}

	public void setItem(AdEntity item) {
		this.item = item;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (getUserVisibleHint()) {
			isVisible = true;
			onVisible();
		} else {
			isVisible = false;
			onInvisible();
		}
	}

	protected void onVisible() {
		if (videoView != null) {
			String url = item.getAdUrl();
			videoView.setVisibility(View.VISIBLE);
			String name = url.substring(url.lastIndexOf("/") + 1, url.length());
			videoFile = new File(LOCAL_VIDEO_PATH, name);
			isLocalExist = videoFile.exists() && videoFile.length() > 0;
			if (isLocalExist) {
				String videoPath = new File(LOCAL_VIDEO_PATH, name)
						.getAbsolutePath();
				videoView.setVideoPath(videoPath);
				videoView.start();
			} else {
				// download video or play it online
				proxy.registerCacheListener(this, url);
				String proxyUrl = proxy.getProxyUrl(url);
				videoView.setVideoPath(proxyUrl);
				videoView.start();
			}
		}
	}

	protected void onInvisible() {
		if (videoView != null) {
			videoView.stopPlayback();
			videoView.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
		proxy = VolleyUtils.getProxy(context);
		glideUtil = new GlideUtil(context);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mViewPager != null) {
			mViewPager.startAutoScroll();
		}
		if (videoView != null) {
			if (!videoView.isPlaying()) {
				videoView.start();
			} else {
				videoView.resume();
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mViewPager != null) {
			mViewPager.stopAutoScroll();
		}
		if (videoView != null) {
			// videoView.pause();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (videoView != null) {
			videoView.stopPlayback();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (item == null) {
			return new View(getActivity());
		}
		mViewPager = (AutoScrollViewPager) container;
		int type = item.getType();
		String url = item.getAdUrl();
		boolean isVideo = type == AdEntity.TYPE_VIDEO
				&& !TextUtils.isEmpty(url);
		if (isVideo) { // video list
			contentView = inflater.inflate(R.layout.video_view, null);
			videoView = (VideoView) contentView.findViewById(R.id.videoView);
			// not necessary
			videoView.setZOrderMediaOverlay(true);

			coverIV = (ImageView) contentView.findViewById(R.id.imageView);
			coverIV.setScaleType(imageScaleType);
			ImageView bgIv = (ImageView) contentView.findViewById(R.id.imageViewBg);
			bgIv.setScaleType(imageScaleType);
			String coverUrl = item.getImageUrl();

			glideUtil.loadImage(coverUrl, coverIV, null);
			glideUtil.loadImage(coverUrl, bgIv, null);
			videoView.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) {
					// mp.setLooping(true);
					if (videoBackgroud) {
						coverIV.setVisibility(View.GONE);
						videoView.setBackgroundColor(Color.TRANSPARENT);
					}
					videoView.setVisibility(View.VISIBLE);
					mViewPager.stopAutoScroll();
				}
			});
			videoView.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					// imageView.setVisibility(View.VISIBLE);
					videoView.setScaleX(0f);
					videoView.setScaleY(0f);
					videoView.setVisibility(View.INVISIBLE);
					mViewPager.startAutoScroll(1000);
				}
			});
			videoView.setOnErrorListener(new OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.e(TAG, "error occurring while playing video, will replay, what: " + what + ", extra: "
							+ extra);
					if (isLocalExist) {
						Log.i(TAG, "going to delete file");
						boolean success = videoFile.delete();
						Log.i(TAG, "delete file " + success);
					}

					mViewPager.startAutoScroll(1000);
					return true;
				}
			});

		} else { // image list
			contentView = new ImageView(context);
			// ImageView iv = new ImageView(context);
			((ImageView) contentView).setBackgroundColor(Color.WHITE);
			((ImageView) contentView).setScaleType(imageScaleType);
			if (TextUtils.isEmpty(url)) {
				return contentView;
			}
			glideUtil.loadImage(url, (ImageView) contentView, null);
		}

		return contentView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
	}

	private void setVideoPosition(int position) {
		if (videoView == null) {
			return;
		}
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
		switch (position) {
			case TOP_LEFT:
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				params.leftMargin = 30;
				params.topMargin = 30;
				break;
			case TOP_RIGHT:
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				params.rightMargin = 30;
				params.topMargin = 30;
				break;
			case BOTTOM_LEFT:
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				params.rightMargin = 30;
				params.bottomMargin = 30;
				break;
			case BOTTOM_RIGHT:
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				params.rightMargin = 30;
				params.bottomMargin = 30;
				break;
			case CENTER:
				params.addRule(RelativeLayout.CENTER_VERTICAL);
				params.addRule(RelativeLayout.CENTER_HORIZONTAL);
				break;
		}

	}

	private void setVideoSize(int width, int height) {
		videoView.getLayoutParams().width = width;
		videoView.getLayoutParams().height = height;
	}

	private static final int TOP_LEFT = 1;
	private static final int TOP_RIGHT = 2;
	private static final int BOTTOM_LEFT = 4;
	private static final int BOTTOM_RIGHT = 5;
	private static final int CENTER = 3;

	@Override
	public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
//		Log.i(TAG, String.format("onCacheAvailable. percents: %d, file: %s, url: %s", percentsAvailable, cacheFile, url));
	}
}