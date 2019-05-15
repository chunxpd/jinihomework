package jini.coding.homework;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.RequestQueue;
import jini.coding.homework.Remote.VolleySingleton;

import java.io.UnsupportedEncodingException;
import java.util.Date;

public class BaseActivity extends AppCompatActivity {

    public final String TAG = getClass().getSimpleName();

    RequestQueue queue;
    int methodCheck;
    Context mContext;

    //시간
    long now = System.currentTimeMillis();
    Date date = new Date(now);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate 호출됨");
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume 호출됨");
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy 호출됨");
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart 호출됨");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause 호출됨");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop 호출됨");
    }


    public void showToast(Context context, String contents) {
        Toast.makeText(context, contents, Toast.LENGTH_LONG).show();
    }

    public void showSnackbar(View view, String contents) {
        Snackbar snackbar = Snackbar.make(view, contents, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void volleyCacheRequest(String url){
        Cache cache = VolleySingleton.getInstance(getApplicationContext()).getRequestQueue().getCache();
        Cache.Entry entry = cache.get(url);
        if(entry != null){
            try {
                String data = new String(entry.data, "UTF-8");
                // handle data, like converting it to xml, json, bitmap etc.,
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        else{

        }
    }

    public void volleyInvalidateCache(String url){
        VolleySingleton.getInstance(getApplicationContext()).getRequestQueue().getCache().invalidate(url, true);
    }

    public void volleyDeleteCache(String url){
        VolleySingleton.getInstance(getApplicationContext()).getRequestQueue().getCache().remove(url);
    }

    public void volleyClearCache(){
        VolleySingleton.getInstance(getApplicationContext()).getRequestQueue().getCache().clear();
    }

}
