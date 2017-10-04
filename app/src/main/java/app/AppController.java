package app;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
/**
 * Created by marmagno on 5/15/2017.
 */

public class AppController {

    private static AppController ourInstance;
    private RequestQueue ourRequestQueue;
    private ImageLoader oImageLoader;
    private static Context oContext;

    private AppController(Context context) {
        oContext = context;
        ourRequestQueue = getRequestQueue();

        oImageLoader = new ImageLoader(ourRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized AppController getInstance(Context context) {
        if (ourInstance == null) {
          ourInstance = new AppController(context);
        }
        return ourInstance;
    }

    public RequestQueue getRequestQueue() {
        if (ourRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            ourRequestQueue = Volley.newRequestQueue(oContext.getApplicationContext());
        }
        return ourRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req,String tag) {
        req.setTag(tag);
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return oImageLoader;
    }

    public void cancelPendingRequests(Object tag) {
        if (ourRequestQueue != null) {
            ourRequestQueue.cancelAll(tag);
        }
    }


}
