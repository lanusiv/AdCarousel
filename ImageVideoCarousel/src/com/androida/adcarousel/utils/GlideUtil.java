package com.androida.adcarousel.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class GlideUtil {
	private static final String TAG = "GlideUtil";

	private Context context;

	public GlideUtil(Context context) {
		this.context = context;
	}

	public void loadImage(String url, ImageView imageView, final ProgressBar pb) {
		Log.i(TAG, "load image：" + url);

		if (pb != null) {
			pb.setVisibility(View.VISIBLE);
		}

		Glide.with(context).load(url).fitCenter().crossFade()
				.listener(new RequestListener<String, GlideDrawable>() {

					@Override
					public boolean onException(Exception e, String arg1,
							Target<GlideDrawable> arg2, boolean arg3) {
						if (pb != null) {
							pb.setVisibility(View.GONE);
						}
						return false;
					}

					@Override
					public boolean onResourceReady(GlideDrawable arg0,
							String arg1, Target<GlideDrawable> arg2,
							boolean arg3, boolean arg4) {
						if (pb != null) {
							pb.setVisibility(View.GONE);
						}
						return false;
					}
				}).into(imageView);
	}
}
