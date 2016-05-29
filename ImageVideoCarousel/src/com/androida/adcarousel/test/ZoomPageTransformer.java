package com.androida.adcarousel.test;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.support.v4.view.ViewPager;

public class ZoomPageTransformer implements ViewPager.PageTransformer {
		private static final float MIN_SCALE = 0.85f;
		private static final float MIN_ALPHA = 0.5f;
		float translationX = 0;
		float alpha = 1;
		float scale = 1;

		@SuppressLint("NewApi")
		public void transformPage(View view, float position) {
			scale = Math.max(MIN_SCALE, 1 - Math.abs(position));
			int width = view.getWidth();
			// 当没有滑动时，当前页的position为0，即屏幕中心，左边相邻页的position为范围是[-,0),
			// 右边相邻页的position范围是(0, 1]
			// translationsX: view在水平方向的位移，当position大于1或者小于-1时，为不可见的其他页面
			// 在可见的三个页面：左，中，右，设置translationX为200，即三个页面都向右平移200像素，这样使原本
			// 左边的不可见页面变得可见，使右边的页面可见范围和左边的一样，因为默认viewpager的子页面宽度是占满屏幕的，
			// 所以要达到三个页面在同一屏幕的效果，
			// 必须设置页面宽度为小于屏幕宽度的某个值，设置是在Adapter的getItemWidth()里边
			if (position > 1 || position < -1) { // 不可见页面，还原translationX
				translationX = 0;
				scale = 1f;
			} else { // [-1, 1]， 即当前可见的左中右三个页面区域
				translationX = 0;//width / 6;// 屏幕宽度 / 8, 偏移屏幕宽度的1/8
				Log.d("hello", "translationX: " + translationX);
				// 设置alpha渐变动画，当position在离中心一定范围内[-0.7, 0.7]，恢复页面的alpha
				if (Math.abs(position) <= 0.7) {
					alpha = 1f;
				} else {
					alpha = (MIN_ALPHA + (scale - MIN_SCALE) / (1 - MIN_SCALE)
							* (1 - MIN_ALPHA));
				}
			}
			view.setTranslationX(translationX);
			view.setAlpha(alpha);
			view.setScaleX(scale);
			view.setScaleY(scale);
		}
	}