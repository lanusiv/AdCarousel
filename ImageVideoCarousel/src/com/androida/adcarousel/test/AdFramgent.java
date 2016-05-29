package com.androida.adcarousel.test;

import wseemann.media.FFmpegMediaMetadataRetriever;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.androida.adcarousel.R;
import com.androida.adcarousel.model.AdEntity;
import com.androida.adcarousel.widget.AutoScrollViewPager;
import com.androida.adcarousel.utils.VolleyUtils;

	public class AdFramgent extends Fragment {
		
		private Context context;
		private View contentView;
		private VideoView videoView;
//		private WebView webView;
		private ImageView imageView;
//		private boolean isVideo;
		private AdEntity item;
		private Bitmap coverImage;
		
		private AutoScrollViewPager mViewPager;
		
		/** current visible state for a Fragment, either visible or invisible */
		protected boolean isVisible;
		
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

				if (videoView.isPlaying()) {
					return;
				}
				String url = item.getAdUrl();
				if (coverImage != null) {
					imageView.setImageBitmap(coverImage);
				}
				imageView.setVisibility(View.VISIBLE);
				imageView.setAlpha(1);
//				videoView.setVideoURI(Uri.parse(url));
//				videoView.start();
				
//				Log.d("hello", "onVisible --> url: " + url);
//				videoView.setVideoURI(Uri.parse(url));
//				imageView.setVisibility(View.VISIBLE);
				imageView.animate()
					.setDuration(500)
					.setStartDelay(2000)
					.alpha(1f)
					.setListener(new AnimatorListener() {
						
						@Override
						public void onAnimationStart(Animator animation) {
//							videoView.pause();
						}
						
						@Override
						public void onAnimationRepeat(Animator animation) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onAnimationEnd(Animator animation) {
//							videoView.setVisibility(View.VISIBLE);
//							videoView.start();
//							videoView.seekTo(1000);
//							videoView.start();
							imageView.setVisibility(View.VISIBLE);
							imageView.setAlpha(1);
							mViewPager.stopAutoScroll();
						}
						
						@Override
						public void onAnimationCancel(Animator animation) {
							// TODO Auto-generated method stub
							
						}
					})
					.start();
////				webView.loadUrl(url);
			}
		}

		protected void onInvisible() {
			if (videoView != null) {
//				videoView.stopPlayback();
//				videoView.seekTo(1);
//				videoView.pause();
//				videoView.setVisibility(View.GONE);
				imageView.setVisibility(View.VISIBLE);
//				imageView.setAlpha(1);
			}
			if (imageView != null) {
				imageView.setVisibility(View.VISIBLE);
				imageView.setAlpha(1);
			}
		}
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			context = getActivity();
			// TODO item = ....
		}
		
		@Override
		public void onResume() {
			super.onResume();
			if (mViewPager != null) {
				mViewPager.startAutoScroll();
			}
			if (videoView != null) {
				if (!videoView.isPlaying()) {
//					videoView.start();
				} else {
					videoView.resume();
				}
			}
			// videoView != null && 
		}
		
		@Override
		public void onPause() {
			super.onPause();
			if (mViewPager != null) {
				mViewPager.stopAutoScroll();
			}
			if (videoView != null) {
				videoView.pause();
			}
		}
		
		@Override
		public void onStop() {
			super.onStop();
			if (videoView != null) {
				videoView.stopPlayback();

				mViewPager.startAutoScroll(1000);
			}
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			mViewPager = (AutoScrollViewPager) container;
			int type = item.getType();
			String url = item.getAdUrl();
			boolean isVideo = type == AdEntity.TYPE_VIDEO && !TextUtils.isEmpty(url);
			if (isVideo) { // video list
				contentView = inflater.inflate(R.layout.video_view, null);
				videoView = (VideoView) contentView.findViewById(R.id.videoView);
				videoView.setZOrderOnTop(false);
//				webView = (WebView) contentView.findViewById(R.id.webView);
				imageView = (ImageView) contentView.findViewById(R.id.imageView);
				// ---
//				imageView.setVisibility(View.GONE);
//				videoView.setVideoURI(Uri.parse(url));
//				videoView.start();
				// ---
				getVideoSnapshot(url);
//				webView.setWebViewClient(new WebViewClient() {
//					@Override
//					public boolean shouldOverrideUrlLoading(WebView view,
//							String url) {
//						// TODO Auto-generated method stub
//						return false;
//					}
//				});
				videoView.setOnPreparedListener(new OnPreparedListener() {
					
					@Override
					public void onPrepared(MediaPlayer mp) {
//						mp.setLooping(true);
						mViewPager.stopAutoScroll();
					}
				});
				videoView.setOnCompletionListener(new OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {
//						imageView.setVisibility(View.VISIBLE);
//						videoView.setVisibility(View.GONE);
						mViewPager.startAutoScroll(1000);
					}
				});
				videoView.setOnErrorListener(new OnErrorListener() {
					
					@Override
					public boolean onError(MediaPlayer mp, int what, int extra) {
//						imageView.setVisibility(View.VISIBLE);
//						videoView.setVisibility(View.GONE);
						mViewPager.startAutoScroll(1000);
						return true;
					}
				});
				
				
			} else {  // image list
				contentView = new ImageView(context);
//				ImageView iv = new ImageView(context);
				((ImageView) contentView).setBackgroundColor(Color.WHITE);
//				((ImageView) contentView).setScaleType(ScaleType.FIT_XY);
				if (TextUtils.isEmpty(url)) {
					return contentView;
				}
//				AppController.getInstance().getImageLoader().get(url, new ImageListener() {
				VolleyUtils.getInstance(context).getImageLoader().get(url, new ImageListener() {
					
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e("AdView", "Image Load Error: " + error.getMessage());
					}
					
					@Override
					public void onResponse(ImageContainer response, boolean arg1) {
						if (response.getBitmap() != null) {
							// load image into imageview
							((ImageView) contentView).setImageBitmap(response.getBitmap());
						}
					}
				});
			}
			
			
			return contentView;
		}
		
		private void getVideoSnapshot(final String videoUri) {
			new AsyncTask<String, Void, Bitmap>() {
				@Override
				protected Bitmap doInBackground(String... params) {
					FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
					mmr.setDataSource(videoUri);
					mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
					mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
					Bitmap b = mmr.getFrameAtTime(1000000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST); // frame at 1 seconds
//					byte [] artwork = mmr.getEmbeddedPicture();
					mmr.release();
					return b;
				}
				
				@Override
				protected void onPostExecute(Bitmap result) {
					imageView.setImageBitmap(result);
					coverImage = result;
				}
			}.execute("");
			
		}
	}