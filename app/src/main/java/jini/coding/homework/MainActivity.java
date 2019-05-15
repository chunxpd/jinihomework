package jini.coding.homework;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends BaseActivity {
    WebView webview;
    ProgressDialog prDialog;
    BackPressCloseHandler backPressCloseHandler;
    String check_url;
    String regId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mContext = this;

        backPressCloseHandler = new BackPressCloseHandler(this);
        webview = (WebView)findViewById(R.id.WebView);
        webview.setWebViewClient(new WebClient());
        webview.setWebChromeClient(new WebChrome());
        webview.clearCache(true);
        WebSettings set = webview.getSettings();
        set.setJavaScriptEnabled(true);
        set.setLoadWithOverviewMode(true);
        set.setUseWideViewPort(true);
        set.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        Intent intent = this.getIntent();
        Bundle extras = intent.getExtras();
        String Url = null;
        if(extras != null){
            if(extras.containsKey("URL"))
            {
                Url = extras.getString("URL");
                if (Url.length() == 0) {
                    Url = null;
                }
            }
        }
        Log.d(TAG, "jinihomework : " + Url);
        if (Url == null) {
            webview.loadUrl(Common.getHomepage());
        } else {
            webview.loadUrl(Url);
        }

    }


    @Override
    public void onBackPressed() {
        if (check_url.equalsIgnoreCase(Common.getHomepage())) {
            backPressCloseHandler.onBackPressed();
        } else if(webview.canGoBack()){
            webview.goBack();
        }else{
            backPressCloseHandler.onBackPressed();
        }
    }



    class BackPressCloseHandler {
        private long backKeyPressedTime = 0;
        private Toast toast;
        private Activity activity;

        public BackPressCloseHandler(Activity context) {
            this.activity = context;
        }
        public void onBackPressed() {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                showGuide();
                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                activity.finish();
                toast.cancel();
            }
        }
        public void showGuide() {
            toast = Toast.makeText(activity, "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    class WebClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            prDialog.dismiss();
            Toast.makeText(MainActivity.this, "페이지를 로딩할 수 없습니다.", Toast.LENGTH_LONG).show();
            webview.loadUrl(Common.getError_page());
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            prDialog = new ProgressDialog(MainActivity.this);
            prDialog.setMessage("사이트 로딩중입니다");
            prDialog.show();
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            check_url = url;
            if(prDialog!=null){
                prDialog.dismiss();
            }
        }
    }

    class WebChrome extends WebChromeClient {
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("title")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    result.cancel();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }
    }

}

