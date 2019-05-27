package jini.coding.homework;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FirebaseInstanceIDService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    NotificationCompat.BigPictureStyle style = null;
    NotificationCompat.Builder notificationBuilder = null;

    /*    @Override
        public void onNewToken(String s) {
            super.onNewToken(s);
            Log.d(TAG, "FirebaseInstanceIDService : " + s);
        }*/

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        System.out.println(remoteMessage);
        System.out.println( remoteMessage.getData().size());
        System.out.println(remoteMessage.getData());
        if (remoteMessage != null && remoteMessage.getData().size() > 0) {
            sendNotification(remoteMessage);
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");
        String intentUri = remoteMessage.getData().get("link");
        String imgSrc = remoteMessage.getData().get("imagesrc");

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (intentUri != null) {
            intent.putExtra("URL", intentUri);
        } else {
            intent.putExtra("URL", "");
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //오레오 채널 대응 코드 //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channel = "jini";
            String channel_nm = "jini";
            NotificationManager notichannel = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channelMessage = new NotificationChannel(channel, channel_nm, NotificationManager.IMPORTANCE_DEFAULT);
            channelMessage.setDescription("jinicoding");
            channelMessage.enableLights(true);
            channelMessage.enableVibration(true);
            channelMessage.setShowBadge(false);
            channelMessage.setVibrationPattern(new long[]{100, 200, 100, 200});
            notichannel.createNotificationChannel(channelMessage);
            notificationBuilder = new NotificationCompat.Builder(this, channel);
            notificationBuilder.setChannelId(channel);
        } else {
            notificationBuilder =new NotificationCompat.Builder(this, "");
        }

        notificationBuilder.setSmallIcon(R.drawable.jinicodingicon)
                .setColor(0xffffaec9)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent);

        if (imgSrc != null) {
            HttpURLConnection connection = null;
            URL url = null;
            try {
                url = new URL(imgSrc);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                style = new NotificationCompat.BigPictureStyle()
                        .bigPicture(myBitmap)
                        .setBigContentTitle(title)
                        .setSummaryText(message);
                notificationBuilder.setStyle(style);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
            }
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(9999, notificationBuilder.build());
    }
}
