package com.androida.adcarousel.model;

import android.text.TextUtils;

public class AdEntity {

	public static final int TYPE_IMAGE = 1;
	public final static int TYPE_VIDEO = 2;

	private String imageUrl;
	private String name;
	private int id;
	private String videoPath;   // video url
	private int type; 			// ad category: 1 - image, 2 - video
	private String animationType;  // transform type for each ad image
	private String interval;  	// stay duration

	// local
	private String adUrl;

	/**
	 *
	 * convenience way to get ad url
	 * @return either image url or video path
	 */
	public String getAdUrl() {
		if (type == TYPE_VIDEO) { // video
			adUrl = videoPath;
		} else { 				  // image
			adUrl = imageUrl;
		}
		if (adUrl == null || TextUtils.isEmpty(adUrl)) {
			return "";
		}
		return adUrl;
	}

	public String getVideoPath() {
		return videoPath;
	}

	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAnimationType() {
		return animationType;
	}

	public void setAnimationType(String animationType) {
		this.animationType = animationType;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
