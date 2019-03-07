package uoit.ca.ccproject;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;

public class NLService extends NotificationListenerService {
        public ArrayList<String>playerList;
        private String TAG = "TESTING";
        private NLServiceReceiver nlservicereciver;

        @Override
        public void onCreate() {
            super.onCreate();
            nlservicereciver = new NLServiceReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("uoit.ca.ccproject.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
            registerReceiver(nlservicereciver, filter);
            musicPlayerList();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            unregisterReceiver(nlservicereciver);
        }

        @Override
        public void onNotificationPosted(StatusBarNotification sbn) {
            try {
                Log.i(TAG, "**********  onNotificationPosted");
                Log.i(TAG, "ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText + "t" + sbn.getPackageName());
                Intent i = new Intent("uoit.ca.ccproject.NOTIFICATION_LISTENER_EXAMPLE");
                i.putExtra("notification_event", "onNotificationPosted :" + sbn.getPackageName() + "n");
                sendBroadcast(i);
            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG, "ERROR " + e);

            }
        }

        @Override
        public void onNotificationRemoved(StatusBarNotification sbn) {
            try {
                Log.i(TAG, "********** onNotificationRemoved");
                Log.i(TAG, "ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText + "t" + sbn.getPackageName());
                Intent i = new Intent("uoit.ca.ccproject.NOTIFICATION_LISTENER_EXAMPLE");
                i.putExtra("notification_event", "onNotificationRemoved :" + sbn.getPackageName() + "n");

                sendBroadcast(i);
            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG, "ERROR " + e);

            }
        }
        public void musicPlayerList(){
            playerList=new ArrayList<String>();
            playerList.add("spotify");
            playerList.add("blackplayer");
            playerList.add("music");
            playerList.add("player");

        }
    public class sendMusicAsync extends AsyncTask<Void, Void, String> {
        String title;
        String text;

        public sendMusicAsync(String ti, String te) {
            title = ti;
            text = te;
        }

        @Override
        protected String doInBackground(Void... voids) {
            Log.e(TAG, "Title: " + title.replaceAll(" ","+").toLowerCase());
            Log.e(TAG, "Text: " + text.replaceAll(" ","+").toLowerCase());

            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String url = "http://api.onemusicapi.com/20151208/release?title=" + title.replaceAll(" ","+").toLowerCase() + "&artist=" + text.replaceAll(" ","+").toLowerCase() + "&user_key=511f13fd5f3daea12fe39976ef0ba7ca";
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    Log.e(TAG, "Response from url: " + jsonStr);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "ERROR " + e);

                }
            }
            return null;

        }
    }

        public void findMusic(String packageName, String title, String text) {
            Log.e(TAG, "SEARCHING FOR MUSIC");

            for (String s : playerList) {
                //Log.e(TAG, "player"+s);
                //Log.e(TAG, "package"+packageName);

                if (packageName.toLowerCase().contains(s.toLowerCase())) {
                    Log.e(TAG, "FOUND PLAYER");

                    sendMusicAsync sma = new sendMusicAsync(title, text);
                    sma.execute();
                }
            }
        }

        class NLServiceReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                try {

                    Log.d("test", "recieve");
                    Log.e(TAG, "RECEIVE");


                    if (intent.getStringExtra("command").equals("clearall")) {
                        NLService.this.cancelAllNotifications();
                    } else if (intent.getStringExtra("command").equals("list")) {
                        Intent i1 = new Intent("uoit.ca.ccproject.NOTIFICATION_LISTENER_EXAMPLE");
                        i1.putExtra("notification_event", "=====================");
                        sendBroadcast(i1);
                        int i = 1;
                        for (StatusBarNotification sbn : NLService.this.getActiveNotifications()) {
                            Intent i2 = new Intent("uoit.ca.ccproject.NOTIFICATION_LISTENER_EXAMPLE");
                            try {
                                i2.putExtra("notification_event", i + " " + sbn.getPackageName() + "n Notification Details: " + sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                i2.putExtra("notification_event", i + " " + sbn.getPackageName() + "n ");
                            }
                            findMusic(sbn.getPackageName(), sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE).toString(), sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString());
                            sendBroadcast(i2);
                            i++;
                        }
                        Intent i3 = new Intent("uoit.ca.ccproject.NOTIFICATION_LISTENER_EXAMPLE");
                        i3.putExtra("notification_event", "===== Notification List ====");
                        sendBroadcast(i3);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "ERROR " + e);

                }

            }
        }
}
