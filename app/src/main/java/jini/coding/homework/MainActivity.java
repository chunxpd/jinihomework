package jini.coding.homework;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import jini.coding.homework.Remote.HttpRequest;
import jini.coding.homework.Remote.RealPathUtil;
import jini.coding.homework.Remote.RequestHttpURLConnection;


public class MainActivity extends BaseActivity {
    WebView webView;

    private static final String TYPE_IMAGE = "image/*";
    private static final int INPUT_FILE_REQUEST_CODE = 1;

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;

    private final long FINISH_INTERVAL_TIME = 2000;
    private long   backPressedTime = 0;
    String regId;

    private String PackageVersion="";
    private String MarketVersions="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // URL 설정.
       // String url = "http://jinihomework.co.kr/version.php";

        // AsyncTask를 통해 HttpURLConnection 수행.  (Market 버전 가져오기 위해 임의로 서버에 버전을 넣어둔거 가져오기
        NetworkTask networkTask = new NetworkTask(Common.getMARKET_VERSION(), null);
        networkTask.execute();

        mContext = this;

        webView  = (WebView) findViewById(R.id.WebView);
        webView.getSettings().setJavaScriptEnabled(true);

        // Enable pinch to zoom without the zoom buttons
        webView.getSettings().setBuiltInZoomControls(true);
        // Enable pinch to zoom without the zoom buttons
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            // Hide the zoom controls for HONEYCOMB+
            webView.getSettings().setDisplayZoomControls(false);
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            webView.getSettings().setTextZoom(100);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onCloseWindow(WebView w) {
                super.onCloseWindow(w);
                finish();
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                final WebSettings settings = view.getSettings();
                settings.setDomStorageEnabled(true);
                settings.setJavaScriptEnabled(true);
                settings.setAllowFileAccess(true);
                settings.setAllowContentAccess(true);
                view.setWebChromeClient(this);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(view);
                resultMsg.sendToTarget();
                return false;
            }

            // For Android Version < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                //System.out.println("WebViewActivity OS Version : " + Build.VERSION.SDK_INT + "\t openFC(VCU), n=1");
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(TYPE_IMAGE);
                startActivityForResult(intent, INPUT_FILE_REQUEST_CODE);
            }

            // For 3.0 <= Android Version < 4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                //System.out.println("WebViewActivity 3<A<4.1, OS Version : " + Build.VERSION.SDK_INT + "\t openFC(VCU,aT), n=2");
                openFileChooser(uploadMsg, acceptType, "");
            }

            // For 4.1 <= Android Version < 5.0
            public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
                Log.d(getClass().getName(), "openFileChooser : "+acceptType+"/"+capture);
                mUploadMessage = uploadFile;
                imageChooser();
            }

            // For Android Version 5.0+
            // Ref: https://github.com/GoogleChrome/chromium-webview-samples/blob/master/input-file-example/app/src/main/java/inputfilesample/android/chrome/google/com/inputfilesample/MainFragment.java
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                System.out.println("WebViewActivity A>5, OS Version : " + Build.VERSION.SDK_INT + "\t onSFC(WV,VCUB,FCP), n=3");
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;
                imageChooser();
                return true;
            }

            private void imageChooser() {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.e(getClass().getName(), "Unable to create Image File", ex);
                    }

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:"+photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType(TYPE_IMAGE);

                Intent[] intentArray;
                if(takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
            }
        });
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(Common.getHomepage());


    }

    /**
     * More info this method can be found at
     * http://developer.android.com/training/camera/photobasics.html
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INPUT_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mFilePathCallback == null) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }
                Uri[] results = new Uri[]{getResultUri(data)};

                mFilePathCallback.onReceiveValue(results);
                mFilePathCallback = null;
            } else {
                if (mUploadMessage == null) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }
                Uri result = getResultUri(data);

                Log.d(getClass().getName(), "openFileChooser : "+result);
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        } else {
            if (mFilePathCallback != null) mFilePathCallback.onReceiveValue(null);
            if (mUploadMessage != null) mUploadMessage.onReceiveValue(null);
            mFilePathCallback = null;
            mUploadMessage = null;
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Uri getResultUri(Intent data) {
        Uri result = null;
        if(data == null || TextUtils.isEmpty(data.getDataString())) {
            // If there is not data, then we may have taken a photo
            if(mCameraPhotoPath != null) {
                result = Uri.parse(mCameraPhotoPath);
            }
        } else {
            String filePath = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                filePath = data.getDataString();
            } else {
                filePath = "file:" + RealPathUtil.getRealPath(this, data.getData());
            }
            result = Uri.parse(filePath);
        }

        return result;
    }

    @SuppressLint("StaticFieldLeak")
    public void getRegistrationId() {
        String response = "";
        regId = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "" + regId);
        if (regId == null) {
            showToast(mContext, "토큰정보가 올바르지 않습니다");
        } else {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void[] params) {
                    String response = "";
                    HashMap<String, String> map = new HashMap<>();
                    try {
                        map.put("mobile_http_host", Common.getHttpHost());
                        map.put("mobile_key", Common.getAppKey());
                        map.put("mobile_token", regId);
                        HttpRequest req = new HttpRequest(Common.getTokenRegistPage());
                        response = req.prepare(HttpRequest.Method.POST).withData(map).sendAndReadString();
                    } catch (Exception e) {
                        response = e.getMessage();
                    }
                    return response;
                }

                @Override
                protected void onPostExecute(String result) {
                    super.onPostExecute(result);
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String msg = jsonObject.getString("msg");
                        if (msg.equals("FAIL")) {
                            showToast(mContext, "토큰정보에 문제가 발생하였습니다. 관리자에에 문의해주세요");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.execute();
        }
    }
    // HttpRequest 사용하여 웹서버에 있는 Version.php를 파싱하여 버전 가져옴
    public class NetworkTask extends AsyncTask<Void, Void, String> {
        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result; // 요청 결과를 저장할 변수.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            MarketVersions=s;
            getPackageVersion();
            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
            //    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            int compare=compareVersionNames(PackageVersion,MarketVersions);
            if(compare==0){  // 버전이 같을때
            }else{  // 업데이트 됐을때
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);       //Builder을 먼저 생성하여 옵션을 설정합니다.
                builder.setTitle("업데이트 안내");                                                                //타이틀을 지정합니다.
                builder.setMessage("업데이트 후 사용해주세요.");                                         //메시지를 지정합니다.

                builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {        //확인 버튼을 생성하고 클릭시 동작을 구현합니다.
                    @Override

                    public void onClick(DialogInterface dialog, int which) {
                        //"YES" Button Click
                        Intent marketLaunch = new Intent(
                                Intent.ACTION_VIEW);
                        marketLaunch.setData(Uri
                                .parse(Common.getMARKET_APP_LINK()));
                        startActivity(marketLaunch);
                        finish();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {       //취소 버튼을 생성하고 클릭시 동작을 구현합니다.
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //"NO" Button Click
                        //Toast.makeText(getApplicationContext(), "NO Button Click", Toast.LENGTH_LONG).show();
                    }
                });
                AlertDialog alert = builder.create();                                                       //빌더를 이용하여 AlertDialog객체를 생성합니다.
                alert.show();

            }

        }
    }

    public void getPackageVersion(){
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);

            PackageVersion = pInfo.versionName;
            int versionCode = pInfo.versionCode;
            Log.e("", "device_version : " + pInfo.versionName + "   " + pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
    public int compareVersionNames(String oldVersionName, String newVersionName) {

        int res = 0;

        String[] oldNumbers = oldVersionName.split("\\.");
        String[] newNumbers = newVersionName.split("\\.");

        // To avoid IndexOutOfBounds
        int maxIndex = Math.min(oldNumbers.length, newNumbers.length);

        for (int i = 0; i < maxIndex; i ++) {
            int oldVersionPart = Integer.valueOf(oldNumbers[i]);
            int newVersionPart = Integer.valueOf(newNumbers[i]);

            if (oldVersionPart < newVersionPart) {
                res = -1;
                break;
            } else if (oldVersionPart > newVersionPart) {
                res = 1;
                break;
            }
        }

        // If versions are the same so far, but they have different length...
        if (res == 0 && oldNumbers.length != newNumbers.length) {
            res = (oldNumbers.length > newNumbers.length)?1:-1;
        }

        return res;
    }

    @Override
    public void onBackPressed()
    {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;
        if(webView.canGoBack()){
            webView.goBack();
        } else {
            if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
                super.onBackPressed();
            } else {
                backPressedTime = tempTime;
                Toast.makeText(getApplicationContext(), "한번 더 뒤로가기 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
            }
        }

    }

}