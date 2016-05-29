package com.androida.adcarousel.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.danikula.videocache.HttpProxyCacheServer;

public class VolleyUtils {

    public static final String TAG = VolleyUtils.class
            .getSimpleName();

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private com.nostra13.universalimageloader.core.ImageLoader UILImageLoader;
    private static Context mContext;

    private static VolleyUtils mInstance;

    private HttpProxyCacheServer proxy;

    public static HttpProxyCacheServer getProxy(Context context) {
        getInstance(context);
        return mInstance.proxy == null ? (mInstance.proxy = mInstance.newProxy(context)) : mInstance.proxy;
    }

    private HttpProxyCacheServer newProxy(Context context) {
        return new HttpProxyCacheServer.Builder(context)
                .maxCacheSize(1024 * 1024 * 1024)
                .maxCacheFilesCount(20)
//                .cacheDirectory(new File(AdView.LOCAL_VIDEO_PATH))
                .build();
    }

    private VolleyUtils(Context context) {
        mContext = context;
    }

    public static synchronized VolleyUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyUtils(context);
        }
        mContext = context;
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public static boolean readSourceAvailable(URL url) throws IOException {
        HttpURLConnection conn = null;
        conn = (HttpURLConnection) url.openConnection();
        int contentLength = conn.getContentLength();
        int responseCode = conn.getResponseCode();

        return contentLength > 0 && responseCode == HttpURLConnection.HTTP_OK;
    }
}