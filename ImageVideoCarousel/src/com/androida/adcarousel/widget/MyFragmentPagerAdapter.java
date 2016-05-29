package com.androida.adcarousel.widget;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.ImageView;

import com.androida.adcarousel.AdFragmentStable;
import com.androida.adcarousel.model.AdEntity;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

	private List<AdEntity> dataList;
	private int size = 0;
	private int count = 0;
	private ImageView.ScaleType scaleType;

	public MyFragmentPagerAdapter(FragmentManager fm, List<AdEntity> data, boolean infinateScroll, ImageView.ScaleType scaleType) {
		super(fm);

		this.dataList = data;
		this.size = data.size();
		this.scaleType =scaleType;

		count = infinateScroll ? Integer.MAX_VALUE : dataList.size();
	}

	@Override
	public Fragment getItem(int arg0) {
		AdFragmentStable f = new AdFragmentStable();
		f.setItem(dataList.get(arg0 % size));
		f.setImageScaleType(scaleType);
		return f;
	}

	@Override
	public int getCount() {
		return count;
	}
}