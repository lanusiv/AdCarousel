package com.androida.sample;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.androida.adcarousel.AdView;
import com.androida.adcarousel.model.AdEntity;
import com.example.lanusiv.adcarousel.R;

import java.util.ArrayList;
import java.util.List;

public class AdActivity extends FragmentActivity {
    private static final String TAG = "AdActivity";

    private static final String IMAGE[] = {
            "http://pic38.nipic.com/20140215/2844191_214643588144_2.jpg",
            "http://fairee.vicp.net:83/2015rm/0615/kele150615.mp4",
            "http://bbsatt.yineitong.com/forum/2011/07/14/11071407442710a8abe86c21c5.jpg",
            "http://fairee.vicp.net:83/2016rm/0122/coca160122.mp4",
            "http://www.photo0086.com/member/2027/waterpic/201206091502362368.JPG",
            "http://pic1a.nipic.com/2008-10-27/200810279215895_2.jpg",
            "http://tupian.enterdesk.com/2013/xll/012/26/3/1.jpg",
            "http://img10.3lian.com/sc6/show02/67/26/05.jpg",
            "http://fairee.vicp.net:83/2016rm/0318/kfc160318.mp4",
            "http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1307/23/c0/23655860_1374563879240.jpg",
            "http://himg2.huanqiu.com/attachment2010/2012/0821/20120821101204216.jpg",
            "http://pic7.nipic.com/20100514/2158700_153228545647_2.jpg"
    };

    private AdView adView;

    private TextView pageTV;

    private int currentPage = 0;

    private AdView.OnPageSelectedListener onPageSelectedListener = new AdView.OnPageSelectedListener() {

        @Override
        public void onPageSelected(int position) {
            int size = adView.getPageSize();
            pageTV.setText((position % size + 1) + "/" + size);
            currentPage = position;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adView = (AdView) findViewById(R.id.adView);
        pageTV = (TextView) findViewById(R.id.pages);

        adView.initAdView(this);
        adView.setInfiniteScroll(true);
        adView.setOnPageSelectedListener(onPageSelectedListener);
//        adView.setImageScaleType(ImageView.ScaleType.FIT_CENTER);
        adView.loadAdList(getData(0));

        int pageSize = adView.getPageSize();
        pageTV.setText((currentPage + 1) + "/" + pageSize);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    private List<AdEntity> getData(int c) {
        List<AdEntity> result = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            AdEntity ad = new AdEntity();
            ad.setImageUrl(IMAGE[i % IMAGE.length]);
            ad.setName("ad #" + i );
            ad.setType(AdEntity.TYPE_IMAGE);
            if (IMAGE[i % IMAGE.length].endsWith(".mp4")) {
                ad.setVideoPath(IMAGE[i % IMAGE.length]);
                ad.setType(AdEntity.TYPE_VIDEO);
            }
            result.add(ad);
        }
        return result;
    }
}
